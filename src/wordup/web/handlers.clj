(ns wordup.web.handlers
  (:use org.httpkit.server))

(defn round-handler [request]
  (with-channel request channel
    (send! channel "hello")
    (on-close channel (fn [status] (println "channel closed: " status)))
    (on-receive channel (fn [data] ;; echo it back
                          (send! channel data)))))
