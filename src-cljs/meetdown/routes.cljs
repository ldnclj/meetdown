(ns meetdown.routes
  (:require [petrol.core :refer [Message process-message]]
            [petrol.routing :refer [UrlHistoryEvent]]))

(def frontend-routes
  ["" {"#" {""            :home
            "login"       :login
            "logout"      :logout
            "newevent"    :new-event
            [:id "-event"] :event
            "test"        :test}}])

(extend-protocol Message
  UrlHistoryEvent
  (process-message [{view :view} app]
    (assoc app :view view)))

(defn href-for
  ([handler]
   (href-for handler {}))
  ([handler args]
   (petrol.routing/href-for frontend-routes handler args)))
