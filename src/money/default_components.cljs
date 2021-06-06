(ns money.default-components
  (:require [reagent.core :as r]
            [react-native :as rn]
            ["react-native-paper" :as rnp]
            ["@react-navigation/native" :refer [NavigationContainer]]
            ["@react-navigation/stack" :refer [createStackNavigator]]
            ["@react-native-community/datetimepicker" :as DateTimePicker]))

(def pressable (r/adapt-react-class rn/Pressable))

(def navigation-container (r/adapt-react-class NavigationContainer))
(def stack (createStackNavigator))
(def navigator (r/adapt-react-class (.-Navigator stack)))
(def screen (r/adapt-react-class (.-Screen stack)))

(def date-time-picker (r/adapt-react-class (aget DateTimePicker "default")))

(def button (r/adapt-react-class rnp/Button))
(def fab (r/adapt-react-class rnp/FAB))
(def paper-provider (r/adapt-react-class rnp/Provider))
(def portal (r/adapt-react-class rnp/Portal))
(def portal-host (r/adapt-react-class (.-Host rnp/Portal)))
(def text-input (r/adapt-react-class rnp/TextInput))

; (defn alert [title]
;       (.alert (.-Alert ReactNative) title))
