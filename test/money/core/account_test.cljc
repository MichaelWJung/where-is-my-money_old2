(ns money.core.account-test
  (:require [clojure.test :refer [deftest is are testing run-tests]]
            [clojure.spec.alpha :as s]
            [money.core.account :as a]))

(def test_account {::a/name "abc"
                   ::a/currency 123
                   ::a/parent nil
                   ::a/type :normal})

(deftest validations
  (testing "Account needs name, curreny, parent, type"
    (are [valid account] (= (s/valid? ::a/account account) valid)
         false {}
         false {::a/name "abc"}
         false {::a/currency 123}
         false {::a/parent nil}
         false {::a/name "abc" ::a/currency 123}
         false {::a/name "abc" ::a/parent nil}
         false {::a/currency 123 ::a/parent nil}
         false {::a/name "abc" ::a/currency 123 ::a/parent nil}
         false {::a/currency 123 ::a/parent nil ::a/type :normal}
         false {::a/name "abc" ::a/parent nil ::a/type :normal}
         false {::a/name "abc" ::a/currency 123 ::a/type :normal}
         true {::a/name "abc" ::a/currency 123 ::a/parent nil ::a/type :normal}
         true {::a/name "abc" ::a/currency 123 ::a/parent 42 ::a/type :normal}
         false {::a/name "abc" ::a/currency 123 ::a/parent "xyz" ::a/type :normal}))
  (testing "Accounts are a map of int to account"
    (are [valid accounts] (= (s/valid? ::a/accounts accounts) valid)
         false {\a test_account}
         true {1 test_account}
         true {1 test_account
               2 test_account})))
