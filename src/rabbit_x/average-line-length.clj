(ns chapter-data.average-line-length
  (:use rabbit-x.data-anal))

(def IGNORE "_")

(defn- parse-line [line]
  (let [tokens (.split (.toLowerCase line) " ")]
    [[IGNORE (count tokens)]]))

(defn average [numbers]
  (/ (apply + numbers)
     (count numbers)))

(defn reducer [combined]
  (average (val (first combined))))

(defn average-line-length [filename]
  (map-reduce parse-line reducer (read-lines filename)))

(average-line-length "resources/test2.txt")

