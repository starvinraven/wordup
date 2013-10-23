(ns wordup.cljs
  (:use [jayq.core :only [$ html val show hide text]]
        [jayq.util :only [log]]
        [dommy.core :only [replace-contents!]])
  (:use-macros
    [dommy.macros :only [deftemplate sel1]]))

(def round-websocket (atom nil))
(def current-round (atom nil))
(def user-info (atom {:username nil :usedwords #{} :points 0}))

(defn to-json-string [m]
  (.stringify (.-JSON js/window) (clj->js m)))

(defn parse-json [x]
  (.parse (.-JSON js/window) x))

(deftemplate word-grid [board]
  (for [row board]
    [:div.row
      (for [letter row]
        [:div.col-xs-3.col-sm-3.col-md-3.text letter])]))

(defn reset-round
  [d]
  (let [grid (word-grid (:board d))
        element (sel1 :#word-grid)]
    (log "render board " (str (:board d)))
    (reset! current-round d)
    (replace-contents! element grid)))

(defn reset-score []
  (swap! user-info assoc-in [:usedwords] #{})
  (swap! user-info assoc-in [:points] 0))

(defn round-start
  [d]
  (log "round-start")
  (reset-score)
  (show ($ "#word-input"))
  (reset-round d))

(defn pause-start
  [d]
  (log "pause-start")
  (hide ($ "#word-input"))
  (reset-round d))

(defn update-score
  [d]
  (let [points-awarded (:points d)
        word (:word d)]
    (log "update-score" d " - " points-awarded)
    (when (> points-awarded 0)
      (log "lol?")
      (swap! user-info update-in [:points] #(+ points-awarded %))
      (swap! user-info update-in [:usedword] conj word)
      (log "updated score " (:points @user-info))
      (text ($ "#points") (:points @user-info)))))

(defn handle-round-msg
  [d]
  (condp = (:status d)
    "paused" (pause-start d)
    "running" (round-start d)
    "scored" (update-score d)))

(defn on-round-msg
  [msg]
  (let [data (.-data msg)
        d (js->clj (parse-json data) :keywordize-keys true)]
    (handle-round-msg d)))

(defn submit-word [word]
  (log "submit " word)
  (.send @round-websocket (to-json-string {:msgtype "word" :word (clojure.string/upper-case word) :round-id (:id @current-round)})))

(defn is-enter-or-space? [event]
  (let [keycode (or (aget event "which") (aget event "keyCode"))]
    (= keycode 13)))

(defn setup-input []
  (let [input-field ($ "#word-input")]
    (-> input-field
      (.asEventStream "keyup")
      (.filter is-enter-or-space?)
      (.map #(val input-field))
      (.doAction #(val input-field ""))
      (.onValue submit-word))))

(defn set-username []
  "FIXME, this can and will clash at some point"
  (let [username (str "anon" (rand-int 10000000))]
    (swap! user-info assoc-in [:username] username)))

(defn ^:export init []
  (log "init")
  (setup-input)
  (set-username)
  (reset! round-websocket (js/WebSocket. "ws://localhost:3000/api/v1/round"))
  (doall
    (map #(aset @round-websocket (first %) (second %))
      [["onopen" (fn [] (log "OPEN"))]
       ["onclose" (fn [] (log "CLOSE"))]
       ["onerror" (fn [e] (log "ERROR:" e))]
       ["onmessage" on-round-msg]])))





