(ns money.components.app-root
  (:require [reagent.core :as r]
            [reagent.react-native :as rn]
            [money.default-components :refer [navigation-container
                                              navigator
                                              screen]]
            [money.navigation :refer [!navigation]]
            [money.components.account-overview :refer [account-list
                                                       account-overview]]
            [money.components.transaction-screen :refer [transaction-screen]]))

(defn home-screen []
  (fn [{:keys [navigation]}]
    [rn/view {:style {:flex-direction "column" :margin 10 :align-items "flex-start"}}
     [account-list {:navigation navigation}]]))

(defn root []
  (fn []
    [navigation-container {:ref (fn [el]
                                  (reset! !navigation el))}
     [navigator {:initialRouteName "Home"}
      [screen {:name "Home" :component (r/reactify-component home-screen)}]
      [screen {:name "Account-Overview" :component (r/reactify-component account-overview)}]
      [screen {:name "Transaction" :component (r/reactify-component transaction-screen)}]]]))


