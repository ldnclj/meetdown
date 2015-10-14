(ns meetdown.client
    (:require-macros
      [cljs.core.async.macros :as asyncm :refer (go go-loop)])
    (:require
      [goog.events :as events]
      [goog.dom :as dom]
      [cljs.core.async :as async :refer (<! >! put! chan)]
      [taoensso.sente  :as sente :refer (cb-success?)]
      [taoensso.sente.packers.transit :as sente-transit]
      [cognitect.transit :as transit]))

(def packer (sente-transit/get-flexi-packer :edn))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" {:type :auto :packer packer})]                      ;
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  )

(def r (transit/reader :json))

(defn db-function [type source target]
  (fn [_]
    (let [txt (.-value (dom/getElement source))]
      (chsk-send! [type (transit/read r txt)]
                  3000
                  (fn [reply]
                    (set! (.-innerHTML (dom/getElement target)) reply))))))

(defn main
  []
  (do
    (events/listen (dom/getElement "insertbutton1") "click" (db-function :meetdown/insert "inserttext1" "insertresult1"))
    (events/listen (dom/getElement "insertbutton2") "click" (db-function :meetdown/insert "inserttext2" "insertresult2"))
    (events/listen (dom/getElement "insertbutton3") "click" (db-function :meetdown/insert "inserttext3" "insertresult3"))
    (events/listen (dom/getElement "querybutton") "click" (db-function :meetdown/query "querytext" "queryresult"))))

(main)