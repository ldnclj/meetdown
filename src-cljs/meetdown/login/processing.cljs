(ns meetdown.login.processing
  (:require [cljs.core.async :refer [chan put!]]
            [meetdown.login.messages :as m]
            [meetdown.login.rest :as rest]
            [meetdown.utils :refer [remove-ns]]
            [petrol.core :refer [EventSource Message wrap]]))

(extend-protocol Message
  m/UsernameUpdated
  (process-message [{:keys [username]} app]
    (assoc-in app [:login :username] username)))

(extend-protocol Message
  m/PasswordUpdated
  (process-message [{:keys [password]} app]
    (assoc-in app [:login :password] password)))

(extend-protocol EventSource
  m/LoginResults
  (watch-channels [{:keys [success body headers]} app]
    (let [channel (chan)]
      (if success
        (do
          (put! channel {:body body, :headers headers})
          #{(wrap m/map->LoginSuccess channel)})
        (do
          (put! channel {:errors [{:form "Failed to login user with the server"}]})
          #{(wrap m/map->LoginFailure channel)})))))

(extend-protocol EventSource
  m/Login
  (watch-channels [_ {:keys [login] :as app}]
    (let [errors (reduce (fn [errs field]
                           (if (empty? (val field))
                             (conj errs { :error :empty-field :key (key field) })
                             errs))
                         []
                         (filter #(not (= :errors (key %))) (merge {:username "" :password ""} login)))
          error-channel (chan)]
      (if (empty? errors)
        #{(rest/login login)}
        (do
          (put! error-channel {:errors errors})
          #{(wrap m/map->LoginFailure error-channel)})))))

(extend-protocol Message
  m/LoginSuccess
  (process-message [{:keys [body headers]} app]
    (-> app
        (assoc-in [:authorization] (get headers "authorization"))
        (assoc :view :home)
        (dissoc :login))))

(extend-protocol Message
  m/LoginFailure
  (process-message [{:keys [errors]} app]
    (assoc-in app [:login :errors] errors)))

(extend-protocol Message
  m/Logout
  (process-message [_ app]
    (->
      app
      (dissoc :authorization)
      (assoc :view :login))
      ;; Todo - jxc - Should refactor to EventSource and notify the server
    ))
