(ns where-is-my-money.views
  (:require [clojure.walk]
            [reagent.core :as r]
            [reagent.react-native :as rn]
            [re-frame.core :as rf :refer [dispatch subscribe]]))

(def ReactNative (js/require "react-native"))
(def ReactNativeVectorIcons (js/require "react-native-vector-icons/dist/FontAwesome"))
(def paper (js/require "react-native-paper"))

(def paper-provider (r/adapt-react-class (.-Provider paper)))
(def button (r/adapt-react-class (.-Button paper)))

(def touchable-opacity (r/adapt-react-class (.-TouchableOpacity ReactNative)))

(def icon (r/adapt-react-class (aget ReactNativeVectorIcons "default")))

(def stylesheet (.-StyleSheet ReactNative))

(defn js->>clj [x]
  (->> x
       (js->clj)
       (clojure.walk/keywordize-keys)))

(defn create-stylesheet [styles]
  (->> styles
       (clj->js)
       (.create stylesheet)
       (js->>clj)))

(def styles
  (create-stylesheet
   {:header {:height 60
             :padding 15
             :backgroundColor "darkslateblue"}
    :text {:color "#ffffff"
           :fontSize 23
           :textAlign "center"}
    :container {:flex 1
                :backgroundColor "#ffffff"
                 ; :paddingTop 60}
                }
    :list-item {:padding 15
                :backgroundColor "#f8f8f8"
                :borderBottomWidth 1
                :borderColor "#eee"}
    :list-item-view {:flexDirection "row"
                     :justifyContent "space-between"
                     :alignItems "center"}
    :list-item-text {:fontSize 18}
    :input {:color "#000000"
            :height 60
            :padding 8
            :fontSize 16}
    :btn {:backgroundColor "#c2bad8"
          :padding 9
          :margin 5}
    :btn-text {:color "darkslateblue"
               :fontSize 20
               :textAlign "center"}}))

(defn header [title]
  [rn/view {:style (:header styles)}
   [rn/text {:style (:text styles)} title]])

(defn add-item []
  [rn/view
   [rn/text-input {:placeholder= "Add Item..."
                   :style (:input styles)
                   :onChangeText (fn [text-value] (dispatch [:set-text text-value]))}]
   [touchable-opacity {:style (:btn styles)
                       :onPress (fn [] (dispatch [:add-item]))}
    [rn/text {:style (:btn-text styles)} [icon {:name "plus" :size 20}] " Add Item"]]])

(defn list-item [props]
  [touchable-opacity {:style (:list-item styles)}
   [rn/view {:style (:list-item-view styles)}
    [rn/text {:style (:list-item-text styles)} (:text (:item props))]
    [icon {:name "remove"
           :size 20
           :color "firebrick"
           :onPress (fn [] (dispatch [:delete-item (:id (:item props))]))}]]])

(defn hello []
  (let [items @(subscribe [:items])]
    [paper-provider
     [button {:icon "camera" :mode "contained"} "Vögelchen"]
     [rn/view {:style (:container styles)}
      [header "Shopping List"]
      [add-item]
      [rn/flat-list {:data items
                     :renderItem (fn [props] (r/as-element [list-item (js->>clj props)]))}]]]))
