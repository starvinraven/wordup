(ns wordup.cljs
  (:use [jayq.core :only [$ html]]
        [jayq.util :only [log]]))

(def round-websocket (atom nil))

(defn parse-json [x]
  (.parse (.-JSON js/window) x))

(defn handle-round-msg
  [d]
  (log "handle msg " d)
  (let [$container ($ "#board")]
    (log "$container " $container " board " (str (:board d)))
    (html $container (str (:board d)))))

(defn on-round-msg
  [msg]
  (let [data (.-data msg)
        d (js->clj (parse-json data) :keywordize-keys true)]
    (log "data", d)
    (handle-round-msg d)))

(defn ^:export init []
  (log "init")
  (reset! round-websocket (js/WebSocket. "ws://localhost:3000/api/v1/round"))
  (doall
    (map #(aset @round-websocket (first %) (second %))
      [["onopen" (fn [] (log "OPEN"))]
       ["onclose" (fn [] (log "CLOSE"))]
       ["onerror" (fn [e] (log "ERROR:" e))]
       ["onmessage" on-round-msg]])))





