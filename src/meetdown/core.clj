(ns meetdown.core
  (:require [meetdown.data :as data]
            [meetdown.system :refer [system start]]))

(defn -main []
  (start (system)))

(comment
  ;; Start your REPL and switch to user ns
  (ns user)

  ;; Refresh everything
  (refresh-all)

  ;; Start the system
  (reset)

  ;; To reset and get a clean system again, call reset once more. The server will be shutdown and a new one started
  (reset)

  ;; Var user/system contains the current system
  (pprint user/system)

  ;; To create an event:
  ;;     curl -X POST -d "{:type :create-event :txn-data {:event/name \"test-event-name\"}}" http://localhost:3000/q --header "Content-Type:application/edn"
  ;; Or:
  (data/create-entity (user/system :db-conn) {:event/name "test-event"})

  ;; To get events:
  ;;     curl -X POST -d "{:type :get-events}" http://localhost:3000/q --header "Content-Type:application/edn"
  ;; Or:
  (data/get-events (user/system :db-conn))
  ;;(data/create-entity db-conn (:txn-data req-body)
  ;;{:type :create-event, :txn-data {:event/name "New event-2"}}
  )
