(ns money.core.adapters.account
  (:require [clojure.spec.alpha :as s]
            [money.core.account :as a]))

(defn get-sorted-id-name-pairs [accounts]
  (s/assert ::a/accounts accounts)
  (vec (sort-by second (map (fn [[id acc]] [id (::a/name acc)]) accounts))))

(defn account-idx->id [accounts idx]
  (s/assert ::a/accounts accounts)
  (let [sorted (get-sorted-id-name-pairs accounts)]
    (first (sorted idx))))
