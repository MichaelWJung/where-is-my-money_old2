(ns money.components.transaction-screen
  (:require [reagent.core :as r]
            [reagent.react-native :as rn]
            [re-frame.core :refer [dispatch subscribe]]
            [money.default-components :refer [button date-time-picker]]
            [money.core.presenters.transaction-presenter :as tp]))

(defn transaction-screen []
  (let [data (subscribe [:transaction-screen])
        editing-date (r/atom false)]
    (fn []
      (let [d @data]
        [rn/view
         [rn/text "Description: " (::tp/description d)]
         [rn/touchable-without-feedback
          {:on-press #(reset! editing-date true)}
          [rn/text {:style {:margin 10 :font-size 20}} "Date: " (::tp/date d)]]
         [button {:mode "contained"
                  :on-press #(dispatch [:save-transaction])}
          "Save"]
         (if @editing-date
           [date-time-picker
            {:value (js/Date. (::tp/date d))
             :on-change (fn [_ date]
                          (if (some? date)
                            (let [unix-time (.valueOf date)]
                              (dispatch [:update-transaction-date unix-time])))
                          (reset! editing-date false))}])]))))
