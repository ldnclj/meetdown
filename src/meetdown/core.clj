(ns meetdown.core
  (:require [com.stuartsierra.component :as component]
            [meetdown
             [data :as d]
             [http :as h]]))

(def config {:dburi "datomic:mem://meetdown"
             :server {:port 3000}})

(defrecord Datomic-connection-component [dburi]
  component/Lifecycle
  (start [component]
    (println "Starting Datomic connection for " dburi)
    (let [conn (d/setup-and-connect-to-db dburi)]
      conn))
  (stop [component]
    (println "Stopping Datomic connection")
    (d/close-db)))

(defn new-database [dburi]
  (map->Datomic-connection-component {:dburi dburi}))

(defn meetdown-system [config]
  (let [{:keys [dburi server]} config]
    (println dburi)
    (component/system-map
     :dbconn  (new-database dburi)
     :handler (component/using
               (h/new-handler)
               [:dbconn])
     :app    (component/using
              (h/new-server server)
              [:handler]))))

(defn -main []
  (component/start
   (meetdown-system config)))

(comment
  ;; Start system by running (-main) or (meetdown.user/go). Use (meetdown.user/reset) to reload.
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
