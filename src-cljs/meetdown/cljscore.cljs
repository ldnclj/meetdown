(ns meetdown.cljscore
  (:require [reagent.core :as reagent :refer [atom]]
            [petrol.core :as petrol]
            [petrol.routing :as petrol-routing]
            [meetdown.routes :refer [frontend-routes]]
            [meetdown.processing]
            [meetdown.login.processing]
            [meetdown.view :as view]))

;; define your app data so that it doesn't get over-written on reload

(def initial-state
  {:event {:name "" :speaker "" :description ""} :location {:postCode ""} :server-state nil :view {:handler :home}})

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
  (petrol/start-message-loop!
   !app
   render-fn
   #{(petrol-routing/init frontend-routes)}))
