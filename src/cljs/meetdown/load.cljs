(ns meetdown.load
  (:require [meetdown.corecljs :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
