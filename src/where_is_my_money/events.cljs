(ns where-is-my-money.events
  (:require [re-frame.core :as rf :refer [reg-event-db]]
            [where-is-my-money.db :refer [default-db]]))

(defn allocate-next-id
  [items]
  ((fnil inc 0) (last (keys items))))

(reg-event-db
 :initialize-db
 (fn [_ _]
   default-db))

(reg-event-db
 :set-text
 (fn [db [_ text]]
   (assoc db :text text)))

(reg-event-db
 :add-item
 (fn [db _]
   (let [items (:items db)
         text (:text db)
         id (allocate-next-id items)]
     (assoc-in db [:items id] {:id id :text text}))))

(reg-event-db
 :delete-item
 (fn [db [_ id]]
   (update db :items dissoc id)))
