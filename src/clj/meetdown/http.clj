(ns meetdown.http
  (:require [org.httpkit.server :as http]
            [com.stuartsierra.component :as component]
            [compojure.core :refer [routes GET POST DELETE ANY context]]
            [compojure.route :refer [resources]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.defaults :as rmd]
            [meetdown.data :as data]
            [ring.middleware.cors :refer [wrap-cors]]))

(defn- handle-query
  [db-conn]
  (fn [{req-body :body-params}]
    (let [db (data/database db-conn)]
     {:body (case (:type req-body)
              :get-events (data/get-events db)
              :create-event (do (println "Req body=" req-body)
                                (let [id (data/create-entity db-conn (:txn-data req-body))
                                      db (data/database db-conn)
                                      entity (data/to-ent db id)]
                                  entity)))
      :create-user  (data/create-entity db-conn (:txn-data req-body))})))

(def home-page
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css")]
    [:body
     [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]]
     (include-js "js/app.js")]]))

(defn app [dbconn]
  (-> (routes
       (GET "/" [] home-page)
       (POST "/q" []
             (handle-query dbconn))
       (resources "/"))
      (wrap-restful-format :formats [:edn :transit-json])
      (wrap-cors :access-control-allow-origin [#"http://localhost:3449"]
                 :access-control-allow-methods [:get :post])
      (rmd/wrap-defaults (-> rmd/site-defaults
                             (assoc-in [:security :anti-forgery] false)))))

(defrecord Server-component [server-options dbconn]
  component/Lifecycle
  (start [component]
    (println "Starting http-kit")
    (let [server (http/run-server (app dbconn) server-options)]
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
