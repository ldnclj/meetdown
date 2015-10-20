(ns meetdown.http
  (:require [org.httpkit.server :as http]
            [yoyo.core :as yc]
            [cats.core :as c]
            [yoyo.system :as ys]
            [yoyo.http-kit :as http-kit]
            [compojure.core :refer [routes GET POST DELETE ANY context]]
            [compojure.route :refer [files]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.defaults :as rmd]
            [meetdown.data :as data]))

(defn m-handler []
  (c/mlet [db-conn (ys/ask :db-conn)]
    (ys/->dep

     (-> (routes
          (files "/")
          (POST "/q" []
                (fn [{req-body :body-params}]
                  {:body (case (:type req-body)
                           :get-events (data/get-events db-conn)) })))

         (wrap-restful-format :formats [:edn :transit-json])
         (rmd/wrap-defaults (-> rmd/site-defaults
                                (assoc-in [:security :anti-forgery] false)))))))

(defn m-server []
  (-> (fn []
        (c/mlet [handler (m-handler)
                 http-kit-opts (ys/ask :config :http-kit)]
          (ys/->dep
           (http-kit/start-server! {:handler handler
                                    :server-opts http-kit-opts}))))
      (ys/named :web-server)))
