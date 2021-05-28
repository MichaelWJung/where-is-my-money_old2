(ns money.components.account-overview
  (:require [money.components.utils :refer [js->clj-keywordized]]
            [reagent.core :as r]
            [reagent.react-native :as rn]
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

(defn transaction [{:keys [item]} navigation]
  [rn/touchable-without-feedback
   {:on-press (fn []
                (dispatch [:edit-transaction (:id item)])
                (.navigate navigation "Transaction"))}
   [rn/view
    [rn/view {:style {:flex-direction "row"}}
     [rn/text {:style {:color "black" :font-size 16
                       :padding-start 8 :padding-top 8 :padding-end 8
                       :flex 1}}
      (:description item)]
     [rn/text {:style {:color "black" :font-size 16
                       :padding-start 8 :padding-top 8 :padding-end 8}}
      (:amount item)]]
    [rn/view {:style {:flex-direction "row"}}
     [rn/text {:style {:color "darkgray" :font-size 16
                       :padding-start 8 :padding-bottom 8 :padding-end 8}}
      (:date item)]
     [rn/text {:style {:color "darkgray" :font-size 16
                       :padding-start 8 :padding-bottom 8 :padding-end 8
                       :flex 1}}
      (:account item)]
     [rn/text {:style {:color "darkgray" :font-size 16
                       :padding-start 8 :padding-bottom 8 :padding-end 8}}
      (:balance item)]]]])

(defn render-transaction [navigation]
  (fn [props]
    (r/as-element [transaction (js->clj-keywordized props) navigation])))

(defn account-overview []
  (let [overview (subscribe [:account-overview])]
    (fn [{:keys [navigation]}]
      [rn/flat-list {:data @overview
                     :renderItem (render-transaction navigation)}])))


