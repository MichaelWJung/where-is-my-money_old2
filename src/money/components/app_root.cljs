(ns money.components.app-root
  (:require [reagent.core :as r]
            [reagent.react-native :as rn]
            [re-frame.core :refer [dispatch]]
            [money.default-components :refer [navigation-container
                                              navigator
                                              paper-provider
                                              pressable
                                              screen]]
            [money.navigation :refer [!navigation]]
            [money.components.account-overview :refer [account-list
                                                       account-overview]]
            [money.components.transaction-screen :refer [transaction-screen]]
            [money.components.utils :refer [js->clj-keywordized
                                            record-navigation-state]]
            ["react-native-paper" :as rnp]
            ["@react-navigation/native" :as rnn]))

(def paper-default-theme (js->clj-keywordized rnp/DefaultTheme))
(def navigation-default-theme (js->clj-keywordized rnn/DefaultTheme))
(def default-theme (assoc paper-default-theme :colors
                          (merge (:colors navigation-default-theme)
                                 (:colors paper-default-theme))))

(defn loading-screen []
  [rn/view
   [rn/text {:style {:color "blue"}} "Loading..."]])

(defn home-screen-fn []
  (fn [{:keys [navigation]}]
    (record-navigation-state :money.db/home-screen)
    [rn/view {:style {:flex-direction "column" :margin 10 :align-items "flex-start"}}
     [account-list {:navigation navigation}]]))

(defn home-screen []
  [:f> home-screen-fn])

(defn root []
  [paper-provider {:theme (clj->js default-theme)}
   [navigation-container {:ref #(reset! !navigation %)
                          :on-ready #(dispatch [:ui-ready])
                          :theme (clj->js default-theme)}
    [navigator {:initialRouteName "Loading"}
     [screen {:name "Loading" :component (r/reactify-component loading-screen)}]
     [screen {:name "Home" :component (r/reactify-component home-screen)}]
     [screen {:name "Account-Overview" :component (r/reactify-component account-overview)}]
     [screen {:name "Transaction" :component (r/reactify-component transaction-screen)}]]]])

