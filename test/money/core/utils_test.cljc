(ns money.core.utils-test
  (:require [clojure.test :refer [deftest is are testing run-tests]]
            [money.core.utils :as u]))

(deftest remove-first
  (are [pred coll expected]
       (= (vec (u/remove-first pred coll)) expected)

       zero? [0] []
       zero? [0 0] [0]
       (partial = 8) [1 2 8 3 4] [1 2 3 4]
       (partial = 8) [8 1 2 8 3 4] [1 2 8 3 4]))
