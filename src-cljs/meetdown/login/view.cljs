(ns meetdown.login.view
  (:require [petrol.core :refer [send! send-value!]]
            [meetdown.login.messages :as ml]
            [meetdown.routes :refer [href-for]]
            [cljs.core.match :refer-macros [match]]
            [cljs.core.async :refer [put!]]))

(defn has-error?
  [field state]
  (not
    (empty?
      (filter  #(= field (:key %)) (:errors state)))))

(defn login-view
  [ui-channel login]
  [:form.form-horizontal {:on-submit (comp (send! ui-channel (ml/->Login)) #(doto % .preventDefault))}
   [:div.row
    [:h3.col-xs-12 "Login"]]
   [:div.form-group { :class (if (has-error? :username login) "has-error") }
    [:label.control-label.col-md-3 {:for "login-username-input"} "Username:"]
    [:div.col-md-9
      [:input.form-control { :id "login-username-input"
                            :type :text
               :value (:username login)
               :on-change (send-value! ui-channel #(ml/map->UsernameUpdated {:username %}))}]]]
   [:div.form-group { :class (if (has-error? :password login) "has-error") }
    [:label.control-label.col-md-3 {:for "login-password-input"} "Password:"]
    [:div.col-md-9
     [:input.form-control { :id "login-password-input"
                           :type :password
                           :value (:password login)
                           :on-change (send-value! ui-channel #(ml/map->PasswordUpdated {:password %}))}]]]
   [:div.input-group
    [:button.btn.btn-primary {:type :submit} "Login"]]])
