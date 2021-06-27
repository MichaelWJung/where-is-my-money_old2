(ns money.components.account-overview
  (:require [money.components.utils :refer [js->clj-keywordized]]
            [money.core.presenters.account-presenter :as ap]
            [money.default-components :refer [fab portal portal-host pressable]]
            [money.helpers :refer [record-back-navigation]]
            [reagent.core :as r]
            [reagent.react-native :as rn]
            [re-frame.core :refer [dispatch subscribe]]
            ["@react-navigation/native" :as rnn]))

(defn account-list []
  (let [accounts (subscribe [:account-names])]
    (fn []
      [rn/view {:style {:width "100%"
                        :height "100%"}}
       (for [[idx acc-name] (map-indexed vector (:account-names @accounts))]
         ^{:key idx}
         [pressable
          {:on-press #(dispatch [:set-account idx])
           :android_ripple (clj->js {:color "gray"})}
          [rn/text {:style {:font-size 24 :padding 8}} acc-name]])])))

(defn transaction [{:keys [item]}]
  [pressable
   {:on-press #(dispatch [:edit-transaction (:id item)])
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

(defn key-extractor [item index]
  (let [i (js->clj-keywordized item)]
    (str (:id i) "_" (:split-id i))))

(defn account-overview-fn []
  (record-back-navigation)
  (let [is-focused? (rnn/useIsFocused)
        overview (subscribe [:account-overview])]
    [rn/view {:style {:container {:flex 1}}}
     [rn/flat-list {:data @overview
                    :renderItem render-transaction
                    :key-extractor key-extractor}]
     [portal
      [fab {:icon "plus"
            :visible is-focused?
            :on-press #(dispatch [:new-transaction])
            :style {:position "absolute"
                    :bottom 16
                    :right 16}}]]]))

(defn account-overview []
  [:f> account-overview-fn])


