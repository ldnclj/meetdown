(ns meetdown.rest
  (:require [cljs-http.client :as http]
            [petrol.core :as petrol]
            [meetdown.messages :as m]
            [meetdown.utils :refer [add-ns]]
            [cljs.core.async :refer [>! <! chan]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn create-event
  [event]
  (let [rest-event (reduce (add-ns "event") {} event)
        event-channel (petrol/wrap m/map->CreateEventResults
                                   (http/post "/q"
                                              {:with-credentials? false
                                               :edn-params {:type :create-event
                                                            :txn-data rest-event}}))]
    event-channel))

(defn create-location
  [location]
  (let [rest-location (reduce (add-ns "location") {} location)
        event-channel (petrol/wrap m/map->CreateLocationResults
                                   (http/post "/q"
                                              {:with-credentials? false
                                               :edn-params {:type :create-location
                                                            :txn-data rest-location}}))]
    event-channel))

(defn find-event
  [id]
  (let [post-channel (petrol/wrap m/map->FindEventResults
                                  (http/post "/q"
                                             {:with-credentials? false
                                              :edn-params {:type :get-event
                                                           :txn-data {:db/id id}}}))]
    post-channel))

(defn find-location
  [id]
  (let [post-channel (petrol/wrap m/map->FindLocationResults
                                  (http/post "/q"
                                             {:with-credentials? false
                                              :edn-params {:type :get-location
                                                           :txn-data {:db/id id}}}))]
    post-channel))
