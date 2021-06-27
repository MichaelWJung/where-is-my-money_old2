(ns money.helpers
  (:require [re-frame.core :refer [dispatch]]
            ["react" :as react]
            ["@react-navigation/native" :as rnn]))

(defn record-back-navigation []
  (let [^js navigation (rnn/useNavigation)]
    (.useEffect
      react
      (fn []
        (.addListener
          navigation
          "beforeRemove"
          #(dispatch [:record-navigating-back]))))))

