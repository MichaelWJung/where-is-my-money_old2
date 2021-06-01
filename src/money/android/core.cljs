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

(defn- use-default-db []
  (dispatch [:load-db generated-db])
  (dispatch [:toast "App db filled with generated defaults"]))

(defn- load-db [serialized]
  (dispatch [:load-db (cljs.reader/read-string serialized)])
  (dispatch [:toast "App db loaded from storage"]))

(defn ^:export -main [& args]
  (dispatch-sync [:initialize-db])
  (-> money.events/async-storage
      (.getItem "db")
      (.then #(if (nil? %)
                (use-default-db)
                (load-db %)))
      (.catch use-default-db)
      (.finally #(dispatch [:db-ready])))
  (r/as-element [app-root]))

; (defn init []
;   (dispatch-sync [:initialize-db {}])
;   (.registerComponent app-registry "WhereIsMyMoney" #(r/reactify-component app-root)))
