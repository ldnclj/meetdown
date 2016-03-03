(ns meetdown.http
  (:require [org.httpkit.server :as http]
            [com.stuartsierra.component :as component]
            [compojure.core :refer [routes GET POST DELETE ANY context]]
            [compojure.route :refer [resources files]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.defaults :as rmd]
            [meetdown.data :as data]
            [ring.middleware.cors :refer [wrap-cors]]
            [org.httpkit.server :refer [run-server]]
            [taoensso.timbre :as timbre]))

(defn- handle-query
  [db-conn]
  (fn [{req-body :body-params}]
    (let [db (data/database db-conn)]
      {:body (case (:type req-body)
               :get-events   (data/get-events db)
               :create-event {:db/id (:db/id (data/create-entity db-conn (:txn-data req-body)))}
               :get-event    (->> (get-in req-body [:txn-data :db/id])
                                  (data/to-ent (data/database db-conn)))
               :create-user  {:db/id (:db/id (data/create-entity db-conn (:txn-data req-body)))})})))

(def home-page
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     [:link {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" :integrity "sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" :crossorigin "anonymous"}]]
    [:body
     [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]]
     (include-js "js/compiled/meetdown.js")
     [:script {:type "text/javascript"} "addEventListener(\"load\", meetdown.cljscore.main, false);"]
     ]]))

(defn make-router [db-conn]
  (routes
   (resources "/")
   (GET "/" [] home-page)
   (POST "/q" []
         (handle-query db-conn))))

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
      (timbre/debug "Handling request:" req)
      (handler req))))

(defn make-handler [db-conn]
  (-> (make-router db-conn)
      (wrap-log-request)
      (wrap-restful-format :formats [:edn :transit-json])
      (rmd/wrap-defaults (assoc-in rmd/site-defaults [:security :anti-forgery] false))))


(defrecord Server-component [server-options db-component]
  component/Lifecycle
  (start [component]
    (println "Starting http-kit")
    (let [server (http/run-server (make-handler (:connection db-component)) server-options)]
      (assoc component :web-server server)))
  (stop [component]
    (println "Shutting down http-kit")
    (let [server (:web-server component)]
      (server :timeout 100))
    (assoc component :web-server nil)))

(defn new-server [server-options]
  (map->Server-component {:server-options server-options}))

(comment

  (data/create-entity (user/system :db-conn) (:txn-data {:event/name "New event-2"}))

  (data/create-entity (user/system :db-conn) (:txn-data req-body))

  )
