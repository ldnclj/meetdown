(ns meetdown.view
  (:require [petrol.core :refer [send! send-value!]]
            [meetdown.messages :as m]
            [cljs.core.match :refer-macros [match]]))

(defn- server-view
  [server-state]
  [:div
   [:label (str "server state: " server-state)]])


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
  [:div.container-fluid
   {:style {:display :flex :flex-direction :column :align-items :center}}
   [:h1.col-md-12 "Event Management Client"]
   (match [view]
          [{:name :new-event}] [event-form ui-channel event]
          [{:name :test}]      [:p "test render"]
          [{:name :event}]     [server-view server-state])])
