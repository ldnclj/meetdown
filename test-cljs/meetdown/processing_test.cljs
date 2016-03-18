(ns meetdown.processing-test
  (:require  [cljs.test :refer-macros [deftest is testing] :as t]
             [meetdown.messages :as m]
             [meetdown.processing :as p]
             [petrol.core :refer [process-message watch-channels]]
             [meetdown.rest :as rest]
             [clojure.test.check :as tc]
             [clojure.test.check.generators :as gen]
             [clojure.test.check.properties :as prop :include-macros true]
             [clojure.test.check.clojure-test :refer-macros [defspec]]))

(deftest test-extract-event
  (testing "Check that extract-event removes :event/ namespace from map returned in response from server"
   (let [response {:body {:event/name "name" :event/speaker "speaker" :event/description "desc" :db/id 12345}}]
      (is (= (contains? {:name "name" :speaker "speaker" :description "desc"} (meetdown.processing/extract-event response)))))))

(deftest test-change-event
  (let [event {:speaker "Me" :name "New event" :description "Some description"}
        app {:event event}]
    (testing "Check that speaker has been changed in event in app"
      (let [speaker "Changed"
            new-speaker-event (m/map->ChangeEvent {:speaker speaker})
            new-app (process-message new-speaker-event app)]
        (is (= speaker
               (get-in new-app [:event :speaker])))
        (is (= (assoc-in app [:event :speaker] nil)
               (assoc-in new-app [:event :speaker] nil)))))
    (testing "Check that name has been changed in event in app"
      (let [name "Changed"
            new-name-event (m/map->ChangeEvent {:name name})
            new-app (process-message new-name-event app)]
        (is (= name
               (get-in new-app [:event :name])))
        (is (= (assoc-in app [:event :name] nil)
               (assoc-in new-app [:event :name] nil)))))
    (testing "Check that description has changed in event in app"
      (let [description "Changed"
            new-description-event (m/map->ChangeEvent {:description description})
            new-app (process-message new-description-event app)]
        (is (= description
               (get-in new-app [:event :description])))
        (is (= (assoc-in app [:event :description] nil)
               (assoc-in new-app [:event :description] nil)))))))

(deftest test-watch-channels-create-event
  (testing "Check that watch-channel for CreateEvent returns a set that includes a map in which
 the event sent is a subset"
    (let [event {:speaker "Me" :name "event" :description "Some description"}
          app   {:event event}]
      (with-redefs [meetdown.rest/create-event (constantly (into {:db/id 1234} event))]
        (let [event-msg (m/map->CreateEvent nil)
              channel-content (watch-channels event-msg app)]
          (is (set? channel-content))
          (is (and
               (every? (set (keys (first channel-content))) (keys event))
               (every? (set (vals (first channel-content))) (vals event)))))))))

(deftest test-create-event-results
  (let [response {:body {:db/id 1234 :event/name "name" :event/speaker "speaker"}}
        app {:event {:name "chris"}}]
    (testing "Check that :event is set to nil"
      (let [new-app (process-message (m/map->CreateEventResults response) app)]
        (is (contains? new-app :event))
        (is (nil? (:event new-app)))))
    (testing "Check that :view has :handler of :event"
      (let [new-app (process-message (m/map->CreateEventResults response) app)]
        (is (= :event (get-in new-app [:view :handler])))))
    (testing "Check that :view has :route-params :id of :id from response"
      (let [new-app (process-message (m/map->CreateEventResults response) app)]
        (is (= 1234 (get-in new-app [:view :route-params :id])))))))

(deftest test-find-event-results
  (let [response {:body {:db/id 1234 :event/name "name" :event/speaker "speaker"}}
        app {:event {:name "chris"}}]
    (testing "Check process-message for FindEventResults returns event in :server-state"
      (let [new-app (process-message (m/map->FindEventResults response) app)]
        (is (= {:id 1234 :name "name" :speaker "speaker"} (:server-state new-app)))))
    (testing "Check process-message for FindEventResults has :handler in :view of :event-found"
      (let [new-app (process-message (m/map->FindEventResults response) app)]
        (is (= {:id 1234 :name "name" :speaker "speaker"} (:server-state new-app)))))))

(deftest test-find-event
  (let [event-id 1234
        event-name "dummy"
        app      {}]
    (testing "Check rest/find-event called for id"
      (with-redefs [rest/find-event (fn [id] {:name event-name :id id})]
        (let [event-found (watch-channels (m/->FindEvent event-id) app)]
          (is (= {:id event-id :name "dummy"} (first event-found)))
          (is (set? event-found))
          (is (= 1 (count event-found))))))))

(def event-map-gen (gen/hash-map :name gen/string :speaker gen/string :description gen/string))

(def event-response-map-gen (gen/hash-map :response event-map-gen))

(def create-event-results-gen (gen/fmap m/->CreateEventResults event-response-map-gen))

(def app-gen (gen/hash-map :event event-map-gen :view (gen/hash-map :handler gen/keyword)))

(defspec create-eventresults-process-message-sets-event-nil
  100
  (prop/for-all [create-event-results create-event-results-gen
                 app app-gen]
                (nil? (:event (process-message create-event-results app)))))


(defspec create-eventresults-process-message-view-handler-is-event
  100
  (prop/for-all [create-event-results create-event-results-gen
                 app app-gen]
                (= :event (get-in (process-message create-event-results app) [:view :handler]))))
