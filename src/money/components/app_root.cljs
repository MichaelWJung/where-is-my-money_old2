(ns money.components.app-root
  (:require [reagent.core :as r]
            [reagent.react-native :as rn]
            [re-frame.core :refer [dispatch]]
            [money.default-components :refer [navigation-container
                                              navigator
                                              pressable
                                              screen]]
            [money.navigation :refer [!navigation]]
            [money.components.account-overview :refer [account-list
                                                       account-overview]]
            [money.components.transaction-screen :refer [transaction-screen]]))

(defn loading-screen []
  [rn/view
   [rn/text {:style {:color "blue"}} "Loading..."]])

(defn home-screen []
  (fn [{:keys [navigation]}]
    [rn/view {:style {:flex-direction "column" :margin 10 :align-items "flex-start"}}
     [account-list {:navigation navigation}]]))

(defn root []
  [navigation-container {:ref #(reset! !navigation %)
                         :on-ready #(dispatch [:ui-ready])}
   [navigator {:initialRouteName "Loading"}
    [screen {:name "Loading" :component (r/reactify-component loading-screen)}]
    [screen {:name "Home" :component (r/reactify-component home-screen)}]
    [screen {:name "Account-Overview" :component (r/reactify-component account-overview)}]
    [screen {:name "Transaction" :component (r/reactify-component transaction-screen)}]]])

