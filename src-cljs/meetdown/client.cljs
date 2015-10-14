(ns meetdown.client
    (:require-macros
      [cljs.core.async.macros :as asyncm :refer (go go-loop)])
    (:require
      ;;[sablono.core :as html :refer-macros [html]]
      [goog.events :as events]
      [goog.dom :as dom]
      [cljs.core.async :as async :refer (<! >! put! chan)]
      [taoensso.sente  :as sente :refer (cb-success?)] ; <--- Add this
      [taoensso.sente.packers.transit :as sente-transit]
      [cognitect.transit :as transit]))

(def packer (sente-transit/get-flexi-packer :edn))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk"
                                  {:type :auto
                                   })]                      ;:packer packer
     (def chsk       chsk)
     (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
     (def chsk-send! send-fn) ; ChannelSocket's send API fn
     (def chsk-state state)   ; Watchable, read-only atom
     )

(def r (transit/reader :json))

(defn main
  []
  (let [insertbutton  (dom/getElement "insertbutton")
        inserttext (dom/getElement "inserttext")
        insertresult (dom/getElement "insertresult")]

    (events/listen insertbutton "click"
                   (fn [_]
                     (let [txt (.-value inserttext)]
                       (chsk-send! [:meetdown/insert (transit/read r txt)]
                                   3000
                                   (fn [reply]
                                     (set! (.-innerHTML insertresult) reply))))))))

(main)