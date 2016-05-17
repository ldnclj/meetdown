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
            [org.httpkit.server :refer [run-server]]
            [taoensso.timbre :as timbre]
            [meetdown.auth :as auth]))

(def auth-handlers
  ;; Auth: test with:
  ;; curl -iXPOST http://localhost:3000/q -d "{:type :login, :user-id "james"}" -H "Content-Type: application/edn"

  ;; curl -iXPOST http://localhost:3000/q -d "{:type :get-current-user}" -H "Content-Type: application/edn" -H "Authorization: <token>"

  {:get-current-user {:handler (fn [{:keys [db body-params user-id user-roles]}]
                                 (when user-id
                                   {:body {:user-id user-id
                                           :user-roles user-roles}}))
                      :user-roles #{:admin}}

   ;; yes, we log *anyone* in...
   :login {:handler (fn [{:keys [db body-params]}]
                       {:body :logged-in
                        :headers {"Authorization" (auth/generate-token {:user-id (:user-id body-params)
                                                                        :user-roles #{:admin}})}})}})

(def event-handlers
  {:get-events {:handler (fn [{:keys [db body-params]}]
                           {:body (data/get-events db)})}

   :create-event {:handler (fn [{:keys [db db-conn body-params]}]
                             {:body {:db/id (:db/id (data/create-entity db-conn (:txn-data body-params)))}})}

   :get-event {:handler (fn [{:keys [db body-params]}]
                          {:body (->> (get-in body-params [:txn-data :db/id])
                                      (data/to-ent db))})}

   :create-location {:handler (fn [{:keys [db db-conn body-params]}]
                             {:body {:db/id (:db/id (java.util.UUID/randomUUID))}})}})

(def user-handlers
  {:create-user {:handler (fn [{:keys [db db-conn body-params]}]
                            {:body {:db/id (:db/id (data/create-entity db-conn (:txn-data body-params)))}})}})

(def all-handlers
  (merge auth-handlers event-handlers user-handlers))

(defn- handle-query [req]
  (let [req-type (get-in req [:body-params :type])]
    (if-let [handler (get-in all-handlers
                             [req-type :handler])]
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
      (rmd/wrap-defaults rmd/api-defaults)))


(defrecord Server-component [server-options db-component]
  component/Lifecycle
  (start [component]
    (timbre/info "Starting http-kit for" server-options)
    (let [server (http/run-server (make-handler (:connection db-component)) server-options)]
      (assoc component :web-server server)))
  (stop [component]
    (timbre/info "Shutting down http-kit")
    (let [server (:web-server component)]
      (server :timeout 100))
    (-> component
        (assoc :web-server nil)
        (assoc :db-component nil)
        (assoc :server-options nil))))

(defn new-server [server-options]
  (map->Server-component {:server-options server-options}))

(comment

  (data/create-entity (user/system :db-conn) (:txn-data {:event/name "New event-2"}))

  (data/create-entity (user/system :db-conn) (:txn-data req-body))

  )
