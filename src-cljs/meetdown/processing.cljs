(ns meetdown.processing
  (:require [meetdown.messages :as m]
            [meetdown.rest :as rest]
            [meetdown.utils :refer [remove-ns]]
            [petrol.core :refer [EventSource Message]]))

(extend-protocol Message
  m/ChangeEvent
  (process-message [event app]
    (let [event-minus-nils (into {} (remove (comp nil? second) event))]
      (assoc app :event (merge (:event app) event-minus-nils)))))

(extend-protocol EventSource
  m/CreateEvent
  (watch-channels [_ {:keys [event] :as app}]
    #{(rest/create-event event)}))

(extend-protocol EventSource
  m/CreateLocation
  (watch-channels [_ {:keys [location] :as app}]
    #{(rest/create-location location)}))

(extend-protocol Message
  m/ChangeLocation
  (process-message [location app]
    (let [location-minus-nils (into {} (remove (comp nil? second) location))]
      (assoc app :location (merge (:location app) location-minus-nils)))))

(defn- extract-event
  "Extract the event from the http response"
  [response]
  (->> response :body (reduce (remove-ns "event") {})))

(extend-protocol Message
  m/CreateEventResults
  (process-message [response app]
    (let [event-id (:id (extract-event response))
          new-app  (-> app
                       (assoc-in [:view :handler] :event)
                       (assoc-in [:view :route-params :id] event-id)
                       (assoc :event nil))]
      (.pushState (.-history js/window) "" "event" (str "#" event-id "-event")) ; TODO Need to remove this side effect from here to view where it belongs.
      new-app)))

(extend-protocol Message
  m/CreateLocationResults
  (process-message [response app]
    (let [location-id (get-in response [:body :db/id])
          new-app  (-> app
                       (assoc-in [:view :handler] :location)
                       (assoc-in [:view :route-params :id] location-id)
                       (assoc :location nil))]
      (.pushState (.-history js/window) "" "location" (str "#" location-id "-location")) ; TODO Need to remove this side effect from here to view where it belongs.
      new-app)))


(extend-protocol Message
  m/FindEventResults
  (process-message [response app]
    (let [event   (extract-event response)
          new-app (-> app
                      (assoc :server-state event)
                      (assoc-in [:view :handler] :event-found))]
      new-app)))

(extend-protocol Message
  m/FindLocationResults
  (process-message [response app]
    (let [location (:body response)
          new-app (-> app
                      (assoc :server-state location)
                      (assoc-in [:view :handler] :location-found))]
      new-app)))

(extend-protocol EventSource
  m/FindEvent
  (watch-channels [{:keys [id]} app]
    (let [id       (if (string? id) (long id) id)
          rest-res (rest/find-event id)]
      #{rest-res})))

(extend-protocol EventSource
  m/FindLocation
  (watch-channels [{:keys [id]} app]
    (let [id (if (string? id) (long id) id)
          rest-res (rest/find-location id)]
      #{rest-res})))
