(ns meetdown.http
  (:require [com.stuartsierra.component :as component]
            [compojure
             [core :refer [GET POST routes]]
             [route :refer [files]]]
            [hiccup
             [core :refer [html]]
             [page :refer [include-css include-js]]]
            [meetdown.data :as data]
            [org.httpkit.server :as http]
            [ring.middleware
             [defaults :as rmd]
             [format :refer [wrap-restful-format]]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn- handle-query
  [db-conn]
  (fn [{req-body :body-params}]
    (info "Handling query " req-body)
    {:body (case (:type req-body)
             :get-events   (data/get-events db-conn)
             :get-event    (let [id (get-in req-body [:txn-data :db/id])]
                             (data/get-entity db-conn id))
             :create-event (data/create-entity db-conn (:txn-data req-body))
             :create-user  (data/create-entity db-conn (:txn-data req-body)))}))

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

(defn make-router [db-conn]
  (-> (routes
       (GET "/" [] home-page)
       (POST "/q" []
             (handle-query db-conn))
       (files "/"))))

(def default-config
  {:params {:urlencoded true
            :keywordize true
            :nested     true
            :multipart  true}

   :responses {:not-modified-responses true
               :absolute-redirects     true
               :content-types          true}})

(defn wrap-log-request
  ([handler]
    (fn [req]
      (debug "Handling request" req)
      (handler req))))

(defn make-handler [db-conn]
  (-> (make-router db-conn)
      (wrap-log-request)
      (wrap-restful-format :formats [:edn :transit-json :json-kw])
      (rmd/wrap-defaults (-> rmd/site-defaults
                             (assoc-in [:security :anti-forgery] false)))))

(defrecord Handler-component [dbconn]
  component/Lifecycle
  (start [component]
    (println "Starting handler routes")
    (make-handler dbconn))
  (stop [component]
    nil))

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

  (data/create-entity (:dbconn user/system) (:txn-data {:event/name "New event-2"}))

  (data/create-entity (:dbconn user/system) (:txn-data req-body))

  (handle-query (:db-conn user/system))

  )
