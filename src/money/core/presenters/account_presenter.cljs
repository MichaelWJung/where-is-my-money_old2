(ns money.core.presenters.account-presenter
  (:require [clojure.spec.alpha :as s]
            [money.core.account :as a]
            [money.core.adapters.account :as aa]
            [money.core.transaction :as t]
            [money.core.utils :as u]))

(s/def ::id int?)
(s/def ::split-id int?)
(s/def ::description string?)
(s/def ::other-account int?)
(s/def ::amount number?)
(s/def ::date int?)
(s/def ::reduced-transaction
  (s/keys :req [::id ::split-id ::description ::other-account ::amount ::date]))
(s/def ::reduced-transactions (s/coll-of ::reduced-transaction))
(s/def ::balance number?)
(s/def ::balances (s/coll-of ::balance))

(defn- get-account-splits [account-id splits]
  (filter #(= account-id (::t/account %)) splits))

;TODO: Handle transactions with more than one split per account
(defn- get-other-account-id [this-account-id splits]
  (let [other-ids (u/remove-first (partial = this-account-id)
                                  (map ::t/account splits))]
    (first other-ids)))

(defn reduce-transaction [transaction accounts account-id]
  (s/assert ::t/transaction transaction)
  (s/assert ::a/accounts accounts)
  (let [splits (::t/splits transaction)
        our-account-splits (get-account-splits account-id splits)
        other-id (get-other-account-id account-id splits)]
    (vec (map-indexed (fn [idx split]
                        {::id (::t/id transaction)
                         ::split-id idx
                         ::description (::t/description transaction)
                         ::amount (::t/amount split)
                         ::date (::t/date transaction)
                         ::other-account other-id})
          our-account-splits))))

(defn- get-amounts [reduced-transactions]
  (map ::amount reduced-transactions))

(defn- last-or-zero [v]
  (if (empty? v)
    0
    (last v)))

(defn reduce-transactions [transactions accounts account-id]
  (s/assert (s/coll-of ::t/transaction) transactions)
  (s/assert ::a/accounts accounts)
  (let [reduced-transactions
        (vec (mapcat #(reduce-transaction % accounts account-id) transactions))]
    {::reduced-transactions reduced-transactions
     ::balances (reductions + (get-amounts reduced-transactions))}))


(defn convert-date [unix-time locale]
  (.toLocaleDateString (js/Date. unix-time)
                       locale
                       #js {:year "numeric" :month "short" :day "numeric"}))

(defn- get-account-name [id accounts]
  (::a/name (get accounts id)))

(defn present-transaction [transaction balance accounts locale]
  (s/assert ::reduced-transaction transaction)
  (s/assert ::a/accounts accounts)
  {:id (::id transaction)
   :split-id (::split-id transaction)
   :description (::description transaction)
   :amount (str (::amount transaction))
   :date (convert-date (::date transaction) locale)
   :account (get-account-name (::other-account transaction) accounts)
   :balance (str balance)})

(defn present-transactions [transactions-and-balances accounts locale]
  (s/assert ::a/accounts accounts)
  (let [transactions (::reduced-transactions transactions-and-balances)
        balances (::balances transactions-and-balances)]
    (s/assert ::reduced-transactions transactions)
    (map #(present-transaction %1 %2 accounts locale) transactions balances)))

(defn- present-account-name [account]
  (s/assert ::a/account account)
  (::a/name account))

(defn present-account-names [accounts]
  (vec (sort (map present-account-name (vals accounts)))))

(defn present-account-list [accounts account-id]
  (s/assert ::a/accounts accounts)
  (let [sorted (aa/get-sorted-id-name-pairs accounts)
        names (mapv (fn [[_ name_]] name_) sorted)
        idx-to-ids (map-indexed (fn [idx [id _]] [idx id]) sorted)
        idx (first (first (filter #(= (second %) account-id) idx-to-ids)))]
    (if (nil? idx) (throw (ex-info "Account id not found" {})))
    {:account-names names
     :account-idx idx
     :active-name (get names idx)}))
