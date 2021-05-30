(ns money.android.core
  (:require ["react-native-gesture-handler"]
            [cljs.reader]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [money.components.app-root :refer [root]]
            [money.db :refer [generated-db]]
            [money.events]
            [money.subs]))

; (def ReactNative (js/require "react-native"))
; (def app-registry (.-AppRegistry ReactNative))

(defn app-root []
  [root])

(defn ^:export -main [& args]
  (dispatch-sync [:initialize-db])
  (-> money.events/async-storage
      (.getItem "db")
      (.then #(dispatch [:load-db (cljs.reader/read-string %)]))
      (.catch #(prn %))
      (.finally #(dispatch [:db-ready])))
  (r/as-element [app-root]))

; (defn init []
;   (dispatch-sync [:initialize-db {}])
;   (.registerComponent app-registry "WhereIsMyMoney" #(r/reactify-component app-root)))
