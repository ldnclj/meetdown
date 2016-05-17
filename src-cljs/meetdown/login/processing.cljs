(ns meetdown.login.processing
  (:require [meetdown.login.messages :as m]
            [meetdown.login.rest :as rest]
            [meetdown.utils :refer [remove-ns]]
            [petrol.core :refer [EventSource Message]]))

(extend-protocol Message
  m/UsernameUpdated
  (process-message [{:keys [username]} app]
    (assoc-in app [:login :username] username)))

(extend-protocol Message
  m/PasswordUpdated
  (process-message [{:keys [password]} app]
    (assoc-in app [:login :password] password)))

(extend-protocol Message
  m/LoginResults
  (process-message [{:keys [success body headers]} app]
    (assoc-in app [:authorization] (get headers "authorization"))))

(extend-protocol EventSource
  m/Login
  (watch-channels [_ {:keys [login] :as app}]
    (println "hello")
    #{(rest/login login)}))

