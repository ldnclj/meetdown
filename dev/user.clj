(ns user
  (:require
   [clojure.java.javadoc :refer (javadoc)]
   [clojure.pprint :refer (pprint)]
   [clojure.reflect :refer (reflect)]
   [clojure.repl :refer (apropos dir doc find-doc pst source)]
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.stacktrace :refer (print-stack-trace)]
   [clojure.test :as test]
   [clojure.tools.namespace.repl :refer (refresh refresh-all)]
   [clojure.tools.trace :refer (trace-ns untrace-ns)]
   [meetdown.system :as system]))

(def system 
  "A Var containing an object representing the application under development."
  nil)

(defn init
  "Creates and initializes the system under development in the Var #'system."
  []
  (alter-var-root #'system
    (constantly (system/system))))

(defn start
  "Starts the system running, updates the Var #'system."
  []
  (alter-var-root #'system system/start))

(defn stop
  "Stops the system if it is currently running, updates the Var #'system."
  []
  (alter-var-root #'system
    (fn [s] (when s (system/stop s)))))

(defn go
  "Initializes and starts the system running."
  []
  (init)
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (refresh :after 'user/go))
