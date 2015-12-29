(ns meetdown.processing
  (:require [meetdown.messages :as m]
            [meetdown.rest :as rest]
            [meetdown.utils :refer [remove-ns]]
            [petrol.core :refer [EventSource Message]]))

(extend-protocol Message
  m/ChangeEventName
  (process-message [{:keys [name]} app]
    (assoc-in app [:event :name] name)))

(extend-protocol Message
  m/ChangeEventSpeaker
  (process-message [{:keys [speaker]} app]
    (assoc-in app [:event :speaker] speaker)))

(extend-protocol Message
  m/ChangeEventDescription
  (process-message [{:keys [description]} app]
    (assoc-in app [:event :description] description)))

(extend-protocol Message
  m/CreateEvent
  (process-message [_ {:keys [event server-state] :as app}]
    app))

(extend-protocol EventSource
  m/CreateEvent
  (watch-channels [_ {:keys [event] :as app}]
    (println "app in EventSource=" app ", event = " event)
    #{(rest/create-event event)}))

(defn- extract-event
  "Extract the event from the http response"
  [response]
  (->> response :body (reduce (remove-ns "event") {})))

(extend-protocol Message
  m/CreateEventResults
  (process-message [response app]
    (println "response = " response)
    (let [event-id (-> (extract-event response) :id)
          new-app  (-> app
                       (assoc-in [:view :handler] :event)
                       (assoc-in [:view :route-params :id] event-id))]
      (.pushState (.-history js/window) "" "event" (str "#" event-id "-event"))
      (println "create event results - new app = " new-app)
      new-app)))

(extend-protocol Message
  m/FindEventResults
  (process-message [response app]
    (println "find event results response - " response)
    (let [event   (extract-event response)
          new-app (-> app
                      (assoc :server-state event)
                      (assoc-in [:view :handler] :event-found))]
      (println "find-event found = " event)
      (println "new-app = " new-app)
      new-app)))

(extend-protocol EventSource
  m/FindEvent
  (watch-channels [{:keys [id]} app]
    (println "find event source = " app ", id = " id)
    (let [id       (if (string? id) (long id) id)
          rest-res (rest/find-event id)]
      (println "rest-res = " rest-res)
      #{rest-res})))
