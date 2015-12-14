(ns meetdown.view
  (:require [petrol.core :refer [send! send-value!]]
            [meetdown.messages :as m]))


(defn- new-event-form
  [ui-channel event]
  [:div
   [:input {:type :text
            :placeholder "Enter event name..."
            :defaultValue (:name event)
            }
    [:button.btn.btn-success
     {:on-click (js/alert "Clicked!")}]]])

(def root
  [ui-channel {:keys [event]}]
  [:div {:style {:display :flex
                 :flex-direction :column
                 :align-terms :center}}
   [:h1 "Host you own event"]
   [new-event-form]])
