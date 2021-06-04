(ns money.core.screens.transaction-test
  (:require [clojure.core :refer [ExceptionInfo]]
            [clojure.test :refer [deftest is are testing]]
            [money.core.account :as a]
            [money.core.screens.transaction :as st]
            [money.core.transaction :as t]))

(deftest parse-string-to-amount
  (testing "Result is number"
    (are [string expected] (= (st/string->amount string) expected)
         "10.0" 10.0
         "10" 10.0))
  (testing "Throws if input is not number"
    (are [string] (thrown? ExceptionInfo (st/string->amount string))
         "10a"
         "abc"
         "{}")))

(defn- account [name_]
  {::a/name name_ ::a/currency 0 ::a/parent nil ::a/type :normal})

(deftest update-transaction-screen
  (testing "Transaction screen data is updated correctly"
    (is (= (st/update-screen
             {::st/description "Buy gold"
              ::st/date 12345678910
              ::st/account-id 0
              ::st/amount 10.0
              ::st/id 6
              ::st/new? true}
             {3 (account "Car")
              7 (account "Insurance")
              10 (account "Entertainment")}
             {:description "Buy bitcoin"
              :date 55555
              :account-idx 2
              :amount "20.0"})
           {::st/description "Buy bitcoin"
            ::st/date 55555
            ::st/account-id 7
            ::st/amount 20.0
            ::st/id 6
            ::st/new? true})))
  (testing "Fails if value is missing"
    (are [new-data] (thrown?
                      ExceptionInfo
                      (st/update-screen
                        {::st/description "Buy gold"
                         ::st/date 12345678910
                         ::st/account-id 0
                         ::st/amount 10.0
                         ::st/id 6
                         ::st/new? true}
                        {3 (account "Car")
                         7 (account "Insurance")
                         10 (account "Entertainment")}
                        new-data))
         {:date 55555
          :account-idx 2
          :amount "20.0"}
         {:description "Buy bitcoin"
          :account-idx 2
          :amount "20.0"}
         {:description "Buy bitcoin"
          :date 55555
          :amount "20.0"}
         {:description "Buy bitcoin"
          :date 55555
          :account-id 2}))
  (testing "Fails on invalid amount"
    (is (thrown?
          ExceptionInfo
          (st/update-screen
            {::st/description "Buy gold"
             ::st/date 12345678910
             ::st/account-id 0
             ::st/amount 10.0
             ::st/id 6
             ::st/new? true}
            {3 (account "Car")
             7 (account "Insurance")
             10 (account "Entertainment")}
            {:description "Buy bitcoin"
             :date 55555
             :account-idx 2
             :amount "008"})))))

(deftest screen-data->transaction
  (testing "Transformed to valid transaction"
    (is (= (st/screen-data->transaction
             1
             {::st/description "Buy gold"
              ::st/date 12345678910
              ::st/account-id 0
              ::st/amount 10.0
              ::st/id 7
              ::st/new? true})
           {::t/id 7
            ::t/description "Buy gold"
            ::t/date 12345678910
            ::t/splits [{::t/description ""
                         ::t/account 1
                         ::t/amount 10.0}
                        {::t/description ""
                         ::t/account 0
                         ::t/amount -10.0}]}))))

(deftest transaction->screen-data
  (testing "Transaction with two splits is transformed to screen data"
    (is (= (st/transaction->screen-data
             1
             {::t/id 7
              ::t/description "Buy gold"
              ::t/date 12345678910
              ::t/splits [{::t/description ""
                           ::t/account 1
                           ::t/amount 10.0}
                          {::t/description ""
                           ::t/account 0
                           ::t/amount -10.0}]})
           {::st/description "Buy gold"
            ::st/date 12345678910
            ::st/account-id 0
            ::st/amount 10.0
            ::st/id 7
            ::st/new? false})))
  (testing "Should pick the correct split to fill data"
    (is (= (st/transaction->screen-data
             1
             {::t/id 7
              ::t/description "Buy gold"
              ::t/date 12345678910
              ::t/splits [{::t/description ""
                           ::t/account 0
                           ::t/amount -10.0}
                          {::t/description ""
                           ::t/account 1
                           ::t/amount 10.0}]})
           {::st/description "Buy gold"
            ::st/date 12345678910
            ::st/account-id 0
            ::st/amount 10.0
            ::st/id 7
            ::st/new? false})))
  (testing "Should fail if transaction has more than two splits"
    (is (thrown? ExceptionInfo
                 (st/transaction->screen-data
                   1
                   {::t/id 7
                    ::t/description "Buy gold"
                    ::t/date 12345678910
                    ::t/splits [{::t/description ""
                                 ::t/account 0
                                 ::t/amount -10.0}
                                {::t/description ""
                                 ::t/account 1
                                 ::t/amount 2.0}
                                {::t/description ""
                                 ::t/account 2
                                 ::t/amount 8.0}]}))))
  (testing "Should fail if account id is not in splits"
    (is (thrown? ExceptionInfo
                 (st/transaction->screen-data
                   2
                   {::t/id 7
                    ::t/description "Buy gold"
                    ::t/date 12345678910
                    ::t/splits [{::t/description ""
                                 ::t/account 0
                                 ::t/amount -10.0}
                                {::t/description ""
                                 ::t/account 1
                                 ::t/amount 10.0}]})))))

(deftest new-transaction
  (testing "Should return an empty new screen with given id, date, and account"
    (is (= (st/new-transaction 23 42 73)
           {::st/description ""
            ::st/date 42
            ::st/account-id 73
            ::st/amount 0.0
            ::st/id 23
            ::st/new? true}))))
