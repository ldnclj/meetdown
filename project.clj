(defproject meetdown "0.1.0-SNAPSHOT"
  :description "An Event Management system"
  :url "http://www.londonclojurians.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.datomic/datomic-free "0.9.5302" :exclusions [joda-time]]
                 [http-kit "2.1.19"]
                 [ring-server "0.4.0"]
                 [ring "1.4.0"]
                 [ring-middleware-format "0.7.0"]
                 [ring/ring-defaults "0.1.5"]
                 [hiccup "1.0.5"]
                 [com.cognitect/transit-clj "0.8.283"]
                 [ring/ring-json "0.3.1"]
                 [org.clojure/clojurescript "1.7.170" :exclusions [org.apache.ant/ant]]
                 [sablono "0.3.4"]
                 [com.taoensso/timbre "4.2.0"]
                 [cljs-http "0.1.37"]
                 [reagent "0.5.0"]
                 [reagent-utils "0.1.5"]
                 [prone "0.8.2"]
                 [compojure "1.4.0"]
                 [environ "1.0.0"]
                 [secretary "1.2.3"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [cljs-ajax "0.5.1"]]
  :main meetdown.core
  :target-path "target/%s"
  :clean-targets ^{:protect false} ["resources/public/js" :target]
  :source-paths ["src/clj"]
  :plugins [[lein-environ "1.0.0"]
            [lein-cljsbuild "1.1.2"]
            [lein-figwheel "0.5.0-1"]]
  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :figwheel true
                             :compiler {:main         meetdown.load
                                        :output-to    "resources/public/js/app.js"
                                        :output-dir   "resources/public/js/out"
                                        :asset-path   "js/out"}}}}
  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev" "src/cljs"]
                   :dependencies [[ring/ring-mock "0.3.0"]
                                  [org.clojure/tools.namespace "0.2.3"]
                                  [org.clojure/java.classpath "0.2.0"]
                                  [figwheel-sidecar "0.5.0-1"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :cljsbuild {:builds {:app {:figwheel true}}}}}
  :repl-options {:init-ns user
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]})
