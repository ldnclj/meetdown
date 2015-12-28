(ns user
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer [refresh]]
            [meetdown.core :as app]))

(def system nil)

(defn init []
  (alter-var-root #'system
                  (constantly (app/meetdown-system {:dburi "datomic:mem://meetdown"
                                                    :server {:port 3000}}))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))
