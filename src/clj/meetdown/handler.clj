(ns meetdown.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [api-defaults wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]))

(def home-page
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css")]
    [:body
     [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]]
     (include-js "js/app.js")]]))

(defroutes routes
  (GET "/" [] home-page)
  (POST "/q" request
    (do (println "Request received:" (:params request))
        (case (:type (:params request))
          "create-event" {:body (assoc (:event (:params request)) :id 1234)}
          (println "Unknown event type" (:type (:params request))))))
  (resources "/")
  (not-found "My Not Found"))

(def app
  (let [handler (->  #'routes (wrap-defaults api-defaults) wrap-json-params wrap-json-response)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))
