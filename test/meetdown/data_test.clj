(ns meetdown.data-test
  (:require [meetdown.data :refer :all]
            [clojure.test :refer :all]
            [datomic.api :as d]))


(def connection-config {:dburi "datomic:mem://meetdown-test"})

(def test-conn (atom nil))

(defn with-test-conn [f]
  (swap! test-conn (constantly (setup-and-connect-to-db (:dburi connection-config))))
  (f))

(use-fixtures :each with-test-conn)

(defn- id-for-existing-event
  [tx]
  (d/resolve-tempid (:db-after tx)
                  (:tempids tx)
                  (d/tempid :db.part/user -1)))

(defn- speculate
  ([] (speculate nil))
  ([tx]
   (if tx
     (fn [conn tx-data] (d/with (:db-after tx) [tx-data]))
     (fn [conn tx-data] (d/with (d/db @test-conn) [tx-data])))))

(deftest test-create-event
  (testing "Create an event that doesn't exist."
    (with-redefs [meetdown.data/store-entity (speculate)]
     (is (> (:db/id (create-entity @test-conn {:event/name "My new event"})) 0))))
  (testing "Create an event that does exist."
    (let [event-details {:db/id #db/id[:db.part/user -1] :event/name "event exists"}
          tx ((speculate) @test-conn event-details)
          id (id-for-existing-event tx)
          stored-event {:db/id id :event/name "event name changed"}]
      (with-redefs [meetdown.data/store-entity (speculate tx)]
        (is (= id (:db/id (create-entity @test-conn stored-event))))))))

(deftest test-get-events
  (testing "No events"
    (is (empty? (get-events (d/db @test-conn)))))
  (testing "event exists"
    (let [event-details {:db/id #db/id[:db.part/user -1] :event/name "an existing event"}
          db (:db-after ((speculate) @test-conn event-details))]
      (is (= (count (get-events db)) 1))
      (is (= (:event/name event-details) (:event/name (first (get-events db))) )))))

(deftest test-to-ent
  (testing "to entity"
    (let [tx (atom nil)]
      (with-redefs [meetdown.data/store-entity (fn [conn tx-data]
                                                 (reset! tx
                                                  ((speculate) conn tx-data)))]
        (let [another-event {:event/name "another event"}
              id (:db/id (create-entity @test-conn another-event))]
          (is (= (:event/name (to-ent (:db-after @tx) id))
                   (:event/name another-event))))))))

(deftest test-create-user
  (testing "Create a new user"
    (with-redefs [meetdown.data/store-entity (speculate)]
      (let [user {:user/email "test@gmail.com" :user/name "User Test"}
            new-user-id (:db/id (create-entity @test-conn user))]
        (is (not (zero? new-user-id))))))
  (testing "Create a new user and get it again"
    (with-redefs [meetdown.data/store-entity (speculate)]
      (let [user {:user/email "test@gmail.com" :user/name "User Test"}
            user-tx (create-entity @test-conn user)
            new-user-id (:db/id user-tx)
            db (:db user-tx)
            actual-user (to-ent db new-user-id)]
        (is (= (:db/id actual-user) new-user-id))
        (is (= (:user/email actual-user) (:user/email user)))
        (is (= (:user/name actual-user) (:user/name user)))))))
