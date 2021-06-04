(ns money.test-runner
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [run-all-tests]]
            [money.core.account-test]
            [money.core.adapters.account-test]
            [money.core.currency-test]
            [money.core.presenters.account-presenter-test]
            [money.core.presenters.transaction-presenter-test]
            [money.core.screens.transaction-test]
            [money.core.transaction-test]
            [money.core.utils-test]))

(defn -main []
  (s/check-asserts true)
  (run-all-tests #"^money\..*"))
