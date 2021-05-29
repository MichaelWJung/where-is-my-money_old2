(ns money.android.core
  (:require ["react-native-gesture-handler"]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch-sync]]
            [money.components.app-root :refer [root]]
            [money.events]
            [money.subs]))

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
