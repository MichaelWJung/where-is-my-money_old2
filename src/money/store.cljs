(ns money.store)

(def store (atom nil))

(defprotocol Store
  (save [this data]))

(defn data->store
  [data]
  (let [s @store]
    (if (nil? s)
      (throw (ex-info "Store uninitialized" {}))
      (save s data))))
