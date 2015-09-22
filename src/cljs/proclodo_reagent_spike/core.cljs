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

(defonce state (atom {:event {:name "event"
                              :description ""} :saved? false}))
(defonce server-state (atom {}))
(defonce click-count (atom 0))

(defonce event-channel (chan))

(defonce save-event
  (go-loop []
    (let [event (<! event-channel)]
     (swap! server-state assoc :event event) ;; TODO replace with a call to server
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

(defn text-input
  ([id state-local]
   (text-input id state-local (name id) (if (vector? id) id (vector id)))
   )
  ([id state-local label path]
  [row label
   (fn []
     [:input
      {:type "text"
       :class "form-control"
       :value (get-in @state-local path )
       :on-change #(reset! state-local
                           (assoc-in @state-local
                                  path (-> % .-target .-value)))}])]))
(defn text-area
  ([id state-local]
   (text-area id state-local (name id) (if (vector? id) id (vector id)))
   )
  ([id state-local label path]
   [row label
    (fn []
      [:textarea
       {:class "form-control"
        :value (get-in @state-local path )
        :on-change #(reset! state-local
                            (assoc-in @state-local
                                   path (-> % .-target .-value)))}])]))
(defn location-input [path state-local]
  [:div
   [:hr]
   [text-input :street-address state-local "Street address" [path :street-address]]
   [text-input :postcode state-local "Postcode" [path :postcode]]
   [:hr]
   ])

(defn new-event-form []
  (let [event-state (atom {:name "NAME"
                           :description "DESC"
                           :location {:street-address "STREET"
                                      :postcode "POST"}
                           :date "DATE"
                           :start-time "START"
                           :end-time "END"
                           :speaker "SPEAK"})]
    (fn []
     [:div
      [text-input :name event-state "Event name" [:name]]
      [text-area :description event-state]
      [location-input :location event-state]
      [text-input :date event-state]
      [text-input :start-time event-state]
      [text-input :end-time event-state]
      [text-input :speaker event-state]
       [:button.btn.btn-default
        {:on-click
         (fn [_]
           (swap! state assoc :event @event-state)
           (go (>! event-channel @event-state)))}
        "Create"]])))

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
  (let [current-page (session/get :current-page)]
    (if (vector? current-page)
      [:div [(get current-page 0) (get current-page 1)]]
      [:div [current-page]]
      ))
  )

(defn show-event [id]
  [:div id])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/new-event" []
  (session/put! :current-page #'new-event))

(secretary/defroute "/event/:id" {:as params}
  (session/put! :current-page [#'show-event (:id params)]))

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
