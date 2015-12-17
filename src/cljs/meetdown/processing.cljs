(ns meetdown.processing
  (:require [petrol.core :refer [Message EventSource]]
            [meetdown.messages :as m]
            [meetdown.rest :as rest]))


(extend-protocol Message
  m/ChangeEventName
  (process-message [{:keys [name]} app]
    (assoc-in app [:event :event/name] name)))

(extend-protocol Message
  m/ChangeEventSpeaker
  (process-message [{:keys [speaker]} app]
    (assoc-in app [:event :event/speaker] speaker)))

(extend-protocol Message
  m/CreateEvent
  (process-message [_ {:keys [event server-state] :as app}]
    (println "app=" app ", event=" event)
    (assoc app :server-state event)))

(extend-protocol EventSource
  m/CreateEvent
  (watch-channels [_ {:keys [event]
                      :as app}]
    (println "app in EventSource=" app ", event = " event)
    #{(rest/create-event event)}))

(extend-protocol Message
  m/CreateEventResults
  (process-message [response app]
    (println "Response= " response)
    (assoc app :server-state (-> response :body))))
