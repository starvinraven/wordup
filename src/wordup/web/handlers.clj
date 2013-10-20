(ns wordup.web.handlers
  (:require [wordup.gameplay.round :as round]
            [clojure.data.json :as json])
  (:use org.httpkit.server)
  (:import (java.io PrintWriter)))

(extend java.lang.Character json/JSONWriter {:-write (fn [x ^PrintWriter out] (.print out (str "\"" x "\"")))})
(extend org.joda.time.DateTime json/JSONWriter {:-write (fn [x ^PrintWriter out] (.print out (str "\"" x "\"")))})

(defn on-round-change
  [channel _key _ref old-value new-value]
  (send! channel (json/write-str new-value)))

(defn round-handler [request]
  (with-channel request channel
    (send! channel (json/write-str @round/current-round))
    ; TODO proper client key instead of request
    (add-watch round/current-round "key" (partial on-round-change channel))
    (on-close channel (fn [status]
                        (remove-watch round/current-round "key")
                        (println "channel closed: " status)))
    (on-receive channel (fn [data] ;; echo it back
                          (send! channel data)))))
