(ns meetdown.http
  (:require [org.httpkit.server :as http]
            [com.stuartsierra.component :as component]
            [clojure.set :as set]
            [compojure.core :refer [routes GET POST DELETE ANY context]]
            [compojure.route :refer [resources files]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.defaults :as rmd]
            [meetdown.data :as data]
            [org.httpkit.server :refer [run-server]]
            [taoensso.timbre :as timbre]
            [meetdown.auth :as auth]
            [ring.util.response :refer [response status]]))

(defn wrap-assert-logged-in [handler]
  (fn [{:keys [user-id] :as req}]
    (if user-id
      (handler req)

      (-> (response :unauthenticated)
          (status 401)))))

(defn wrap-assert-user-roles [handler user-roles]
  (fn [{req-user-roles :user-roles, :as req}]
    (if (empty? (set/difference (set user-roles) (set req-user-roles)))
      (handler req)
      (-> (response :forbidden)
          (status 403)))))

(def auth-handlers
  ;; Auth: test with:
  ;; eval the 'add-user!' call in the bottom of auth.clj (well, it was when I wrote this)

  ;; curl -iXPOST http://localhost:8000/q -d '{:type :login, :username "james", :password "password-123"}' -H "Content-Type: application/edn"

  ;; curl -iXPOST http://localhost:8000/q -d '{:type :get-current-user}' -H "Content-Type: application/edn" -H "Authorization: <token>"

  {:get-current-user (-> (fn [{:keys [db body-params user-id user-roles]}]
                           (when user-id
                             {:body {:user-id user-id
                                     :user-roles user-roles}}))

                         wrap-assert-logged-in)

   :login (fn [{:keys [db body-params]}]
            (let [{:keys [username password]} body-params]
              (if-let [{:keys [user-id username]} (auth/check-login {:username username
                                                                     :password password}
                                                                    {:db db})]
                {:body {:user-id user-id
                        :username username}

                 :headers {"Authorization" (auth/generate-token {:user-id user-id
                                                                 :user-roles #{:admin}})}}

                (-> (response {:status :unauthenticated
                               :body-params body-params})
                    (status 401)))))})

(def event-handlers
  {:get-events (fn [{:keys [db body-params]}]
                 {:body (data/get-events db)})
   :create-location (fn [{:keys [db-conn body-params]}]
                                (let [response {:body {:db/id (:db/id (data/create-entity db-conn (:txn-data body-params)))}}]
                                  (timbre/debug "Location response: " response)
                                  response))
   :get-location  (fn [{:keys [db body-params]}]
                             {:body (->> (get-in body-params [:txn-data :db/id])
                                         (data/to-ent db))})
   :create-event (fn [{:keys [db db-conn body-params]}]
                   {:body {:db/id (:db/id (data/create-entity db-conn (:txn-data body-params)))}})

   :get-event (fn [{:keys [db body-params]}]
                {:body (->> (get-in body-params [:txn-data :db/id])
                            (data/to-ent db))})})

(def user-handlers
  {:create-user (fn [{:keys [db db-conn body-params]}]
                  {:body {:db/id (:db/id (data/create-entity db-conn (:txn-data body-params)))}})})

(def all-handlers
  (merge auth-handlers event-handlers user-handlers))

(defn- handle-query [req]
  (let [req-type (get-in req [:body-params :type])]
    (if-let [handler (get all-handlers req-type)]
      (handler req)
      {:status 415 :body (str "Unsupported type - " req-type)})))

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
      [:h3 "Loading...."]
      [:p "Loading application. Please wait.... "]]
     (include-js "js/compiled/meetdown.js")
     [:script {:type "text/javascript"} "addEventListener(\"load\", meetdown.cljscore.main, false);"]]]))

(defn make-router []
  (routes
    (resources "/")
    (GET "/" [] home-page)
    (POST "/q" []
      (fn [req]
        (handle-query req)))))

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

(defn wrap-log-response
  ([handler]
   (fn [req]
     (let [response (handler req)]
       (timbre/debug "Returning response:" response)
       response))))

(defn wrap-authenticate-user [handler]
  (fn [req]
    (handler (merge req
                    (auth/authenticate-user (get-in req [:headers "authorization"]))))))

(defn wrap-db [handler db-conn]
  (fn [req]
    (handler (assoc req
               :db-conn db-conn
               :db (data/database db-conn)))))

(defn user-authorised? [user-roles type]
  (let [roles-authorised (get-in all-handlers [type :user-roles])]
      (or (not roles-authorised)
          (some roles-authorised user-roles))))

(defn wrap-handle-authorisation [handler]
  (fn [{:keys [user-roles] :as req}]
    (if (user-authorised? user-roles (get-in req [:body-params :type]))
      (handler req)
      {:body :not-authorised
       :status 401})))

(defn make-handler [db-conn]
  (-> (make-router)
      wrap-handle-authorisation
      wrap-log-request
      (wrap-restful-format :formats [:edn :transit-json])
      wrap-authenticate-user
      (wrap-db db-conn)
      (rmd/wrap-defaults rmd/api-defaults)
      wrap-log-response))


(defrecord Server-component [server-options db-component web-server]
  component/Lifecycle
  (start [this]
    (if (:web-server this) ;; make start idempotent
      this
      (do
        (timbre/info "Starting http-kit for" server-options)
        (let [server (http/run-server (make-handler (:connection db-component)) server-options)]
          (assoc this :web-server server)))))
  (stop [this]
    (timbre/info "Shutting down http-kit")
    (let [server (:web-server this)]
      (if server ;; make stop idempotent
        (do
          (server :timeout 100)
          (-> this
              (assoc :web-server nil)
              (assoc :db-component nil)))
        this))))

(defn new-server [server-options]
  (map->Server-component {:server-options server-options}))

(comment

  (def conn (get-in user/system [:db-component :connection]))
  (data/create-entity conn {:event/name "New event-2"})

  (data/get-events (data/database conn))


  )
