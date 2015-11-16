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
                           :http-kit {:port 3000}})))

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

(defn make-system []
  (-> (ys/make-system #{(dev-config)
                        (datomic-connection-component)
                        (h/m-server)})

      (yc/with-system-put-to 'user/foo-system)))

(defn -main []
  (y/set-system-fn! 'meetdown.core/make-system)

  (y/start!))

(comment
  ;; Start system by running (-main). Use (y/reload!) to reload.
  ;; To create data run transact in comment in data.clj
  ;; To fetch events -
  ;;    curl -X POST -d "{:type :get-events}" http://localhost:3000/q --header "Content-Type:application/edn"



  )
