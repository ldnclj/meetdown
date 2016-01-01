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
                 [ring/ring-mock "0.2.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [sablono "0.3.4"]
                 [com.taoensso/timbre "4.2.0"]
                 [cljs-http "0.1.37"]
                 [cljsjs/react "0.13.3-1"]
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
  :source-paths ["src/clj" "src/cljs"]
  :plugins [[lein-environ "1.0.0"]
            [lein-asset-minifier "0.2.2"]]
  :minify-assets
  {:assets
   {"resources/public/css/site.min.css" "resources/public/css/site.css"}}
  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to    "resources/public/js/app.js"
                                        :output-dir   "resources/public/js/out"
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}
  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev" "env/dev/clj"]
                   :dependencies [[org.clojure/tools.namespace "0.2.3"]
                                  [org.clojure/java.classpath "0.2.0"]
                                  [matcha "0.1.0"]
                                  [ring/ring-mock "0.2.0"]
                                  [ring/ring-devel "1.4.0"]
                                  [lein-figwheel "0.5.0-1"]]
                   :plugins [[lein-figwheel "0.5.0-1"]
                             [lein-cljsbuild "1.1.1"]]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :nrepl-port 7888
                              :css-dirs ["resources/public/css"]}

                   :env {:dev true}
                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs" "src/cljs"]
                                              :compiler {:main "meetdown.dev"
                                                         :source-map true}}}}}}
  :repl-options {:init-ns user})
