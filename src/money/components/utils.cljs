(ns money.components.utils
  (:require [clojure.walk]
            [re-frame.core :refer [dispatch]]
            ["react" :as react]
            ["@react-navigation/native" :as rnn]))

(defn js->clj-keywordized [x]
  (->> x
       (js->clj)
       (clojure.walk/keywordize-keys)))

(defn record-navigation-state [screen]
  (rnn/useFocusEffect #(do (dispatch [:record-navigation-screen screen]) js/undefined)))
