(ns rabbit-x.blabbr
  (:gen-class)
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.exchange  :as le]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]))

(def ^{:const true} default-exchange-name "")
(def ^{:const true} default-content "plain text")
(def ^{:const true} default-msgType "greeting" )
(def users ["joe" "aaron" "bob"])
(def data ["BOS 101, NYK 89" "ORL 85, ALT 88"])
(def ex "nba.scores")

(defn start-consumer
  "Starts a consumer bound to the given topic exchange in a separate thread"
  [ch topic-name username]
  (let [queue-name (format "%s" username)
        handler    (fn [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
                     (println (format "[consumer] %s received %s" username (String. payload "UTF-8"))))]
    (lq/declare ch queue-name :exclusive false :auto-delete true)
    (lq/bind    ch queue-name topic-name)
    (lc/subscribe ch queue-name handler :auto-ack true)))

(defmacro wrap-publish [default-exchange-name content mType data]
  `(doseq [datum# ~data]
     (lb/publish ~'ch ~'ex ~default-exchange-name datum# :content-type  ~content :type ~mType)))

(defn -main
  [& args]
  (let [conn  (rmq/connect)
        ch    (lch/open conn)
        ex    ex
        users users
        data data]
    (le/declare ch ex "fanout" :durable false :auto-delete true)
    (doseq [u users]
      (start-consumer ch ex u))
    (doseq [d data]
   (lb/publish ch ex "" d :content-type "text/plain" :type "scores.update"))
    (Thread/sleep 2000)
    (rmq/close ch)
    (rmq/close conn)))

(-main)
