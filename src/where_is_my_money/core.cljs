(ns where-is-my-money.core
  (:require [clojure.walk]
            [reagent.core :as r]
            [re-frame.core :as rf :refer [dispatch dispatch-sync subscribe]]
            [where-is-my-money.events]
            [where-is-my-money.subs]
            [where-is-my-money.views :refer [hello]]))

(dispatch-sync [:initialize-db])

(defn ^:export -main [& args]
  ; (rf/clear-subscription-cache!)
  (r/as-element [hello]))
