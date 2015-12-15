(ns meetdown.data
  (require [datomic.api :only [q db] :as d]))

(defn database [db-conn] (d/db db-conn))

(defn install-base-schema [conn]
  @(d/transact
     conn (read-string (slurp "resources/schema.edn"))))

(defn setup-and-connect-to-db [uri]
  (do (d/create-database uri)
      (let [conn (d/connect uri)]
        (install-base-schema conn)
        conn)))

(defn close-db []
  (do (d/shutdown false)
      (println "DB shut down")))

(defn create-entity
  "Takes transaction data and returns the resolved tempid"
  [conn tx-data]
  (let [had-id (contains? tx-data ":db/id")
        data-with-id (if had-id
                       tx-data
                       (assoc tx-data :db/id #db/id[:db.part/user -1000001]))
        tx @(d/transact conn [data-with-id])]
    (if had-id (tx-data ":db/id")
               (d/resolve-tempid (d/db conn) (:tempids tx)
                                 (d/tempid :db.part/user -1000001)))))

(defn to-ent "Takes a db id and connection and returns the entity"
  [db id] (when-let [entity (d/entity db id)] (d/touch entity)))

(defn get-events [db]
  (d/pull-many db [:*]
               (->> (d/q '{:find [?event-id]
                           :where [[?event-id :event/name]]}
                         db)
                    (map first))))


(comment

  (database (get user/system :dbconn))
  (get-events (database (get-in user/system [:dbconn])))

  (let [dbconn (:dbconn user/system)
        id     (create-entity dbconn {:event/name "Newest event"})
        db (database dbconn)]
    (d/touch (d/entity db id)))

  (let [dbconn (:dbconn user/system)
        id (create-entity dbconn {:event/name "Newest event"})
        db (database dbconn)]
    (to-ent db id))



  (d/transact (:db-conn meetdown.user/system) [{:db/id #db/id[:db.part/user]
                                           :event/name "ProCloDo Dojo 20 Novemnber 2015"}])

  )
