(ns money.default-components
  (:require [reagent.core :as r]
            ["react-native" :as ReactNative]
            ["@react-navigation/native" :refer [NavigationContainer]]
            ["@react-navigation/stack" :refer [createStackNavigator]]
            ["@react-native-community/datetimepicker" :as DateTimePicker]))

; (def touchable-native-feedback (r/adapt-react-class (.-TouchableNativeFeedback ReactNative)))

(def navigation-container (r/adapt-react-class NavigationContainer))
(def stack (createStackNavigator))
(def navigator (r/adapt-react-class (.-Navigator stack)))
(def screen (r/adapt-react-class (.-Screen stack)))

(def date-time-picker (r/adapt-react-class (aget DateTimePicker "default")))

(defn alert [title]
      (.alert (.-Alert ReactNative) title))
