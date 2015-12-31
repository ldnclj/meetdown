(ns meetdown.system
  (:require [meetdown.data :as data]
            [meetdown.http :as http]))

(defn system
  "Returns a new instance of the whole application."
  ([]
   {:config {:dburi    "datomic:mem://meetdown"
             :http-kit {:port 3000}}}))

(defn start
  "Performs side effects to initialize the system, acquire resources, and start it running. Returns an updated instance of the system."
  ([system]
   (-> system
       (data/start-database!)
       (http/start-http-server!))))

(defn stop
  "Performs side effects to shut down the system and release its resources. Returns an updated instance of the system."
  ([system]
   (-> system
       (http/stop-http-server!)
       (data/stop-database!))))
