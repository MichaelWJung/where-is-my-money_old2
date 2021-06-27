(ns money.components.transaction-screen
  (:require [reagent.core :as r]
            [reagent.react-native :as rn]
            [re-frame.core :refer [dispatch subscribe]]
            [money.default-components :refer [button
                                              date-time-picker
                                              picker
                                              picker-item
                                              text-input]]
            [money.core.presenters.transaction-presenter :as tp]
            [money.helpers :refer [record-back-navigation]]))

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
        :on-change-text #(dispatch [:update-transaction-amount %])}])))

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

(defn account-picker []
  (let [selected-id (subscribe [:transaction-screen-selected-account])
        accounts (subscribe [:transaction-screen-accounts])
        selected-account (r/atom @selected-id)]
    [picker
     {:selected-value @selected-id
      :on-value-change #(dispatch [:update-transaction-account %1])}
     (for [[i acc] (map-indexed vector @accounts)]
       [picker-item
        {:key i
         :label acc
         :value i}])]))

(defn ok-button []
  (let [button-text (subscribe [:transaction-screen-ok-button-text])]
    [button {:mode "contained"
             :on-press #(dispatch [:save-transaction])}
     @button-text]))

(defn transaction-screen-fn []
  (record-back-navigation)
  [:<>
   [description-input]
   [amount-input]
   [date-field]
   [account-picker]
   [ok-button]])

(defn transaction-screen []
  [:f> transaction-screen-fn])
