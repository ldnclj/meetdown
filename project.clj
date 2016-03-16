(defproject meetdown "0.1.0-SNAPSHOT"
  :description "An Event Management system"
  :url "http://www.londonclojurians.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.datomic/datomic-free "0.9.5302" :exclusions [joda-time]]
                 [http-kit "2.1.19"]
                 [ring-middleware-format "0.7.0"]
                 [ring/ring-defaults "0.1.5"]
                 [compojure "1.3.4"]
                 [reagent "0.5.1"]
                 [cljs-http "0.1.38"]
                 [org.clojure/clojurescript "1.7.170" :exclusions [org.apache.ant/ant]]
                 [org.clojure/core.async "0.2.374"]
                 [petrol "0.1.2"]
                 [hiccup "1.0.5"]
                 [com.taoensso/timbre "4.2.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]]
  :main meetdown.core
  :source-paths ["src"]
  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-1"]
            [lein-cloverage "1.0.6"]
            [lein-kibit "0.1.2"]
            [refactor-nrepl "2.2.0-SNAPSHOT"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src-cljs" "test-cljs"]
                        :figwheel {:on-jsload    "meetdown.cljscore/reload-hook"}
                        :compiler {:main         meetdown.cljscore
                                   :output-to    "resources/public/js/compiled/meetdown.js"
                                   :output-dir   "resources/public/js/compiled/out"
                                   :asset-path   "js/compiled/out"
                                   :source-map-timestamp true}}

                       {:id "test"
                        :source-paths ["src-cljs" "test-cljs"]
                        :figwheel true
                        :compiler {:output-to "resources/private/js/unit-test.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]
  :test-commands
  {"unit" ["phantomjs" "phantom/unit-test.js" "phantom/unit-test.html"]}}
  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev" "src-cljs" "test" "test-cljs"]
                   :dependencies [[ring/ring-mock "0.3.0"]
                                  [lein-doo "0.1.6"]
                                  [org.clojure/tools.namespace "0.2.3"]
                                  [org.clojure/java.classpath "0.2.0"]
                                  [figwheel-sidecar "0.5.0-1"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :plugins [[lein-doo "0.1.6"]]}
             :repl {:plugins [[cider/cider-nrepl "0.10.0"]]}}
  :repl-options {:init-ns user
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]})
