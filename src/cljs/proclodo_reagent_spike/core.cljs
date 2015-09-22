(ns proclodo-reagent-spike.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [reagent-forms.core :as forms]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:import goog.History))

(defonce state (atom {:event {:name "event"} :saved? false}))
(defonce server-state (atom {}))
(defonce click-count (atom 0))

(defonce event-channel (chan))

(defonce save-event
  (go-loop []
    (let [event (<! event-channel)]
     (swap! server-state assoc :event event)
     (recur))))

;;--------------------------
;; Forms
(defn row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 [input]]])

(defn input [label type id]
  (row label [:input.form-control {:field type :id id}]))

(comment
  (defn set-value! [id value]
    (swap! state assoc :saved? false)
    (swap! state assoc-in [:event id] value)))

(defn get-value [id]
  (get-in @state [:event id]))

(defn text-input [id label state-local]
  [row label
   (fn []
     [:input
      {:type "text"
       :class "form-control"
       :value @state-local
       :on-change #(reset! state-local (-> % .-target .-value))}])])

(defn new-event-form []
  (let [event-state (atom "")]
    (fn []
     [:div
      [text-input :name "Event name" event-state]
       [:button.btn.btn-default
        {:on-click
         (fn [_]
           (swap! state assoc-in [:event :name] @event-state)
           (go (>! event-channel @event-state)))}
        "Create"]
       [:div
        [:label (get-in @state [:event :name])]]])))

(defn counting-component []
  [:div
   "The atom " [:code "click-count"] " has value: "
   @click-count ", "
   [:input {:type "button" :value "Click me!"
            :on-click #(swap! click-count inc)}]])
  ;; -------------------------
  ;; Views

(defn home-page []
  [:div [:h2 "Welcome to proclodo-reagent-spike"]
   (counting-component)
   [:div [:a {:href "#/new-event"} "create event"]]])

(defn new-event []
  [:div [:h2 "About proclodo-reagent-spike"]
   [new-event-form]
   [:div [:p "Server state " (:event @server-state)]]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/new-event" []
  (session/put! :current-page #'new-event))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
