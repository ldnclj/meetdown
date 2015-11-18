(defproject ldnclj-datomic "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.4.0"]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.datomic/datomic-free "0.9.5302" :exclusions [joda-time]]
                 [com.taoensso/sente "1.6.0"]

                 [jarohen/yoyo "0.0.6-beta2"]
                 [jarohen/yoyo.http-kit "0.0.5-beta2"]
                 [http-kit "2.1.19"]
                 [ring/ring-core "1.3.2"]
                 [compojure "1.3.4"]
                 [hiccup "1.0.5"]
                 [com.cognitect/transit-clj "0.8.283"]
                 [ring-middleware-format "0.6.0"]
                 [ring/ring-defaults "0.1.5"]

                 [com.cognitect/transit-cljs "0.8.225"]
                 [org.clojure/clojurescript "1.7.48"]
                 [sablono "0.3.4"]
                 [cljs-http "0.1.37"]]
  :main meetdown.core
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
                                   :verbose true}}]}
  :profiles {:dev
             {:figwheel
              {:nrepl-port 7888}}})
