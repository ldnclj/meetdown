(ns proclodo-reagent-spike.prod
  (:require [proclodo-reagent-spike.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
