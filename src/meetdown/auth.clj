(ns meetdown.auth
  (:require [clj-jwt.core :as jwt]
            [clj-time.core :as t]
            [meetdown.config :refer [config]]
            [clojure.tools.reader.edn :as edn]))

(defn jwt-secret []
  (:jwt-secret (config)))

(defn authenticate-user [token]
  (when-let [{{:keys [sub user-roles]} :claims, :as jwt} (try
                                                           (jwt/str->jwt token)
                                                           (catch Exception e
                                                             ;; the token's invalid. we fall out of this function
                                                             nil))]
    (when (jwt/verify jwt (jwt-secret))
      {:user-id sub
       :user-roles (edn/read-string user-roles)})))

(defn generate-token [{:keys [user-id user-roles]}]
  (-> {:iss "meetdown"
       :exp (t/from-now (t/days 1))
       :sub user-id
       :user-roles (pr-str user-roles)}

      jwt/jwt
      (jwt/sign :HS256 (jwt-secret))
      jwt/to-str))

(comment
  (-> {:user-id "foo", :user-roles #{:admin}}
      generate-token
      authenticate-user))
