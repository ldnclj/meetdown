(ns meetdown.login.rest
  (:require [cljs-http.client :as http]
            [petrol.core :as petrol]
            [meetdown.login.messages :as m]
            [meetdown.utils :refer [add-ns]]
            [cljs.core.async :refer [>! <! chan]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn login
  [login]
  (petrol/wrap m/map->LoginResults
               (http/post "/q"
                          {:with-credentials? false
                           :edn-params {:type :login
                                        :username (:username login)
                                        :password (:password login)}})))


