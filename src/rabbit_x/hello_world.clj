(ns rabbit-x.hello-world
  (:gen-class)
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]))

(def ^{:const true} default-exchange-name "")
(def ^{:const true} default-content "plain text")
(def ^{:const true} default-msgType "greeting" )

(defn message-handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  (println (format "[consumer] Received a message: %s, delivery tag: %d, content type: %s, type: %s"
                   (String. payload "UTF-8") delivery-tag content-type type)))


(defn -main
  ([message] (-main message default-content default-msgType))
  ([message content-type message-type]
  (let [conn  (rmq/connect)
        ch    (lch/open conn)
        qname "langohr.examples.hello-world"]
    (println (format "[main] Connected. Channel id: %d" (.getChannelNumber ch)))
    (lq/declare ch qname :exclusive false :auto-delete true)
    (lc/subscribe ch qname message-handler :auto-ack true)
    (lb/publish ch default-exchange-name qname message :content-type content-type :type message-type)
    (Thread/sleep 2000)
    (println "[main] Disconnecting...")
    (rmq/close ch)
    (rmq/close conn))))

(-main "Who's your daddy?")
