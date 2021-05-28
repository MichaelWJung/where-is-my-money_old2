(ns money.core.screens.transaction
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]
            [money.core.adapters.account :as aa]
            [money.core.transaction :as t]))

(s/def ::description string?)
(s/def ::date int?)
(s/def ::account-id int?)
(s/def ::amount number?)
(s/def ::id int?)
(s/def ::new? boolean?)

(s/def ::transaction-screen-state
  (s/keys :req [::description ::date ::account-id ::amount ::id ::new?]))

(defn string->amount [s]
  (let [r (cljs.reader/read-string s)]
    (if-not (number? r)
      (throw (ex-info "Cannot parse amount string" {}))
      r)))

; TODO: Write unit test
(defn update-transaction-date [screen date]
  (s/assert ::transaction-screen-state screen)
  (if (nil? date)
    (throw (ex-info "Missing values" {})))
  (assoc screen ::date date))

(defn update-screen [screen accounts new-data]
  (let [{:keys [description date account-idx amount]} new-data]
    (s/assert ::transaction-screen-state screen)
    (if (or (nil? description)
            (nil? date)
            (nil? account-idx)
            (nil? amount))
      (throw (ex-info "Missing values" {})))
    (-> screen
        (assoc ::description description)
        (assoc ::date date)
        (assoc ::account-id (aa/account-idx->id accounts account-idx))
        (assoc ::amount (string->amount amount)))))

(defn- create-balanced-splits [account1 account2 amount]
  [{::t/description "" ::t/account account1 ::t/amount amount}
   {::t/description "" ::t/account account2 ::t/amount (- amount)}])

(defn screen-data->transaction [from-account screen-data]
  (let [{:keys [::description ::date ::account-id ::amount ::id]} screen-data]
    (s/assert ::transaction-screen-state screen-data)
    {::t/id id
     ::t/description description
     ::t/date date
     ::t/splits (create-balanced-splits from-account account-id amount)}))

(defn- get-split-with-id [[split1 split2 :as splits] account-id]
  (if (not-any? #(= account-id (::t/account %)) splits)
    (throw (ex-info "Account id not found in transaction splits" {})))
  (if (= account-id (::t/account split1))
    split1
    split2))

(defn- get-other-split [[split1 split2 :as splits] account-id]
  (if (not-any? #(= account-id (::t/account %)) splits)
    (throw (ex-info "Account id not found in transaction splits" {})))
  (if (= account-id (::t/account split1))
    split2
    split1))

(defn transaction->screen-data [account transaction]
  (let [{:keys [::t/id ::t/description ::t/date ::t/splits]} transaction]
    (if (> (count splits) 2)
      (throw
        (ex-info "Cannot convert transactions with more than two splits" {})))
    (let [our-split (get-split-with-id splits account)
          other-split (get-other-split splits account)]
      ; (prn our-split)
      ; (prn other-split)
      {::description description
       ::date date
       ::account-id (::t/account other-split)
       ::amount (::t/amount our-split)
       ::id id
       ::new? false})))

(defn new-transaction [id date account]
  {::description ""
   ::date date
   ::account-id account
   ::amount 0.0
   ::id id
   ::new? true})
