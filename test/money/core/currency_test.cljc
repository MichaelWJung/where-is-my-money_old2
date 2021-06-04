(ns money.core.currency-test
  (:require [clojure.test :refer [deftest is are testing run-tests]]
            [clojure.spec.alpha :as s]
            [money.core.currency :as c]
            [cljs.core :refer [ExceptionInfo]]))

(deftest validations
  (testing "Currency consists of a name"
    (are [valid currency]
         (= (s/valid? ::c/currency currency) valid)
         false {}
         true {::c/name "BTC"}))
  (testing "Currencies are a map of int to currency"
    (are [valid currencies]
         (= (s/valid? ::c/currencies currencies) valid)
         false {\a {::c/name "BTC"}
                2 {::c/name "CHF"}
                3 {::c/name "USD"}}
         true {1 {::c/name "BTC"}
               2 {::c/name "CHF"}
               3 {::c/name "USD"}}))
  (testing "Exchange rates consist of two currency ids and a factor"
    (are [valid exchange-rate]
         (= (s/valid? ::c/exchange-rate exchange-rate) valid)
         false [1 2 "abc"]
         false ["abc" 1 2.5]
         false [1 "abc" 2.5]
         true [1 2 1.25])))

; #?(:clj  (def ExceptionInfo clojure.lang.ExceptionInfo)
;    :cljs (def ExceptionInfo cljs.core/ExceptionInfo))

(deftest conversions
  (testing "Converts"
    (are [expected conversion]
         (let [[amount from to exchange-rate] conversion]
           (= expected (c/convert amount from to exchange-rate)))
         7.5 [5.0 1 2 [1 2 1.5]]
         10.0 [4.0 1 2 [1 2 2.5]]
         2.0 [4.0 2 1 [1 2 2.0]])
    (is (thrown? ExceptionInfo
                 (c/convert 4.0 2 3 [1 2 2.0])))
    (is (thrown? ExceptionInfo
                 (c/convert 4.0 3 1 [1 2 2.0])))
    ))

