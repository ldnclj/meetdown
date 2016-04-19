(ns user
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer [refresh]]
            [meetdown.core :as app]
            [figwheel-sidecar.repl-api :as ra]))

(def server-port-num 8000)

(def system nil)

(defn init []
  (alter-var-root #'system
                  (constantly (app/meetdown-system {:dburi "datomic:mem://meetdown"
                                                    :server {:port server-port-num}}))))

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

;; As figwheel doesn't seem to properly merge cljsbuild config from profiles
;; we have to define our figwheel config here and use some helper fns to start
;; and stop figwheel.

(def figwheel-config
  {:figwheel-options {}
   :build-ids ["dev"]
   :all-builds
   [{:id "dev"
     :source-paths ["src-cljs" "test-cljs"]
     :figwheel {:on-jsload    "meetdown.cljscore/reload-hook"}
     :compiler {:main         "meetdown.cljscore"
                :output-to    "resources/public/js/compiled/meetdown.js"
                :output-dir   "resources/public/js/compiled/out"
                :asset-path   "js/compiled/out"
                :source-map-timestamp true
                :closure-defines {'meetdown.rest/url-port server-port-num} ;; <-- define port for client to match server above
                }}]})


(defn start-figwheel! [] (do (ra/start-figwheel! figwheel-config) nil))

(defn stop-figwheel! [] (do (ra/stop-figwheel!) nil))

(defn cljs-repl [] (ra/cljs-repl))
