(ns money.core.utils)

(defn remove-first [pred coll]
  (let [[m n] (split-with (comp not pred) coll)]
    (concat m (rest n))))
