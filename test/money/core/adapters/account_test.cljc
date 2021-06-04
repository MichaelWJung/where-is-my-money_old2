(ns money.core.adapters.account-test
  (:require [clojure.test :refer [deftest is are testing]]
            [money.core.account :as a]
            [money.core.adapters.account :as aa]))

(def account1 {::a/name "Checking Account"
               ::a/currency 1
               ::a/parent nil
               ::a/type :normal})
(def account2 {::a/name "Travel"
               ::a/currency 1
               ::a/parent nil
               ::a/type :normal})
(def account3 {::a/name "Entertainment"
               ::a/currency 1
               ::a/parent nil
               ::a/type :normal})
(def accounts {1 account1
               2 account2
               3 account3})

(deftest account-idx->id
  (are [idx id] (= (aa/account-idx->id accounts idx) id)
       1 3
       0 1
       2 2))
