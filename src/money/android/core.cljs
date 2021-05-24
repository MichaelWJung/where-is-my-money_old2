(ns money.android.core
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch-sync]]
            [money.components.app-root :refer [root]]
            [money.events]
            [money.subs]))

(def ReactNativeGestureHandler (js/require "react-native-gesture-handler"))
; (def ReactNative (js/require "react-native"))
; (def app-registry (.-AppRegistry ReactNative))

(defn app-root []
  [root])

(defn ^:export -main [& args]
  (dispatch-sync [:initialize-db {}])
  (r/as-element [app-root]))

; (defn init []
;   (dispatch-sync [:initialize-db {}])
;   (.registerComponent app-registry "WhereIsMyMoney" #(r/reactify-component app-root)))
