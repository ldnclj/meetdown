(ns meetdown.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [meetdown.view-test]
   [meetdown.processing-test]))

(enable-console-print!)

(doo-tests 'meetdown.processing-test
           'meetdown.view-test)

(defn ^:export runner []
  )
