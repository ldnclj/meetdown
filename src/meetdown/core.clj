(ns meetdown.core
  (require [yoyo.core :as yc]
           [meetdown.data :as d]
           [meetdown.http :as h]
           [yoyo :as y]
           [yoyo.system :as ys]
           [cats.core :as c]))


(defn dev-config []
  (-> (fn []
        (ys/->dep
          (yc/->component {:dburi    "datomic:mem://meetdown"
                           :http-kit {}})))

      (ys/named :config)))


(defn datomic-connection-component []
  (-> (fn []
        (c/mlet [uri (ys/ask :config :dburi)]
                (ys/->dep
                  (let [conn (d/setup-and-connect-to-db uri)]
                    (yc/->component conn
                                    (fn []
                                      (d/close-db)))))))

      (ys/named :db-conn)))

(defn http-kit-component []
  (-> (fn []
        (c/mlet [http-kit-opts (ys/ask :config :http-kit)
                 db-conn (ys/ask :db-conn)]
                (ys/->dep
                  (let [stop-server! (h/start-server! (d/create-msg-handler db-conn) http-kit-opts)]
                    (yc/->component http-kit-opts
                                    (fn []
                                      (stop-server!)))))))
      (ys/named :http-kit)))



(defn create-dev-system []
  (y/set-system-fn!
    (fn []
      (ys/make-system #{(dev-config)
                        (datomic-connection-component)
                        (http-kit-component)}))))

;(y/start!)
;(y/stop!)