(ns money.components.app-root
  (:require [reagent.core :as r]
            [reagent.react-native :as rn]
            ; [money.default-components]
            [money.default-components :refer [navigation-container
                                              navigator
                                              screen]]
            #_[money.default-components :refer [text view
                                                touchable-highlight
                                                navigation-container
                                                navigator
                                                screen]]
            [money.components.account-overview :refer [account-list
                                                       account-overview]]
            [money.components.transaction-screen :refer [transaction-screen]]))

(def ReactNative (js/require "react-native"))

(defn home-screen []
  (fn [{:keys [navigation]}]
    [rn/view {:style {:flex-direction "column" :margin 10 :align-items "flex-start"}}
     [account-list {:navigation navigation}]]))

(defn root []
  (fn []
    [navigation-container
     [navigator {:initialRouteName "Home"}
      [screen {:name "Home" :component (r/reactify-component home-screen)}]
      [screen {:name "Account-Overview" :component (r/reactify-component account-overview)}]
      [screen {:name "Transaction" :component (r/reactify-component transaction-screen)}]]]))

