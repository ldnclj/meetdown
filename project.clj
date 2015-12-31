(defproject meetdown "0.1.0-SNAPSHOT"
  :description "An Event Management system"
  :url "http://www.londonclojurians.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.datomic/datomic-free "0.9.5302" :exclusions [joda-time]]
                 [http-kit "2.1.19"]
                 [ring/ring-core "1.3.2"]
                 [ring-middleware-format "0.7.0"]
                 [ring/ring-defaults "0.1.5"]
                 [compojure "1.3.4"]
                                  [hiccup "1.0.5"]
                 [com.cognitect/transit-clj "0.8.283"]
                 [ring-middleware-format "0.6.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-mock "0.2.0"]
                 [com.cognitect/transit-cljs "0.8.225"]
                 [org.clojure/clojurescript "1.7.48"]
                 [sablono "0.3.4"]
                 [cljs-http "0.1.37"]]
  :main meetdown.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev"]
                   :figwheel     {:nrepl-port 7888}
                   :dependencies [[org.clojure/tools.namespace "0.2.3"]
                                  [org.clojure/java.classpath "0.2.0"]
                                  [matcha "0.1.0"]]}
                   :repl-options {:init-ns user}}
  :cljsbuild {
              :builds [{
                        ; The path to the top-level ClojureScript source directory:
                        :source-paths ["src-cljs"]
                        ; The standard ClojureScript compiler options:
                        ; (See the ClojureScript compiler documentation for details.)
                        :figwheel :true
                        :compiler {
                                   :output-to "public/main.js"  ; default: target/cljsbuild-main.js
                                   :output-dir "public/out"
                                   :optimizations :none
                                   :pretty-print true
                                   :verbose true}}]})
