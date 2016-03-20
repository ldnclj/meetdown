(ns meetdown.core
  (:require [com.stuartsierra.component :as component]
            [meetdown
             [data :as d]
             [http :as h]]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as core-appenders]))

(def timbre-config
  {:level     :info
   :appenders
   {:println (core-appenders/println-appender {:stream :auto})}})

(timbre/set-config! timbre-config)

(def config {:dburi "datomic:mem://meetdown"
             :server {:port 3000}})

(defrecord Datomic-connection-component [dburi connection]
  component/Lifecycle
  (start [component]
    (timbre/info "Starting Datomic connection for" dburi)
    (let [conn (d/setup-and-connect-to-db dburi)]
      (assoc component :connection conn)))
  (stop [component]
    (timbre/info "Stopping Datomic connection")
    (d/close-db)
    nil))

(defn new-database [dburi]
  (map->Datomic-connection-component {:dburi dburi}))

(defn meetdown-system [config]
  (let [{:keys [dburi server]} config]
    (component/system-map
     :db-component  (new-database dburi)
     :app           (component/using
                     (h/new-server server)
                     [:db-component]))))

(defn -main []
  (component/start (meetdown-system config)))

(comment
  ;; Start system by running (-main) or (user/go). Use (user/reset) to reload.
  ;; To create data run transact in comment in data.clj
  ;; To fetch events -
  ;;    curl -X POST -d "{:type :get-events}" http://localhost:3000/q --header "Content-Type:application/edn"

  ;; To reset and get a clean system again, call reset once more. The server will be shutdown and a new one started
  (reset)

  ;; Var user/system contains the current system
  (pprint user/system)

  ;; To create an event:
  ;;     curl -X POST -d "{:type :create-event :txn-data {:event/name \"test-event-name\"}}" http://localhost:3000/q --header "Content-Type:application/edn"
  ;; Or:
  (data/create-entity (user/system :db-conn) {:event/name "test-event"})

  ;; To get events:
  ;;     curl -X POST -d "{:type :get-events}" http://localhost:3000/q --header "Content-Type:application/edn"
  ;; Or:
  (data/get-events (user/system :db-conn))
  ;;(data/create-entity db-conn (:txn-data req-body)
  ;;{:type :create-event, :txn-data {:event/name "New event-2"}}

  )
