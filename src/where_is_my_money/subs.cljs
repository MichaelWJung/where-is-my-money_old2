(ns where-is-my-money.subs
  (:require [re-frame.core :as rf :refer [reg-sub]]))

(reg-sub
 :items
 (fn [db _]
   (vals (:items db))))
