(ns money.core.presenters.transaction-presenter-test
  (:require [clojure.test :refer [deftest is are testing]]
            [money.core.account :as a]
            [money.core.presenters.transaction-presenter :as tp]
            [money.core.screens.transaction :as st]))

(defn- account [id name]
  {::a/name name
   ::a/currency 1
   ::a/parent nil
   ::a/type :normal})

(deftest present-transaction-screen
  (testing "New transaction screen is presented correctly"
    (is (=
         (tp/present-transaction-screen
           {::st/description "Movie tickets"
            ::st/date 123
            ::st/account-id 7
            ::st/amount 10.0
            ::st/id 1
            ::st/new? true}
           {0 (account 0 "Rent")
            7 (account 7 "Cash")
            2 (account 2 "Car maintenance")})

         {::tp/screen-title tp/new-transaction-title
          ::tp/ok-button-text tp/create-button-text
          ::tp/description "Movie tickets"
          ::tp/date 123
          ::tp/amount "10"
          ::tp/selected-account 1
          ::tp/accounts ["Car maintenance" "Cash" "Rent"]}))))
