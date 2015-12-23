(ns meetdown.core
  (:require [reagent.core :as reagent :refer [atom]]
            [petrol.core :as petrol]
            [meetdown.processing]
            [meetdown.view :as view]))

;; define your app data so that it doesn't get over-written on reload

(def initial-state
  {:event {:name "" :speaker "" :description ""} :server-state nil :view {:name :new-event}})

(defonce !app
  (reagent/atom initial-state))

;; figwheel reload-hook
(defn reload-hook
  []
  (swap! !app identity))

(defn render-fn
  [ui-channel app]
  (reagent/render-component [view/root ui-channel app]
                            js/document.body))

(defn ^:export main
  []
  (enable-console-print!)
  (petrol/start-message-loop! !app render-fn))
