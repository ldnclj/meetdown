(ns meetdown.data
  (require [datomic.api :only [q db] :as d]))

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
  (let [tx @(d/transact con [(assoc tx-data :db/id #db/id[:db.part/user -1000001])])]
    (d/resolve-tempid (d/db con) (:tempids tx)
                      (d/tempid :db.part/user -1000001))))

(defn to-ent "Takes a db id and connection and returns the entity"
  [conn id] (-> conn d/db (d/entity id)))

(defmulti msg-handler :id) ; Dispatch on event-id


(defn create-msg-handler [dbconn]
  (fn [{:as ev-msg :keys [?reply-fn]}]
    (println (str "Message: " ev-msg))
    (try
      (msg-handler ev-msg dbconn)
      (catch Exception e
        (do (println (str "Caught: " e))
            (when ?reply-fn (?reply-fn (.getMessage e))))))))

(do
  (defmethod msg-handler :default ; Fallback
    [{:keys [event ?reply-fn]} _]
    (do
      (println "Unhandled event: %s" event)
      (when ?reply-fn
        (?reply-fn {:uhandled event}))))

  (defmethod msg-handler :meetdown/insert
    [{:keys [?reply-fn ?data]} dbconn]
    (println "Insert: %s" ?data)
    (let [db-id (create-entity dbconn ?data)
          inserted (d/touch (to-ent dbconn db-id))]
      (when ?reply-fn
        (?reply-fn inserted)))))
