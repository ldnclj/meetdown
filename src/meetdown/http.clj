(ns meetdown.http
  (:require [org.httpkit.server :as http]
            [com.stuartsierra.component :as component]
            [compojure.core :refer [routes GET POST DELETE ANY context]]
            [compojure.route :refer [files]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.defaults :as rmd]
            [meetdown.data :as data]))

(defn- handle-query
  [db-conn]
  (fn [{req-body :body-params}]
    {:body (case (:type req-body)
             :get-events (data/get-events db-conn)
             :create-event (data/create-entity db-conn (:txn-data req-body)))
             :create-user  (data/create-entity db-conn (:txn-data req-body))}))

(defrecord Handler-component [dbconn]
  component/Lifecycle
  (start [component]
    (println "Starting handler routes")
    (-> (routes
          (files "/")
          (POST "/q" []
                (handle-query dbconn)))
         (wrap-restful-format :formats [:edn :transit-json])
         (rmd/wrap-defaults (-> rmd/site-defaults
                                (assoc-in [:security :anti-forgery] false)))))
  (stop [component]))

(defn new-handler []
  (map->Handler-component {}))

(defrecord Server-component [server-options handler]
  component/Lifecycle
  (start [component]
    (println "Starting http-kit")
    (let [server (http/run-server handler server-options)]
      (assoc component :web-server server)))
  (stop [component]
    (println "Shutting down http-kit")
    (let [server (:web-server component)]
      (server :timeout 100))))

(defn new-server [server-options]
  (map->Server-component {:server-options server-options}))

(comment

  (data/create-entity meetdown.user/system (:txn-data {:event/name "New event-2"}))

  )
