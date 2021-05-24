(ns money.core.account
  (:require [clojure.spec.alpha :as s]))

(def type? #{:normal :trading})

(s/def ::name string?)
(s/def ::currency int?)
(s/def ::parent #(or (int? %) (nil? %)))
(s/def ::type type?)
(s/def ::account (s/keys :req [::name ::currency ::parent ::type]))
(s/def ::accounts (s/map-of int? ::account))
