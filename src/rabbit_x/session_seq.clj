(ns rabbit-x.session-seq
(:require [rabbit-x.rails-log :as log]
          [rabbit-x.data-anal :as data]
          [rabbit-x.analyzer :as analyzer]))

(defn session-seq [requests]
  (group-by analyzer/session-id requests))

(defn duration [requests]
  (let [begin (log/time-of-request (first requests))
        end (log/time-of-request (last requests))]
    (- (.getMillis end) (.getMillis begin))))

(defn parse-session [[session-id requests]]
  (let [metrics {:length (count requests)
                 :duration (duration requests)}]
    [[session-id metrics]]))

(defn averages [collected-values]
  (let [num-sessions (count collected-values)
        all-metrics (apply concat (vals collected-values))
        total-length (apply + (map :length all-metrics))
        total-duration (apply + (map :duration all-metrics))]
    {:average-length (/ total-length num-sessions)
     :average-duration (/ total-duration num-sessions)}))

(defn investigate-sessions [filename]
(let [results (data/map-reduce parse-session averages
(session-seq (log/request-seq filename)))]
(println "Avg length:" (* 1.0 (:average-length results)))
(println "Avg duration:" (* 1.0 (:average-duration results)))))

(def rl (log/request-seq "resources/rails.txt"))
(def ss (session-seq rl))

;; Why don't these work as expected?
(parse-session (second ss))
(investigate-sessions "resources/rails.txt")
