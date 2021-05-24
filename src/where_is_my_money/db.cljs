(ns where-is-my-money.db)

(def default-db
  {:items (sorted-map 1 {:id 1 :text "Milk"}
                      2 {:id 2 :text "Eggs"}
                      3 {:id 3 :text "Bread"}
                      4 {:id 4 :text "Juice"}
                      5 {:id 5 :text "Chips"}
                      6 {:id 6 :text "Käse"})
   :text ""})
