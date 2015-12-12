(ns meetdown.core
  (:require [meetdown.system :refer [system start]]))

(defn -main []
  (start (system)))

(comment
  ;; Start system by running (-main). Use (user/reset) to reload.
  ;; To create data run transact in comment in data.clj
  ;; To fetch events -
  ;;    curl -X POST -d "{:type :get-events}" http://localhost:3000/q --header "Content-Type:application/edn"


  ;; curl -X POST -d "{:type :create-event :txn-data {:event/name \"New event-2\"}}" http://localhost:3000/q --header "Content-Type:application/edn"

  ;;(data/create-entity db-conn (:txn-data req-body)
  ;;{:type :create-event, :txn-data {:event/name "New event-2"}}

  )
