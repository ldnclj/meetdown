(ns meetdown.config
  (:require [nomad :refer [defconfig]]
            [clojure.java.io :as io]))

(defconfig config (io/resource "meetdown-config.edn"))
