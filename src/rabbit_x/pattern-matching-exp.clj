;; Pattern matching experiment!!

(use '[clojure.core.match :only (match)])

(let [x 4 y 6 z 9]
  (match [x y z]
         [(:or 1 2 3) _ _] :a
         [4 (:or 5 6 7) _] :b
         :else nil))

(match [1 2 3]
  [(_ :guard #(odd? %)) _ (_ :guard even?)] :a1
  [(_ :guard #(odd? %)) _ _] :a2
  :else :a4)

(match [(java.util.Date. 2010 10 1 12 30)]
  [{:year 2009 :month a}] :a
  [{:year (:or 2010 2011) :month b}] :b
  :else :no-match)

(let [x true
      y true
      z true]
  (match [x y z]
    [_ false true] 1
    [false true _ ] 2
    [_ _ false] 3
    [_ _ true] 4
    :else 5))

(let [v [1 2 4]
      x true]
  (match [v x]
    [[_ 2 2] true] 1
    [[_ 2 3] false] 2
    [[1 2 4] _] 3
    :else 4))

(let [x {:a 1 :b 1 :c 3}
      y 1
      z 1]
(match [x]
[{:a y :b z}] true
  :else 4))


;; test data
(def users {:Jesse {:pass "test" :id "Jesse"}})


;;helpers
(defn mock-user [id pass] {:id id :pass pass})

(defn prep-validation [id pass]
  (let [user (mock-user id pass)]
    user))


;; simple if version
(defn valid? [id pass]
  (let [user (prep-validation id pass)]
    (if (and (= (get Jesse :id) id) (= (get Jesse :db-pass) pass)) true false)))

            ;;tests
            (valid? "Jesse" "test")
            (valid? "Jeremy" "test")
            (valid? "Jesse" "tets")


;; pattern matching version
(defn matches? [id pass]
  (cond (contains? users (keyword id))
        (let [user (prep-validation id pass)
              db-name ((keyword id) users)
              db-id (db-name :id)
              db-pass (get db-name :pass)
              db-user {:pass db-pass :id db-id}]
          (match [user]
                 [db-user] true
                 :else false))
        :else
        false))

            ;;tests
            (matches? "Jesse" "test")
            (matches? "Jeremy" "test")
            (matches? "Jesse" "tets")

