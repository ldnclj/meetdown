(ns meetdown.core
  (:require [ajax.core :as ajax]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts!]]
            [clojure.string :as str]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:import goog.History))

(defonce state (atom {:event {:name "event"
                           :description ""
                           :location {:street-address ""
                                      :postcode ""}
                           :date ""
                           :start-time ""
                           :end-time ""
                           :speaker ""} :saved? false}))
(defonce server-state (atom {}))
(defonce click-count (atom 0))

(defonce event-channel (chan))

(defn create-event-link
  "Render the create event link"
  []
  [:div [:a {:href "#/new-event"} "Create event"]])

(defn home-page-link
  "Render the home page link"
  []
  [:div [:a {:href "#/"} "Go to the Home Page"]])

(defn post-event
  "Post a new event to the server."
  [event]
  (ajax/POST "/q"
             {:params          {:type  :create-event
                                :event event}
              :handler         (fn [response]
                                 (set! (.-hash (.-location js/window)) (str "/event/" (response "id"))))
              :error-hander    (fn [& args] (println "NOT OK" args))
              :format          (ajax/json-request-format)
              :response-format (ajax/json-response-format)}))

(defonce save-event
  (go-loop []
    (let [event (<! event-channel)]
      (post-event event)
      (recur))))

;;--------------------------
;; Forms
(defn row
  "Renders a row containing content"
  [label content]
  [:div.row
   [:div [:label label]]
   [:div [content]]])

(defn input
  "Renders an input field of specified type."
  [label type id]
  (row label [:input.form-control {:field type :id id}]))

(defn id->label
  "Convert element id to label"
  [id]
  (str/capitalize (str/replace (name id) "-" " ")))

(defn text-input
  "Render a text input field."
  ([id state-local]
   (let [label (id->label id)]
     (text-input id state-local label)))
  ([id state-local label]
   (let [path (if (vector? id) id (vector id))]
     (text-input id state-local label path)))
  ([id state-local label path] (text-input id state-local label path label))
  ([id state-local label path placeholder]
   [row label
    (fn []
      [:input
       {:type :text
        :placeholder placeholder
        :class "form-control"
        :value (get-in @state-local path)
        :on-change #(reset! state-local
                            (assoc-in @state-local
                                      path (-> % .-target .-value)))}])]))
(defn text-area
  "Render a text area."
  ([id state-local]
   (let [label (id->label id)
         path (if (vector? id) id (vector id))]
     (text-area id state-local label path)))
  ([id state-local label path] (text-area id state-local label path label))
  ([id state-local label path placeholder]
   [row label
    (fn []
      [:textarea
       {:class "form-control"
        :placeholder placeholder
        :value (get-in @state-local path )
        :on-change #(reset! state-local
                            (assoc-in @state-local
                                   path (-> % .-target .-value)))}])]))

(defn location-input
  "Render the location for the event."
  [path state-local]
  [:div
   [:hr]
   [text-input :street-address state-local "Street address" [path :street-address]]
   [text-input :postcode state-local "Postcode" [path :postcode]]
   [:hr]])

(defn new-event-form
  "Render a form to enter a new event."
  []
  (let [event-state (atom {:name ""
                           :description ""
                           :location {:street-address ""
                                      :postcode ""}
                           :date ""
                           :start-time ""
                           :end-time ""
                           :speaker ""})]
    (fn []
      [:div
       {:style {:display :flex
                :margin "20px"
                :flex-direction :column
                :align-items :center}}
       [text-input :name event-state "Event name" [:name]]
       [text-area :description event-state]
       [location-input :location event-state]
       [text-input :date event-state]
       [text-input :start-time event-state]
       [text-input :end-time event-state]
       [text-input :speaker event-state]
       [:div
        {:style {:display :flex
                 :margin "20px"
                 :flex-direction :column
                 :align-items :center}}
        [:button.btn.btn-success
         {:on-click
          (fn [_]
            (swap! state assoc :event @event-state)
            (swap! state assoc :saved? true)
            (go (>! event-channel @event-state)))}
         "Create"]]])))

(defn counting-component
  "Render the good old counting component."
  []
  [:div
   "The atom " [:code "click-count"] " has value: "
   @click-count ", "
   [:button.btn.btn-success {:on-click #(swap! click-count inc)}
    "Click me!"]])

  ;; -------------------------
  ;; Views

(defn home-page
  "Render the home page with the counting component."
  []
  [:div {:style {:display :flex
                 :flex-direction :column
                 :align-items :center}}
   [:h2 "Welcome to meetdown"]
   [counting-component]
   [create-event-link]])

(defn new-event
  "Render the new event page"
  []
  [:div {:style {:display :flex
                 :flex-direction :column
                 :align-items :center}}
   [:h2 "Add a new event"]
   [new-event-form]
   [:div [:p "Server state " (:event @server-state)]]
   [home-page-link]])

(defn current-page
  "Render the current page."
  []
  (let [current-page (session/get :current-page)]
    (if (vector? current-page)
      [:div [(get current-page 0) (get current-page 1)]]
      [:div [current-page]])))

(defn event-row
  "Render a row for an event attribute"
  [[key text]]
  (if-not (map? text)
    (let [label (id->label key)]
     [:div [:label label ":"] " " text])
    (for [attribute text]
      (event-row attribute))))

(defn show-event
  "Render a page showing an existing event"
  [id]
  [:div {:style {:display :flex
                 :flex-direction :column
                 :align-items :center}}
   [:h2 "Event : " id]
   [:div
    (for [attribute (:event @state)]
      (event-row attribute))]
   [create-event-link]
   [home-page-link]])

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
