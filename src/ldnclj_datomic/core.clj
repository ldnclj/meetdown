(ns ldnclj-datomic.core
  (require [datomic.api :only [q db] :as d]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))




(defn go-db [uri]
  (do (d/create-database uri)
      (let [conn (d/connect uri)]
        ;;(install-base-schema conn)
        conn)))