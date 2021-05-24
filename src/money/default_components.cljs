(ns money.default-components
  (:require [reagent.core :as r]
            ["react-native" :as ReactNative]
            ["@react-navigation/native" :refer [NavigationContainer]]
            ["@react-navigation/stack" :refer [createStackNavigator]]
            ; ["@react-native-community/datetimepicker" :as DateTimePicker]
            ))

; (def ReactNative (js/require "react-native"))

; (def text (r/adapt-react-class (.-Text ReactNative)))
; (def view (r/adapt-react-class (.-View ReactNative)))
; (def scroll-view (r/adapt-react-class (.-ScrollView ReactNative)))
; (def picker (r/adapt-react-class (.-Picker ReactNative)))
; (def picker-item (r/adapt-react-class (.. ReactNative -Picker -Item)))
; (def image (r/adapt-react-class (.-Image ReactNative)))
; (def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def touchable-native-feedback (r/adapt-react-class (.-TouchableNativeFeedback ReactNative)))

(def navigation-container (r/adapt-react-class NavigationContainer))
(prn navigation-container)
(def stack (createStackNavigator))
(def navigator (r/adapt-react-class (.-Navigator stack)))
(def screen (r/adapt-react-class (.-Screen stack)))

; (def date-time-picker (r/adapt-react-class (aget DateTimePicker "default")))

; (defn alert [title]
;       (.alert (.-Alert ReactNative) title))
