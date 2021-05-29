(ns money.components.account-overview
  (:require [money.components.utils :refer [js->clj-keywordized]]
            [money.default-components :refer [pressable]]
            [reagent.core :as r]
            [reagent.react-native :as rn]
            [re-frame.core :refer [dispatch subscribe]]))

(defn account-list []
  (let [accounts (subscribe [:account-names])]
    (fn []
      [rn/view {:style {:width "100%"
                        :height "100%"}}
       (for [[idx acc-name] (map-indexed vector (:account-names @accounts))]
         ^{:key idx}
         [pressable
          {:on-press #(do (dispatch [:set-account idx])
                          (dispatch [:navigate "Account-Overview"]))
           :android_ripple (clj->js {:color "gray"})}
          [rn/text {:style {:font-size 24 :padding 8}} acc-name]])])))

(defn transaction [{:keys [item]}]
  [pressable
   {:on-press (fn []
                (dispatch [:edit-transaction (:id item)])
                (dispatch [:navigate "Transaction"]))
    :android_ripple (clj->js {:color "gray"})}
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

(defn render-transaction [props]
  (r/as-element [transaction (js->clj-keywordized props)]))

(defn account-overview []
  (let [overview (subscribe [:account-overview])]
    (fn []
      [rn/flat-list {:data @overview
                     :renderItem render-transaction}])))


