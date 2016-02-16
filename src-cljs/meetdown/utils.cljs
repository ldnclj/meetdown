(ns meetdown.utils
  (:require [clojure.string :as str]))

(defn add-ns
  "Add the specified namespace to the keywords in the map"
  [ns]
  (fn [m [k v]] (assoc m (->> (name k) (str ns "/") keyword) v)))


(defn remove-ns
  "Remove the specified namespace from the keywords in the map"
  [ns]
  (fn [m [k v]] (assoc m (-> (name k) (str/replace (re-pattern (str ns "/")) "") keyword) v)))
