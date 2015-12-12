(ns meetdown.http
  (:require [compojure.core :refer [routes GET POST DELETE ANY context]]
            [compojure.route :refer [files]]
            [meetdown.data :as data]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.defaults :as rmd]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn create-event [db-conn params]
  (info "Creating event with params" params)
  (data/create-entity db-conn (:txn-data params)))

(defn get-event [db-conn id]
  (info "Getting event" id))

(defn- handle-query
  [db-conn]
  (fn [{req-body :body-params}]
    (info "Handling query " req-body)
    {:body (case (:type req-body)
             :get-events   (data/get-events db-conn)
             :create-event (data/create-entity db-conn (:txn-data req-body)))
     :create-user  (data/create-entity db-conn (:txn-data req-body))}))

(defn make-router [db-conn]
  (-> (routes
       (files "/")
       (POST "/q" []
             (handle-query db-conn)))))

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
      (wrap-restful-format :formats [:edn :transit-json])
      (rmd/wrap-defaults (-> rmd/site-defaults
                             (assoc-in [:security :anti-forgery] false)))))

(defn start-http-server! [{:keys [db-conn http-opts handler] :as system}]
  (when (not handler)
    (let [opts    (get-in system [:config :http-kit])
          handler (make-handler (system :db-conn))]
      (info "Starting http server")
      (-> system
          (assoc :f-stop-http! (run-server handler opts))
          (assoc :handler      handler)))))

(defn stop-http-server! [{:keys [f-stop-http!] :as system}]
  (when f-stop-http!
    (info "Stopping http server")
    (f-stop-http!)
    (assoc system :f-stop-http! nil)))

(comment

  (data/create-entity user/foo-system (:txn-data {:event/name "New event-2"}))

  (data/create-entity db-conn (:txn-data req-body))

  )
