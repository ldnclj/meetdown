(ns meetdown.view
  (:require [petrol.core :refer [send! send-value!]]
            [meetdown.messages :as m]
            [cljs.core.match :refer-macros [match]]))

(defn- home-view
  []
  [:div.col-md-12.
   [:a {:href "#/new-event"} "Create new meetdown"]])

(defn- server-view
  [server-state]
  [:div.table
   [:div.row
    [:h3.col-md-12 "Server State"]]
   (for [event-attr server-state]
     (let [attr-name (-> event-attr first name)]
       [:div.row {:key attr-name}
        [:label.col-md-3 (-> attr-name (str ":"))]
        [:div.col-md-9 (second event-attr)]]))])


(defn- event-form
  [ui-channel {:keys [name speaker] :as event}]
  [:table.table
   [:tr
    [:td [:label "Event name:"]]
    [:td [:input {:type :text
                  :placeholder "Event name..."
                  :defaultValue name
                  :on-change (send-value! ui-channel m/->ChangeEventName)}]]]
   [:tr
    [:td [:label "Speaker:"]]
    [:td [:input {:type :text
                  :placeholder "Speaker..."
                  :defaultValue speaker
                  :on-change (send-value! ui-channel m/->ChangeEventSpeaker)}]]]
   [:tr
    [:td [:label "Description:"]]
    [:td [:input {:type :text
                  :placeholder "Description..."
                  :defaultValue speaker
                  :on-change (send-value! ui-channel m/->ChangeEventDescription)}]]]
   [:tr
    [:td
     [:button.btn.btn-success
      {:on-click (send! ui-channel (m/->CreateEvent event))}
      "Go"]]]])


(defn root
  [ui-channel {:keys [event server-state view]
               :as app}]
  [:div
   [:div.container
    [:div.row
     [:div.col-md-6.col-md-offset-3
      [:h2 "Event Management Client"]]]
    (match [view]
             [{:name :new-event}] [event-form ui-channel event]
             [{:name :test}]      [:p "test render"]
             [{:name :event}]     [server-view server-state]
             [{:name :home}]      [home-view])]])
