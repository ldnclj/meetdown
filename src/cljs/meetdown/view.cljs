(ns meetdown.view
  (:require [petrol.core :refer [send! send-value!]]
            [meetdown.messages :as m]))

(defn- server-view
  [server-state]
  [:div
   [:label (str "server state again: " server-state)]])


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
    [:td
     [:button.btn.btn-success
      {:on-click (send! ui-channel (m/->CreateEvent event))}
      "Go"]]]])


(defn root
  [ui-channel {:keys [event server-state]
               :as app}]
  [:div.container-fluid
   {:style {:display :flex :flex-direction :column :align-items :center}}
   [:h1.col-md-12 "Event Management Client"]
   [event-form ui-channel event]

   [:div
    [server-view server-state]]])
