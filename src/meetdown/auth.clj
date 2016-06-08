(ns meetdown.auth
  (:require [clj-jwt.core :as jwt]
            [clj-time.core :as t]
            [meetdown.config :refer [config]]
            [clojure.tools.reader.edn :as edn]
            [buddy.hashers :as hash]
            [datomic.api :as d]))

(defn jwt-secret []
  (:jwt-secret (config)))

(defn authenticate-user [token]
  (when-let [{{:keys [sub user-roles]} :claims, :as jwt} (try
                                                           (jwt/str->jwt token)
                                                           (catch Exception e
                                                             ;; the token's invalid. we fall out of this function
                                                             nil))]
    (when (jwt/verify jwt (jwt-secret))
      {:user-id (java.util.UUID/fromString sub)
       :user-roles (edn/read-string user-roles)})))

(defn generate-token [{:keys [user-id user-roles]}]
  (-> {:iss "meetdown"
       :exp (t/from-now (t/days 1))
       :sub (str user-id)
       :user-roles (pr-str user-roles)}

      jwt/jwt
      (jwt/sign :HS256 (jwt-secret))
      jwt/to-str))

(defn hash-password [password]
  (hash/encrypt password {:alg :bcrypt+sha512}))

(defn verify-password [provided expected-hash]
  (hash/check provided expected-hash))

(comment
  (-> {:user-id "foo", :user-roles #{:admin}}
      generate-token
      authenticate-user)

  (let [super-secure-password "password-123"
        hash (hash-password super-secure-password)]
    [hash (verify-password super-secure-password hash)]))

(defn check-login [{:keys [username password]} {:keys [db]}]
  (when-let [{expected-hash :user/password-hash
              user-id :user/id
              username :user/username}
             (d/pull db
                     [:user/id :user/username :user/password-hash]
                     [:user/username username])]
    (when (verify-password password expected-hash)
      {:user-id user-id
       :username username})))

(defn add-user! [{:keys [username password]} {:keys [db-conn]}]
  (d/transact db-conn
              [{:db/id #db/id [:db.part/user]
                :user/id (d/squuid)
                :user/username username
                :user/password-hash (hash-password password)}]))

(comment
  (add-user! {:username "james", :password "password-123"}
             {:db-conn (get-in user/system [:db-component :connection])})

  (d/pull (d/db (get-in user/system [:db-component :connection]))
          [:user/id :user/username :user/password-hash]
          [:user/username "james"])

  (check-login {:username "james", :password "password-123"}
               {:db (d/db (get-in user/system [:db-component :connection]))}))
