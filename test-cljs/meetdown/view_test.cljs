(ns meetdown.view-test
  (:require [cljs.core.async :refer [<! chan >!]]
            [cljs.test :as t :refer-macros [async deftest is testing]]
            [meetdown.view :as v]
            [meetdown.messages :refer [FindEvent]]
            [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(deftest test-async
  (testing "Just a toy test to try out async testing."
   (let [c (chan)]
      (cljs.test/async done
                       (go
                         (>! c "hello"))
                       (go
                         (let [msg (<! c)]
                           (is (= "hello" msg))))
                       (done)))))

(deftest test-event-lookup
  (let [c (chan)]
    (async done
           (testing "Test that event-lookup puts the route-params from view to the ui-channel in the form of a FindEvent record."
                      (v/event-lookup c {:route-params {:id 1234}})
                      (go
                        (let [msg (<! c)]
                          (is (= 1234 (:id msg)))
                          (is (= FindEvent (type msg))))
                        (done))))))

;; FYI When I run run-tests through the repl in CIDER using (cljs.test/run-tests 'meetdown.view-test) the output goes to the browser console!
;; Not sure what to do about this but I'll live with it for now.
