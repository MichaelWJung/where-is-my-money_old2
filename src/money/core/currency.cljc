(ns money.core.currency
  (:require [clojure.spec.alpha :as s]))

(s/def ::name string?)
(s/def ::currency (s/keys :req [::name]))
(s/def ::currencies (s/map-of int? ::currency))

(s/def ::exchange-rate (s/cat :from int? :to int? :factor number?))

(defn convert [amount from to exchange-rate]
  (let [[rate-from rate-to factor] exchange-rate]
    (if-not (or (and (= from rate-from) (= to rate-to))
                (and (= from rate-to) (= to rate-from)))
      (throw (ex-info "Exchange rate does not match conversion currencies" {}))
      (if (= from rate-from)
        (* amount factor)
        (/ amount factor))
      )))
