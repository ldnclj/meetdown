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

(def query-entity-url "http://localhost:4000/q")

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
  (http-post query-entity-url {:type :create-event :txn-data {:event/name name}}))

(defn- call-create-user [email]
  (http-post query-entity-url {:type :create-user :txn-data {:user/email email}}))

(defn- call-get-events []
  (http-post query-entity-url {:type :get-events}))

(defn- call-get-event [id]
  (http-post query-entity-url {:type :get-event :txn-data {:db/id id}}))

(deftest test-create-event-then-metadata-and-headers-set
  (let [response (call-create-event "test-event-name")
        headers  (response :headers)]
    (is (= (response :status ) HttpServletResponse/SC_OK))
    (is (= (headers :Content-Type) "application/edn; charset=utf-8"))))

(deftest test-create-event-then-id
  (let [body (extract-body (call-create-event "test-event-name"))]
    (is (not= body nil))
    (is (not (zero? (:db/id body
                            ))))))

(defn has? [x coll]
  (some #{x} coll))

(deftest test-create-event-then-metadata-and-headers-set
  (let [response (call-get-events)
        headers  (response :headers)]
    (is (= (response :status ) HttpServletResponse/SC_OK))
    (is (= (headers :Content-Type) "application/edn; charset=utf-8"))))

;; TODO Would be neater to blank the database before this test
(deftest test-get-events-then-event-exists
  (let [event-name "test-event-name"
        id         (:db/id (extract-body (call-create-event event-name)))
        events     (extract-body (call-get-events))]
    (is (has? {:db/id id :event/name event-name} events))))

(deftest test-get-event-when-event-exists
  (let [event-name "my-test-event"
        id         (:db/id (extract-body (call-create-event event-name)))
        actual-event (extract-body (call-get-event id))]
    (is (= {:db/id id :event/name event-name} actual-event))))

(deftest test-create-user
  (testing "Create a new user"
    (let [body (extract-body (call-create-user "newuser@dummy.com"))]
      (is (not (nil? body))))))

(deftest test-unsupported-type
  (testing "Ask for a type that is not supported"
    (let [response (http-post "/q" {:type :unsupported :txn-data {}})]
      (is (= (:status response) 415))
      (is (= (:body response) "Unsupported type - :unsupported")))))

(deftest load-home-page
  (testing "Home page loads"
    (let [response ((http/make-handler (get-in @test-system [:app :db-component :connection]))
                    (request :get "/"))]
      (is (re-find #"<div id=\"app\"" (:body response)))
      (is (re-find #"<script type=\"text/javascript\">addEventListener\(\"load\", meetdown.cljscore.main, false\);</script>" (:body response))))))


(comment

  (reset! test-system (component/start (app/meetdown-system test-system-config)))
  (:body (->
            (request :post "/q" {:type :unsupported :txn-data {}})
            ((http/make-handler (get-in @test-system [:app :db-component :connection])))))
  (component/stop @test-system)
  )
