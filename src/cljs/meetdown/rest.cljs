(ns meetdown.rest
  (:require [cljs-http.client :as http]
            [petrol.core :as petrol]
            [meetdown.messages :as m]))

(defn- add-ns
  "Add the specified namespace to the keywords in the map"
  [ns]
  (fn [m [k v]] (assoc m (->> (name k) (str ns "/") keyword) v)))

(defn create-event
  [event]
  (let [rest-event (reduce (add-ns "event") {} event)]
   (->> (http/post "http://localhost:3000/q"
                   {:with-credentials? false
                    :edn-params {:type :create-event
                                 :txn-data rest-event}})
        (petrol/wrap m/map->CreateEventResults))))
