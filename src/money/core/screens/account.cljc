(ns money.core.screens.account
  (:require [cljs.spec.alpha :as s]))

(s/def ::account-id int?)

(s/def ::account-screen-state
  (s/keys :req [::account-id]))
