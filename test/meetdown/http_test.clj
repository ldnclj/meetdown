(ns meetdown.http-test
  (:require [clojure.test :refer :all]
            [clojure.walk :refer [keywordize-keys]]
            [meetdown.core :as app]
            [meetdown.http :as http]
            [com.stuartsierra.component :as component]
            [ring.mock.request :refer [request body content-type header]])
  (:import [javax.servlet.http HttpServletResponse]))

(def test-system-config
  {:dburi    "datomic:mem://meetdown"
   :server   {:port 4000}})

(def test-system (atom nil))

(defn with-test-system [f]
  (swap! test-system (constantly (app/meetdown-system test-system-config)))
  (swap! test-system component/start)
  (f)
  (swap! test-system component/stop))

(use-fixtures :each with-test-system)

(deftest test-infrastructure-sanity-check
  (is (= [:db-component :app] (keys @test-system))))

(def create-event-url "http://localhost:4000/q")

(defn http-post [url data]
  (let [handler (http/make-handler (get-in @test-system [:app :db-component :connection]))]
    (keywordize-keys
     (handler (-> (request :post url)
                  (body (prn-str data))
                  (content-type "application/edn")
                  (header "Accept" "application/edn"))))))

(defn- extract-body
  ([response]
   (clojure.edn/read-string
    (slurp (response :body)))))

(defn- call-create-event [name]
  (http-post "http://localhost:4000/q" {:type :create-event :txn-data {:event/name name}}))

(defn- call-get-events []
  (http-post "http://localhost:4000/q" {:type :get-events}))

(defn- call-get-event [id]
  (http-post "http://localhost:4000/q" {:type :get-event :txn-data {:db/id id}}))

(deftest create-event-then-metadata-and-headers-set
  (let [response (call-create-event "test-event-name")
        headers  (response :headers)]
    (is (= (response :status ) HttpServletResponse/SC_OK))
    (is (= (headers :Content-Type) "application/edn; charset=utf-8"))))

(deftest create-event-then-id
  (let [response (call-create-event "test-event-name")]
    (is (not (= (extract-body response) nil)))))

(defn has? [x coll]
  (some #{x} coll))

(deftest create-event-then-metadata-and-headers-set
  (let [response (call-get-events)
        headers  (response :headers)]
    (is (= (response :status ) HttpServletResponse/SC_OK))
    (is (= (headers :Content-Type) "application/edn; charset=utf-8"))))

;; TODO Would be neater to blank the database before this test
(deftest get-events-then-event-exists
  (let [event-name "test-event-name"
        id         (:event/id (extract-body (call-create-event event-name)))
        events     (extract-body (call-get-events))]
    (is (has? {:db/id id :event/name event-name} events))))

(deftest get-event-when-event-exists
  (let [event-name "my-test-event"
        id         (:event/id (extract-body (call-create-event event-name)))
        actual-event (extract-body (call-get-event id))]
    (is (= {:db/id id :event/name event-name} actual-event))))

(comment
  (swap! test-system (constantly (app/meetdown-system test-system-config)))
  (swap! test-system component/start)

  (clojure.pprint/pprint @test-system)
  (:event/id (extract-body (call-create-event "name")))
  )
