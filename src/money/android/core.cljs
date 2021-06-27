(ns money.android.core
  (:require ["react-native-gesture-handler"]
            [cljs.reader]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [money.components.app-root :refer [root]]
            [money.db :refer [generated-db]]
            [money.events]
            [money.subs]
            ["react-native" :refer [AppRegistry]]))

(defn app-root []
  [root])

(defn- use-default-db []
  (dispatch [:load-db generated-db])
  (dispatch [:toast "App db filled with generated defaults"]))

(defn- load-db [serialized]
  (dispatch [:load-db (cljs.reader/read-string serialized)])
  (dispatch [:toast "App db loaded from storage"]))

(defonce component-to-update (atom nil))

(def updatable-app-root
  (with-meta app-root
    {:component-did-mount
     (fn [] (this-as ^js this
                     (reset! component-to-update this)))}))

(defn reload {:dev/after-load true} []
  (.forceUpdate ^js @component-to-update))

(defn init []
  (dispatch-sync [:initialize-db {}])
  (-> money.events/async-storage
      (.getItem "db")
      (.then #(if (nil? %)
                (use-default-db)
                (load-db %)))
      (.catch use-default-db)
      (.finally #(dispatch [:db-ready])))
  (.registerComponent AppRegistry "WhereIsMyMoney" #(r/reactify-component updatable-app-root)))
