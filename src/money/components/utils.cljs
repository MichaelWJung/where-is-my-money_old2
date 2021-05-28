(ns money.components.utils
  (:require [clojure.walk]))

(defn js->clj-keywordized [x]
  (->> x
       (js->clj)
       (clojure.walk/keywordize-keys)))
