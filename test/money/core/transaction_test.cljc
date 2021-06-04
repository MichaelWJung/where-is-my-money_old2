(ns money.core.transaction-test
  (:require [clojure.core :refer [ExceptionInfo]]
            [clojure.test :refer [deftest is are testing run-tests]]
            [clojure.spec.alpha :as s]
            [money.core.account :as a]
            [money.core.currency :as c]
            [money.core.transaction :as t]))

(def split1 {::t/description "abc" ::t/account 1 ::t/amount 10})
(def split2 {::t/description "xyz" ::t/account 2 ::t/amount -10})
(def splits [split1 split2])

(deftest spec-validations
  (testing "Split needs description, account, amount"
    (are [valid split] (= (s/valid? ::t/split split) valid)
         false {}
         false {::t/description "abc" ::t/account 123}
         false {::t/description "abc" ::t/amount 123}
         true {::t/description "abc" ::t/account 123 ::t/amount 123}
         true split1
         true split2))
  (testing "The splits of a transaction need to be a vector of at least two"
    (are [valid splits] (= (s/valid? ::t/splits splits) valid)
         false [split1]
         false '(split1 split2)
         true [split1 split2]))
  (testing "Transaction needs description, data, splits"
    (are [valid transaction] (= (s/valid? ::t/transaction transaction) valid)
         false {}
         false {::t/description "abc" ::t/date 1}
         false {::t/description "abc" ::t/splits splits}
         false {::t/date 1 ::splits splits}
         false {::t/description "abc" ::t/date 1 ::t/splits splits}
         true {::t/id 1 ::t/description "abc" ::t/date 1 ::t/splits splits}
         true {::t/id 1
               ::t/description "abc"
               ::t/date 1
               ::t/splits splits
               ::c/exchange-rate [1 2 1.25]})))

(def account1 {::a/name "account 1"
               ::a/currency 1
               ::a/parent nil
               ::a/type :normal})
(def account2 {::a/name "account 2"
               ::a/currency 1
               ::a/parent nil
               ::a/type :normal})
(def accounts {1 account1
               2 account2})

(def currency1 {::c/name "BTC"})
(def currency2 {::c/name "Grams of gold"})
(def currencies {1 currency1 2 currency2})

(defn- transaction
  ([id splits]
   (transaction id 1 splits))
  ([id date splits]
   {::t/id id
    ::t/description ""
    ::t/date date
    ::t/splits splits}))

(defn- split [account amount]
  {::t/description ""
   ::t/account account
   ::t/amount amount})

(defn- valid-transaction? [transaction]
  (t/valid-transaction? transaction accounts currencies))

(deftest consistency-validations
  (testing "Validation functions checks specs"
    (is (not (valid-transaction?
               {::t/description "abc" ::t/date 1 ::t/splits splits}))))
  (testing "Splits need to be balanced"
    (are [valid transaction] (= (valid-transaction? transaction) valid)
         true (transaction 1 [(split 1 1.0) (split 2 -1.0)])
         false (transaction 2 [(split 1 1.0) (split 2 2.0)])))
  ; (testing "Accounts all exist"
  ;   (are [valid transaction] (= (valid-transaction? transaction) valid)
  ;        false (transaction [(split 1 1.0) (split 7 -1.0)])))
  )

(defn- balanced-splits [[account1 account2] amount]
  [(split account1 amount)
   (split account2 (- amount))])

(deftest get-account-transactions
  (testing "Returns transactions containing account id, sorted by date"
    (are [transactions-map account-id result]
         (= (t/get-account-transactions transactions-map account-id) result)

         {}
         1
         []

         {1 (transaction 1 10 (balanced-splits [1 2] 10.0))
          2 (transaction 2 20 (balanced-splits [1 3] 20.0))
          3 (transaction 3 30 (balanced-splits [2 3] 40.0))}
         2
         [(transaction 1 10 (balanced-splits [1 2] 10.0))
          (transaction 3 30 (balanced-splits [2 3] 40.0))]

         {1 (transaction 1 10 (balanced-splits [1 3] 10.0))
          2 (transaction 2 40 (balanced-splits [1 2] 20.0))
          3 (transaction 3 20 (balanced-splits [1 3] 30.0))
          4 (transaction 4 30 (balanced-splits [2 3] 40.0))}
         1
         [(transaction 1 10 (balanced-splits [1 3] 10.0))
          (transaction 3 20 (balanced-splits [1 3] 30.0))
          (transaction 2 40 (balanced-splits [1 2] 20.0))])))

(deftest remove-transaction-by-id
  (testing "Transaction not found: no change"
    (is (= (t/remove-transaction-by-id
             {1 (transaction 1 10 (balanced-splits [1 2] 10.0))
              2 (transaction 2 20 (balanced-splits [1 3] 20.0))
              3 (transaction 3 30 (balanced-splits [2 3] 40.0))}
             4)
           {1 (transaction 1 10 (balanced-splits [1 2] 10.0))
            2 (transaction 2 20 (balanced-splits [1 3] 20.0))
            3 (transaction 3 30 (balanced-splits [2 3] 40.0))})))
  (testing "Transaction found: removed"
    (is (= (t/remove-transaction-by-id
             {1 (transaction 1 10 (balanced-splits [1 2] 10.0))
              2 (transaction 2 20 (balanced-splits [1 3] 20.0))
              3 (transaction 3 30 (balanced-splits [2 3] 40.0))}
             2)
           {1 (transaction 1 10 (balanced-splits [1 2] 10.0))
            3 (transaction 3 30 (balanced-splits [2 3] 40.0))}))))

(deftest create-simple-transaction
  (testing "Simple transaction is created"
    (is
      (= (t/create-simple-transaction {:id 123
                                       :description "abcd"
                                       :date 10
                                       :account1 1
                                       :account2 2
                                       :amount 10.0})
         {::t/id 123
          ::t/description "abcd"
          ::t/date 10
          ::t/splits [{::t/description "" ::t/account 1 ::t/amount 10.0}
                      {::t/description "" ::t/account 2 ::t/amount -10.0}]})))
  (testing "Return nil if value is missing"
    (are [transaction]
         (thrown? ExceptionInfo (t/create-simple-transaction transaction))
         {:id 123 :description "abcd" :date 10 :account1 1 :account2 2}
         {:id 123 :description "abcd" :date 10 :account1 1 :amount 10.0}
         {:id 123 :description "abcd" :date 10 :account2 2 :amount 10.0}
         {:description "abcd" :date 10 :account1 1 :account2 2 :amount 10.0})))

(deftest add-simple-transaction
  (testing "Adds simple transaction"
    (is (= (t/add-simple-transaction
             {1 (transaction 1 10 (balanced-splits [1 2] 10.0))
              2 (transaction 2 20 (balanced-splits [1 3] 20.0))
              3 (transaction 3 30 (balanced-splits [2 3] 40.0))}
             {:id 123
              :description "abcd"
              :date 10
              :account1 1
              :account2 2
              :amount 10.0})
             {1 (transaction 1 10 (balanced-splits [1 2] 10.0))
              2 (transaction 2 20 (balanced-splits [1 3] 20.0))
              3 (transaction 3 30 (balanced-splits [2 3] 40.0))
              123 (assoc (transaction 123 10 (balanced-splits [1 2] 10.0))
                         ::t/description "abcd")}))))

(deftest add-transaction
  (testing "Id does not exist => add"
    (is (= (t/add-transaction
             {1 (transaction 1 10 (balanced-splits [1 2] 10.0))
              2 (transaction 2 20 (balanced-splits [1 3] 20.0))
              3 (transaction 3 30 (balanced-splits [2 3] 40.0))}
             (transaction 123 10 (balanced-splits [1 2] 10.0)))
             {1 (transaction 1 10 (balanced-splits [1 2] 10.0))
              2 (transaction 2 20 (balanced-splits [1 3] 20.0))
              3 (transaction 3 30 (balanced-splits [2 3] 40.0))
              123 (transaction 123 10 (balanced-splits [1 2] 10.0))})))
  (testing "Id already exists => replace"
    (is (= (t/add-transaction
             {1 (transaction 1 10 (balanced-splits [1 2] 10.0))
              2 (transaction 2 20 (balanced-splits [1 3] 20.0))
              3 (transaction 3 30 (balanced-splits [2 3] 40.0))}
             (transaction 3 10 (balanced-splits [1 2] 10.0)))
             {1 (transaction 1 10 (balanced-splits [1 2] 10.0))
              2 (transaction 2 20 (balanced-splits [1 3] 20.0))
              3 (transaction 3 10 (balanced-splits [1 2] 10.0))}))))
