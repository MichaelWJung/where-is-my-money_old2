(ns money.components.account-overview
  (:require [reagent.react-native :as rn]
            [re-frame.core :refer [dispatch subscribe]]))

(defn account-list []
  (let [accounts (subscribe [:account-names])]
    (fn [{:keys [navigation]}]
      [rn/view
       (for [[idx acc-name] (map-indexed vector (:account-names @accounts))]
         ^{:key idx}
         [rn/touchable-without-feedback
          {:on-press #(do (dispatch [:set-account idx])
                          (.navigate navigation "Account-Overview"))}
          [rn/text {:style {:font-size 24 :padding 8}} acc-name]])])))

(defn transaction [{:keys [data navigation]}]
  [rn/touchable-without-feedback
   {:on-press (fn []
                (dispatch [:edit-transaction (:id data)])
                (.navigate navigation "Transaction"))}
   [rn/view
    [rn/view {:style {:flex-direction "row"}}
     [rn/text {:style {:color "black" :font-size 16
                       :padding-start 8 :padding-top 8 :padding-end 8
                       :flex 1}}
      (:description data)]
     [rn/text {:style {:color "black" :font-size 16
                       :padding-start 8 :padding-top 8 :padding-end 8}}
      (:amount data)]]
    [rn/view {:style {:flex-direction "row"}}
     [rn/text {:style {:color "darkgray" :font-size 16
                       :padding-start 8 :padding-bottom 8 :padding-end 8}}
      (:date data)]
     [rn/text {:style {:color "darkgray" :font-size 16
                       :padding-start 8 :padding-bottom 8 :padding-end 8
                       :flex 1}}
      (:account data)]
     [rn/text {:style {:color "darkgray" :font-size 16
                       :padding-start 8 :padding-bottom 8 :padding-end 8}}
      (:balance data)]]]])

(defn account-overview []
  (let [overview (subscribe [:account-overview])]
    (fn [{:keys [navigation]}]
      [rn/scroll-view
       (for [transaction-data @overview]
         ^{:key (:id transaction-data)}
         [transaction {:navigation navigation :data transaction-data}])])))


