(ns money.events
  (:require [clojure.spec.alpha :as s]
            [money.db :as db]
            [money.navigation :refer [!navigation]]
            [money.core.account :as a]
            [money.core.adapters.account :as aa]
            [money.core.screens.account :as sa]
            [money.core.screens.transaction :as st]
            [money.core.transaction :as t]
            [re-frame.core :as rf]
            ["react-native" :refer [ToastAndroid]]
            ["@react-navigation/native" :refer [CommonActions StackActions]]
            ["@react-native-async-storage/async-storage" :as AsyncStorage]))

(def async-storage (aget AsyncStorage "default"))

(defn check-and-throw
  "Throws an exception if `db` doesn’t match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (println "db: " db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

(def check-spec-interceptor (rf/after (partial check-and-throw :money.db/db)))

(def ->store
  (rf/->interceptor
    :id :->store
    :after (fn [context]
             (rf/assoc-effect context :persist (rf/get-effect context :db)))))

(def transaction-interceptors [check-spec-interceptor
                               ->store
                               (rf/path ::db/data ::t/transactions)])

(def transaction-screen-interceptors
  [check-spec-interceptor
   ->store
   (rf/path ::db/screen-states ::st/transaction-screen-state)])

(def data-interceptors [check-spec-interceptor
                        ->store])

(rf/reg-fx
  :navigation
  (fn [[action target]]
    (let [^js navigation @!navigation]
      (case action
        :go-back (.dispatch navigation (.goBack CommonActions))
        :navigate (.dispatch
                    navigation
                    (.reset CommonActions
                            (clj->js
                              {:type "stack"
                               :key "stack-1"
                               :routeNames ["Loading"
                                            "Home"
                                            "Account-Overview"
                                            "Transaction"]
                               :routes
                               (case target
                                 ::db/home-screen
                                 [{:key "home-1"
                                   :name "Home"}]

                                 ::db/account-overview
                                 [{:key "home-1"
                                   :name "Home"}
                                  {:key "account-overview-1"
                                   :name "Account-Overview"}]

                                 ::db/transaction-screen
                                 [{:key "home-1"
                                   :name "Home"}
                                  {:key "account-overview-1"
                                   :name "Account-Overview"}
                                  {:key "transaction-1"
                                   :name "Transaction"}]
                                 )})))))))

(rf/reg-fx
 :persist
 (fn [db]
   (let [serialized (str (dissoc db ::db/startup))]
     (-> async-storage
         (.setItem "db" serialized)
         (.catch #(prn "Error: " %))))))

(rf/reg-fx
  :show-toast
  (fn [message]
    (.show ToastAndroid message (.-SHORT ToastAndroid))))

(rf/reg-cofx
 :now
 (fn [cofx _data]
   (assoc cofx :now (.now js/Date))))

(rf/reg-event-db
  :record-navigating-back
  [check-spec-interceptor]
  (fn [db _]
    (let [source (::db/navigation db)]
      (assoc db ::db/navigation
             (case source
               ::db/account-overview ::db/home-screen
               ::db/transaction-screen ::db/account-overview)))))

(rf/reg-event-fx
  :navigate-to
  [check-spec-interceptor]
  (fn [{:keys [db]} [_ target]]
    (let [source (::db/navigation db)
          fx (case [source target]
               [::db/home-screen ::db/account-overview]
               [[:navigation [:navigate ::db/account-overview]]]

               [::db/account-overview ::db/transaction-screen]
               [[:navigation [:navigate ::db/transaction-screen]]]

               [::db/transaction-screen ::db/account-overview]
               [[:navigation [:go-back]]]

               [::db/account-overview ::db/home-screen]
               [[:navigation [:go-back]]]
               )]
      (prn :navigate-to " Source: " source ", Target: " target)
      {:db (assoc db ::db/navigation target)
       :fx fx})))

(rf/reg-event-db
 :initialize-db
 [check-spec-interceptor]
 (fn [_ _]
   db/default-db))

(rf/reg-event-db
  :load-db
  [check-spec-interceptor]
  (fn [db [_ stored]]
    (merge db stored)))

(rf/reg-event-fx
 :set-account
 data-interceptors
 (fn [{:keys [db]} [_ account-idx]]
   (let [accounts (get-in db [::db/data ::a/accounts])]
     {:db (assoc-in db
                    [::db/screen-states ::sa/account-screen-state ::sa/account-id]
                    (aa/account-idx->id accounts account-idx))
      :fx [[:dispatch [:navigate-to ::db/account-overview]]]})))

(rf/reg-event-db
 :remove-transaction
 transaction-interceptors
 (fn [transactions [_ id-to-remove]]
   (t/remove-transaction-by-id transactions id-to-remove)))

(rf/reg-event-db
 :update-transaction-date
 transaction-screen-interceptors
 (fn [screen-state [_ date]]
   (st/update-transaction-date screen-state date)))

(rf/reg-event-db
  :update-transaction-amount
  transaction-screen-interceptors
  (fn [screen-state [_ amount]]
    (st/update-amount screen-state amount)))

(rf/reg-event-db
  :update-transaction-description
  transaction-screen-interceptors
  (fn [screen-state [_ description]]
    (st/update-description screen-state description)))

(rf/reg-event-db
  :update-transaction-account
  data-interceptors
  (fn [db [_ new-account]]
    (let [accounts (get-in db [::db/data ::a/accounts])]
      (update-in
        db
        [::db/screen-states ::st/transaction-screen-state]
        #(st/update-account % accounts new-account)))))

(rf/reg-event-db
 :update-transaction-data
 data-interceptors
 (fn [db [_ transaction-data]]
   (if-not (contains? (::db/screen-states db) ::st/transaction-screen-state)
     db
     (let [accounts (get-in db [::db/data ::a/accounts])]
       (try
         (update-in db
                    [::db/screen-states ::st/transaction-screen-state]
                    #(st/update-screen % accounts transaction-data))
         (catch ExceptionInfo e
           (prn (ex-data e))
           db))))))

(rf/reg-event-fx
 :save-transaction
 data-interceptors
 (fn [{:keys [db]} _]
   (let [screen-data
         (get-in db [::db/screen-states ::st/transaction-screen-state])

         current-account
         (get-in db [::db/screen-states
                     ::sa/account-screen-state ::sa/account-id])

         new-transaction
         (st/screen-data->transaction current-account screen-data)]
     {:db (-> db
              (update-in [::db/data ::t/transactions]
                         #(t/add-transaction % new-transaction))
              (assoc ::db/navigation :account))
      :fx [[:dispatch [:navigate-to ::db/account-overview]]]})))

(rf/reg-event-fx
 :edit-transaction
 data-interceptors
 (fn [{:keys [db]} [_ id]]
   (let [transaction (get-in db [::db/data ::t/transactions id])
         current-account (get-in db [::db/screen-states
                                     ::sa/account-screen-state
                                     ::sa/account-id])]
     {:db (assoc-in db
                    [::db/screen-states ::st/transaction-screen-state]
                    (st/transaction->screen-data current-account transaction))
      :fx [[:dispatch [:navigate-to ::db/transaction-screen]]]})))

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
              (assoc-in [:highest-ids :transaction] id))
      :fx [[:dispatch [:navigate-to ::db/transaction-screen]]]})))

(rf/reg-event-db
 :close-transaction-screen
 data-interceptors
 (fn [db _]
   (-> db
       (update-in [::db/screen-states] dissoc ::st/transaction-screen-state)
       (assoc ::db/navigation :account))))

(rf/reg-event-fx
 :ui-ready
 [check-spec-interceptor]
 (fn [{:keys [db]} _]
   {:db (assoc-in db [::db/startup ::db/ui-ready?] true)
    :fx [[:dispatch [:check-startup]]]}))

(rf/reg-event-fx
 :db-ready
 [check-spec-interceptor]
 (fn [{:keys [db]} _]
   {:db (assoc-in db [::db/startup ::db/db-ready?] true)
    :fx [[:dispatch [:check-startup]]]}))

(rf/reg-event-fx
 :check-startup
 [check-spec-interceptor]
 (fn [{:keys [db]} _]
   (let [ui-ready? (get-in db [::db/startup ::db/ui-ready?])
         db-ready? (get-in db [::db/startup ::db/db-ready?])]
     (if (and ui-ready? db-ready?)
       {:db db
        :fx [[:navigation [:navigate (::db/navigation db)]]]}
       {:db db}))))

(rf/reg-event-fx
  :toast
  (fn [{:keys [db]} [_ message]]
    {:db db
     :fx [[:show-toast message]]}))
