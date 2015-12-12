(ns meetdown.data
  (:require [datomic.api :only [q db] :as d]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn install-base-schema [conn]
  @(d/transact
     conn (read-string (slurp "resources/schema.edn"))))

(defn setup-and-connect-to-db [uri]
  (do (d/create-database uri)
      (let [conn (d/connect uri)]
        (install-base-schema conn)
        conn)))

(defn close-db []
  (d/shutdown false))

(defn create-entity
  "Takes transaction data and returns the resolved tempid"
  [con tx-data]
  (let [had-id (contains? tx-data ":db/id")
        data-with-id (if had-id
                       tx-data
                       (assoc tx-data :db/id #db/id[:db.part/user -1000001]))
        tx @(d/transact con [data-with-id])]
    (if had-id (tx-data ":db/id")
               (d/resolve-tempid (d/db con) (:tempids tx)
                                 (d/tempid :db.part/user -1000001)))))

(defn to-ent "Takes a db id and connection and returns the entity"
  [conn id] (-> conn d/db (d/entity id)))

(defn get-events [db-conn]
  (let [db (d/db db-conn)]
    (d/pull-many db [:*]
                 (->> (d/q '{:find [?event-id]
                             :where [[?event-id :event/name]]}
                           db)
                      (map first)))))

(defn start-database!
  [{:keys [db-conn] :as system}]
  (when (not db-conn)
    (info "Starting database")
    (let [uri (get-in system [:config :dburi])]
      (assoc system :db-conn (setup-and-connect-to-db uri)))))

(defn stop-database!
  [system]
  (when (system :db-conn)
    (info "Stopping database")
    (close-db)
    (assoc system :db-conn nil)))



(comment
  (get-events (:db-conn user/foo-system))

  (create-entity (:db-conn user/foo-system) {:event/name "New event"})



  (d/transact (:db-conn user/foo-system) [{:db/id #db/id[:db.part/user]
                                           :event/name "ProCloDo Dojo 20 Novemnber 2015"}])

  )
