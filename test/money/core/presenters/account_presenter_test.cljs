(ns money.core.presenters.account-presenter-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is are testing]]
            [money.core.account :as a]
            [money.core.presenters.account-presenter :as ap]
            [money.core.screens.account :as sa]
            [money.core.transaction :as t]))

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

(defn- split [account amount]
  {::t/description ""
   ::t/account account
   ::t/amount amount})

(defn- transaction
  ([id description date amount account-id]
   (transaction id description date amount 1 account-id))
  ([id description date amount account-id-1 account-id-2]
   {::t/id id
    ::t/description description
    ::t/date date
    ::t/splits [(split account-id-1 amount)
                (split account-id-2 (- amount))]}))

(deftest date-conversion
  (is (= (ap/convert-date 1578830400000 "de-DE") "12. Jan. 2020"))
  (is (= (ap/convert-date 1578830400000 "en-US") "Jan 12, 2020"))
  (is (= (ap/convert-date 1578830400000 "ru-RU") "12 янв. 2020 г.")))

(deftest reduce-transaction
  (are [transaction expected]
       (let [reduced-transaction (ap/reduce-transaction transaction accounts 1)]
         (and (= reduced-transaction expected)
              (s/valid? ::ap/reduced-transactions reduced-transaction)))

       (transaction 7 "Hotel" 987654 123 2)
       [{::ap/id 7
         ::ap/split-id 0
         ::ap/description "Hotel"
         ::ap/amount 123
         ::ap/date 987654
         ::ap/other-account 2}]

       (transaction 73 "Flight" 55555 10 1 1)
       [{::ap/id 73
         ::ap/split-id 0
         ::ap/description "Flight"
         ::ap/amount 10
         ::ap/date 55555
         ::ap/other-account 1}
        {::ap/id 73
         ::ap/split-id 1
         ::ap/description "Flight"
         ::ap/amount -10
         ::ap/date 55555
         ::ap/other-account 1}]))

(deftest reduce-transactions
  (is (= (ap/reduce-transactions
           [(transaction 1 "Hotel" 987654 123 2)
            (transaction 2 "Movie theater" 555666 444 3)
            (transaction 3 "Taxi" 11111 22 1 1)]
           accounts
           1)
         {::ap/reduced-transactions [{::ap/id 1
                                      ::ap/split-id 0
                                      ::ap/description "Hotel"
                                      ::ap/amount 123
                                      ::ap/date 987654
                                      ::ap/other-account 2}
                                     {::ap/id 2
                                      ::ap/split-id 0
                                      ::ap/description "Movie theater"
                                      ::ap/amount 444
                                      ::ap/date 555666
                                      ::ap/other-account 3}
                                     {::ap/id 3
                                      ::ap/split-id 0
                                      ::ap/description "Taxi"
                                      ::ap/amount 22
                                      ::ap/date 11111
                                      ::ap/other-account 1}
                                     {::ap/id 3
                                      ::ap/split-id 1
                                      ::ap/description "Taxi"
                                      ::ap/amount -22
                                      ::ap/date 11111
                                      ::ap/other-account 1}]
          ::ap/balances [123 567 589 567]})))

(deftest presenter
  (testing "Transactions are correctly converted (without balance)"
    (are [transaction balance locale expected]
         (= (ap/present-transaction transaction balance accounts locale)
            expected)

         {::ap/id 7
          ::ap/split-id 0
          ::ap/description "Hotel"
          ::ap/amount 123
          ::ap/date 1578830400000
          ::ap/other-account 2}
         123
         "de-DE"
         {:id 7
          :split-id 0
          :description "Hotel"
          :amount "123"
          :date "12. Jan. 2020"
          :account "Travel"
          :balance "123"}

         {::ap/id 8
          ::ap/split-id 1
          ::ap/description "Taxi"
          ::ap/amount 42
          ::ap/date 1578830400000
          ::ap/other-account 2}
         456
         "en-US"
         {:id 8
          :split-id 1
          :description "Taxi"
          :amount "42"
          :date "Jan 12, 2020"
          :account "Travel"
          :balance "456"}
         )))

(deftest present-transactions
  (is (= (ap/present-transactions
           {::ap/reduced-transactions [{::ap/id 5
                                        ::ap/split-id 0
                                        ::ap/description "Hotel"
                                        ::ap/amount 123
                                        ::ap/date 1578830400000
                                        ::ap/other-account 2}
                                       {::ap/id 6
                                        ::ap/split-id 3
                                        ::ap/description "Taxi"
                                        ::ap/amount 42
                                        ::ap/date 1578916800000
                                        ::ap/other-account 2}]
            ::ap/balances [123 165]}
           accounts
           "de-DE")

         [{:id 5
           :split-id 0
           :description "Hotel"
           :amount "123"
           :date "12. Jan. 2020"
           :account "Travel"
           :balance "123"}
          {:id 6
           :split-id 3
           :description "Taxi"
           :amount "42"
           :date "13. Jan. 2020"
           :account "Travel"
           :balance "165"}])))

(deftest present-account-names
  (is (= (ap/present-account-names accounts)
         ["Checking Account" "Entertainment" "Travel"])))

(deftest present-account-list
  (is (= (ap/present-account-list accounts 3)
         {:account-names ["Checking Account" "Entertainment" "Travel"]
          :account-idx 1
          :active-name "Entertainment"}))
  (is (thrown? ExceptionInfo (ap/present-account-list accounts {::sa/account-id 4}))))
