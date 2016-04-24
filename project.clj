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
                 [jarohen/nomad "0.8.0-beta3" :exclusions [org.clojure/clojure prismatic/schema]]
                 [com.taoensso/timbre "4.2.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [clj-jwt "0.1.1"]]
  :main meetdown.core
  :source-paths ["src"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "resources/private/js"]
  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-1"]
            [lein-cloverage "1.0.6"]
            [lein-kibit "0.1.2"]]
  ;;  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds {:app
                       {:source-paths ["src-cljs"]
                        :compiler {:main         "meetdown.cljscore"
                                   :output-to    "resources/public/js/compiled/meetdown.js"
                                   :output-dir   "resources/public/js/compiled/out"
                                   :asset-path   "js/compiled/out"}}

                       :test
                       {:source-paths ["src-cljs" "test-cljs"]
                        :figwheel true
                        :compiler {:output-to "resources/private/js/unit-test.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}}

              ;; CHJ- had real trouble using cljsbuild with cljs.test/async tests so gave up and used 'doo' which just worked out of the box.
              ;;              :test-commands
              ;;              {"unit" ["phantomjs" "phantom/unit-test.js" "phantom/unit-test.html"]}
              }
  :profiles {:uberjar {:aot :all
                       :prep-tasks ["compile" ["cljsbuild" "once" "app"]]}
             :dev {:source-paths ["dev" "src-cljs" "test" "test-cljs"]
                   :dependencies [[ring/ring-mock "0.3.0"]
                                  [lein-doo "0.1.6"]
                                  [org.clojure/tools.namespace "0.2.3"]
                                  [org.clojure/java.classpath "0.2.0"]
                                  [figwheel-sidecar "0.5.0-1"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/test.check "0.9.0"]]
                   :plugins [[lein-doo "0.1.6"]]}
             :repl {:plugins [[cider/cider-nrepl "0.10.0"]]}}
  :aliases {"test-all" ["do" "test" ["doo" "phantom" "test" "once"]]
            "run-all"  ["do" "clean" ["cljsbuild" "once"] "run"]}
  :repl-options {:init-ns user
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]})
