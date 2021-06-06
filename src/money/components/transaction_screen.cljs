(ns money.components.transaction-screen
  (:require [reagent.core :as r]
            [reagent.react-native :as rn]
            [re-frame.core :refer [dispatch subscribe]]
            [money.default-components :refer [button
                                              date-time-picker
                                              text-input]]
            [money.core.presenters.transaction-presenter :as tp]))

(defn description-input []
  (let [description (subscribe [:transaction-screen-description])]
    (fn []
      [text-input
       {:label "Description"
        :default-value @description
        :on-change-text #(dispatch [:update-transaction-description %])}])))

(defn amount-input []
  (let [amount (subscribe [:transaction-screen-amount])]
    (fn []
      [text-input
       {:label "Amount"
        :default-value @amount
        :keyboard-type "numeric"
        :on-change-text #(dispatch [:update-transaction-amount (js/parseFloat %)])}])))

(defn date-field []
  (let [date (subscribe [:transaction-screen-date])
        editing-date (r/atom false)]
    (fn []
      [rn/view
       [rn/touchable-without-feedback
        {:on-press #(reset! editing-date true)}
        [rn/text {:style {:margin 10 :font-size 20}} "Date: " @date]]
       (if @editing-date
         [date-time-picker
          {:value (js/Date. @date)
           :on-change (fn [_ date]
                        (if (some? date)
                          (let [unix-time (.valueOf date)]
                            (dispatch [:update-transaction-date unix-time])))
                        (reset! editing-date false))}])])))

(defn transaction-screen []
  [:<>
   [description-input]
   [amount-input]
   [date-field]
   [button {:mode "contained"
            :on-press #(dispatch [:save-transaction])}
    "Save"]])
