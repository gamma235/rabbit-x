(ns rabbit-x.analyzer
(:require [rabbit-x.rails-log :as log]
      [rabbit-x.data-anal]))

(defn parse-record [log-record]
  (let [data {:total 1}
        data (assoc data (log/controller-name log-record) 1)]
    [[(log/day-of-request-str log-record) data]]))

(defn reduce-days [[date date-vals]]
  {date (apply merge-with + date-vals)})

(defn rails-reducer [collected-values]
  (apply merge (map reduce-days collected-values)))

(defn investigate-log [log-file]
  (map-reduce parse-record rails-reducer (log/request-seq log-file)))

(defn session-id [log-record]
  (second (.split (second log-record) ": ")))
