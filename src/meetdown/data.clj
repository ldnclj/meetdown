(ns meetdown.data
  (:require [datomic.api :only [q db] :as d]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

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

(defn- store-entity
  [conn data-with-id]
  @(d/transact conn [data-with-id]))

(defn- resolve-temp-id
  [db-after tx]
  (d/resolve-tempid db-after (:tempids tx)
                    (d/tempid :db.part/user -1000001)))

(defn create-entity
  "Takes transaction data and returns the resolved tempid and the database after the txn."
  [conn tx-data]
  (let [had-id (contains? tx-data :db/id)
        data-with-id (if had-id
                       tx-data
                       (assoc tx-data :db/id #db/id[:db.part/user -1000001]))
        tx (store-entity conn data-with-id)
        db-after (:db-after tx)]
    (if had-id
      {:db/id (tx-data :db/id) :db db-after}
      {:db/id (resolve-temp-id  db-after tx) :db db-after})))

(defn to-ent "Takes a db id and connection and returns the entity"
  [db id] (when-let [entity (d/entity db id)] (d/touch entity)))

(defn get-events [db]
  (d/pull-many db [:*]
               (map
                first
                (d/q '{:find [?event-id]
                           :where [[?event-id :event/name]]}
                         db))))


(comment

  ;; grab schema
  (defn get-user-schema [conn]
    (d/q '[:find ?id
           :where [?e :db/ident ?id]
           [_ :db.install/attribute ?e]
           [?e :db.install/partition :db.part/db]]
         (d/db conn)))

  (get-user-schema (get-in user/system [:db-component :connection]))


  (database (get-in user/system [:db-component :connection]))
  (get-events (database (get-in user/system [:db-component :connection])))
  (create-entity (get-in user/system [:db-component :connection]) {:event/name "Newest event"})
  (to-ent (database (get-in user/system [:db-component :connection])) 17592186045418)

  (let [dbconn (get-in user/system [:db-component :connection]) (:dbconn user/system)
        id     (create-entity dbconn {:event/name "Newest event"})
        db     (database dbconn)]
    (d/touch (d/entity db id)))

  (let [dbconn (get-in user/system [:db-component :connection])
        id (create-entity dbconn {:event/name "Newest event"})
        db (database dbconn)]
    (to-ent db id))



  (d/transact (get-in user/system [:db-component :connection])
              [{:db/id #db/id[:db.part/user]
                :event/name "ProCloDo Dojo 20 Novemnber 2015"}])

  (get-events (get-in user/system [:db-component :connection]))

  (create-entity (-> user/system :db-component :connection) {:event/name "Newest event"})



  (d/transact (-> user/system :db-component :connection) [{:db/id #db/id[:db.part/user]
                                                           :event/name "ProCloDo Dojo 20 Novemnber 2015"}])


  )
