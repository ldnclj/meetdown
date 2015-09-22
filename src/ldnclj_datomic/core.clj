(ns ldnclj-datomic.core
  (require [datomic.api :only [q db] :as d]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn install-base-schema [conn]
  @(d/transact
     conn (read-string (slurp "resources/schema.edn"))))

(defn go-db [uri]
  (do (d/create-database uri)
      (let [conn (d/connect uri)]
        (install-base-schema conn)
        conn)))

(defn create-entity
  "Takes transaction data and returns the resolved tempid"
  [con tx-data]
  (let [tx @(d/transact con [(assoc tx-data :db/id #db/id[:db.part/user -1000001])])]
    (d/resolve-tempid (d/db con) (:tempids tx)
                      (d/tempid :db.part/user -1000001))))

(defn to-ent "Takes a db id and connection and returns the entity"
  [conn id] (-> conn d/db (d/entity id)))

(defn single-entity
  "Takes a query and returns an entity"
  [con query & args]
  (d/touch (to-ent con
                   (ffirst (apply d/q query (d/db con)
                                  args)))))

(def user-by-email
  '[:find ?c
    :in $ ?em
    :where
    [?c :user/email ?em]])

(defn show-schema [conn]
  (let [db (d/db conn)]
    (clojure.pprint/pprint
      (map #(->> % first (d/entity db) d/touch)
           (d/q '[:find ?v
                  :where [_ :db.install/attribute ?v]]
                db)))))

(defn insert-and-retrieve-user [conn name surname email]
  (do (create-entity conn {:user/firstname name :user/surname surname :user/email email})
      (d/touch (single-entity conn user-by-email email))))