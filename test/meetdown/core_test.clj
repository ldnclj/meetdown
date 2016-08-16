(ns meetdown.core-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [meetdown.core :refer :all]))

(deftest test-new-database
  (let [dburi "datomic:mem://new"]
    (is (= (:dburi (new-database dburi)) dburi))
    (is (not (nil? (:connection (component/start (new-database dburi))))))))

(deftest test-meetdown-system
  (let [config {:dburi "datomic:mem://new" :server {:port 4000}}
        system (component/start (meetdown-system config))]
    (is (not (nil? (:app system))))
    (is (= (:server config) (get-in system [:app :server-options])))
    (is (not (nil? (:db-component system))))
    (is (= (:dburi config) (get-in system [:db-component :dburi])))
    (component/stop system)))

(deftest test-main-returns-system-map
  (let [system (-main)]
    (is (not (nil? system)))
    (is (not (nil? (:db-component system))))
    (is (not (nil? (:app system))))
    (component/stop system)))

(comment
  (def system (atom (component/start (meetdown-system {:dburi "datomic:mem://new" :server {:port 4000}}))))

  (get-in @system [:app :server-options])
  (:app @system)

  (swap! system component/stop)
  (component/stop (-main))

  )
