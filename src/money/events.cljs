(ns money.events
  (:require [money.db :as db]
            [money.navigation :refer [!navigation]]
            ; [money.store :refer [data->store]]
            [clojure.spec.alpha :as s]
            [money.core.adapters.account :as aa]
            [money.core.screens.account :as sa]
            [money.core.screens.transaction :as st]
            [money.core.transaction :as t]
            [re-frame.core :as rf]))

(defn check-and-throw
  "Throws an exception if `db` doesn’t match the Spec `a-spec`."
  [a-spec db]
  ; (prn db)
  (when-not (s/valid? a-spec db)
    (println "db: " db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

(def check-spec-interceptor (rf/after (partial check-and-throw :money.db/db)))

; (def ->store (rf/after data->store))

(def transaction-interceptors [check-spec-interceptor
                               ; ->store
                               (rf/path :data :transactions)])

(def data-interceptors [check-spec-interceptor
                        ; ->store
                        ])

(rf/reg-fx
  :navigate
  (fn [target]
    (let [navigation @!navigation]
      (.navigate navigation target))))

(rf/reg-cofx
  :now
  (fn [cofx _data]
    (assoc cofx :now (.now js/Date))))

(rf/reg-event-db
  :initialize-db
  [check-spec-interceptor]
  (fn [_ [_ stored]]
    (merge db/default-db stored)))

(rf/reg-event-db
  :set-account
  data-interceptors
  (fn [db [_ account-idx]]
    (let [accounts (get-in db [:data :accounts])]
      (assoc-in db
                [::db/screen-states ::sa/account-screen-state ::sa/account-id]
                (aa/account-idx->id accounts account-idx)))))

(rf/reg-event-db
  :remove-transaction
  transaction-interceptors
  (fn [transactions [_ id-to-remove]]
    (t/remove-transaction-by-id transactions id-to-remove)))

(rf/reg-event-db
  :update-transaction-date
  data-interceptors
  (fn [db [_ date]]
    (if-not (contains? (::db/screen-states db) ::st/transaction-screen-state)
      db
      (update-in db [::db/screen-states ::st/transaction-screen-state]
                 #(st/update-transaction-date % date)))))

(rf/reg-event-db
  :update-transaction-data
  data-interceptors
  (fn [db [_ transaction-data]]
    (if-not (contains? (::db/screen-states db) ::st/transaction-screen-state)
      db
      (let [accounts (get-in db [:data :accounts])]
        (try
          (update-in db
                     [::db/screen-states ::st/transaction-screen-state]
                     #(st/update-screen % accounts transaction-data))
          (catch ExceptionInfo e
            (prn (ex-data e))
            db))))))

(rf/reg-event-db
  :save-transaction
  data-interceptors
  (fn [db _]
    (let [screen-data
          (get-in db [::db/screen-states ::st/transaction-screen-state])

          current-account
          (get-in db [::db/screen-states
                      ::sa/account-screen-state ::sa/account-id])

          new-transaction
          (st/screen-data->transaction current-account screen-data)]
      (-> db
          (update-in [:data :transactions]
                     #(t/add-transaction % new-transaction))
          (assoc :navigation :account)))))

(rf/reg-event-db
  :edit-transaction
  data-interceptors
  (fn [db [_ id]]
    (let [transaction (get-in db [:data :transactions id])
          current-account (get-in db [::db/screen-states
                                      ::sa/account-screen-state
                                      ::sa/account-id])]
      (-> db
          (assoc-in [::db/screen-states ::st/transaction-screen-state]
                    (st/transaction->screen-data current-account transaction))
          (assoc :navigation :transaction)))))

(rf/reg-event-fx
  :new-transaction
  [check-spec-interceptor
   (rf/inject-cofx :now)
   ; ->store
   ]
  (fn [cofx _]
    (let [db (:db cofx)
          now (:now cofx)
          id (inc (get-in db [:highest-ids :transaction]))]
      {:db (-> db
               (assoc-in [::db/screen-states ::st/transaction-screen-state]
                         (st/new-transaction id now 0))
               (assoc-in [:highest-ids :transaction] id)
               (assoc :navigation :transaction))})))

(rf/reg-event-db
  :close-transaction-screen
  data-interceptors
  (fn [db _]
    (-> db
        (update-in [::db/screen-states] dissoc ::st/transaction-screen-state)
        (assoc :navigation :account))))

(rf/reg-event-fx
  :navigate
  (fn [_ [_ target]]
    {:navigate target}))
