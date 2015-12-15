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
                 [reagent "0.5.1"]
                 [cljs-http "0.1.38"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"]
                 [petrol "0.1.0"]
                 [hiccup "1.0.5"]
                 [ring-cors "0.1.7"]]
  :main meetdown.core
  :source-paths ["src/clj" "src/cljs"]
  :target-path "target/%s"
  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-1"]]
  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :figwheel {:on-jsload "meetdown.corecljs/reload-hook"}
                             :compiler {:main         meetdown.corecljs
                                        :output-to    "resources/public/js/app.js"
                                        :output-dir   "resources/public/js/out"
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}
  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.3"]
                                  [org.clojure/java.classpath "0.2.0"]]
                   :repl-options {:init-ns user}}})
