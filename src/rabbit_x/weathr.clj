(ns rabbit-x.weathr
  (:gen-class)
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.exchange  :as le]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]))

(def ^{:const true}
  weather-exchange "weathr")

(def locations{""               "americas.north.#"
                   "americas.south" "americas.south.#"
                   "us.california"  "americas.north.us.ca.*"
                   "us.tx.austin"   "#.tx.austin"
                   "it.rome"        "europe.italy.rome"
                   "asia.hk"        "asia.southeast.hk.#"})

(def updates {"San Diego update" "americas.north.us.ca.sandiego"
    "Berkeley update"  "americas.north.us.ca.berkeley"
    "SF update"        "americas.north.us.ca.sanfrancisco"
    "NYC update"       "americas.north.us.ny.newyork"
    "São Paolo update" "americas.south.brazil.saopaolo"
    "Hong Kong update" "asia.southeast.hk.hongkong"
     "Kyoto update"     "asia.southeast.japan.kyoto"
   "Shanghai update"  "asia.southeast.prc.shanghai"
   "Rome update"      "europe.italy.roma"
   "Paris update"     "europe.france.paris"})

(defn start-consumer
  "Starts a consumer bound to the given topic exchange in a separate thread"
  [ch topic-name queue-name]
  (let [queue-name' (.getQueue (lq/declare ch queue-name :exclusive false :auto-delete true))
        handler     (fn [ch {:keys [routing-key] :as meta} ^bytes payload]
                      (println (format "[consumer] Consumed '%s' from %s, routing key: %s" (String. payload "UTF-8") queue-name' routing-key)))]
    (lq/bind    ch queue-name' weather-exchange :routing-key topic-name)
    (lc/subscribe ch queue-name' handler :auto-ack true)))

(defn publish-update
  "Publishes a weather update"
  [ch payload routing-key]
  (lb/publish ch weather-exchange routing-key payload :content-type "text/plain" :type "weather.update"))

(defn -main
  [& args]
  (let [conn      (rmq/connect)
        ch        (lch/open conn)
        locations locations
        updates updates]
    (le/declare ch weather-exchange "topic" :durable false :auto-delete true)
    (doseq [[k v] locations]
      (start-consumer ch v k))
    (doseq [[x y] updates]
    (publish-update ch x y))
    (Thread/sleep 2000)
    (rmq/close ch)
    (rmq/close conn)))

(-main)
