(ns meetdown.http
  (:require [org.httpkit.server :as http]
            [yoyo.core :as yc]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [compojure.core :refer [routes GET POST DELETE ANY context]]
            [compojure.route :refer [files]]
            [taoensso.sente.packers.transit :as sente-transit]
            [ring.middleware.keyword-params]
            [ring.middleware.params]))

(def packer (sente-transit/get-flexi-packer :edn))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {})] ;{:packer packer}
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
   )

(defn make-routes []
  (routes
    (files "/")
    (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
    (POST "/chsk" req (ring-ajax-post                req))))


(defn start-server!
  [db-func server-opts]
  (let [routes-function (make-routes)
        server (http/run-server (-> routes-function
                                    ring.middleware.keyword-params/wrap-keyword-params
                                    ring.middleware.params/wrap-params)
                                server-opts)]
    (do
      (sente/start-chsk-router! ch-chsk db-func)
      server)))

