(ns wordup.cljs
  (:use [jayq.core :only [$ html val show hide text empty add-class remove-class]]
        [jayq.util :only [log]]
        [dommy.core :only [replace-contents! append!]])
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

(deftemplate used-word [word points]
  [:li {:id word :class (if (> points 0) "points" "no-points")}
   [:span.word word]
   [:span.word-points points]])

(deftemplate user-score [username points]
  [:li (str username ": "points)])

(defn reset-input
  []
  (val ($ "#word-input") ""))

(defn reset-words
  []
  (empty ($ "#used-words")))

(defn reset-round
  [d]
  (let [grid (word-grid (:board d))
        element (sel1 :#word-grid)]
    (log "render board " (str (:board d)))
    (reset! current-round d)
    (replace-contents! element grid)
    (reset-input)))

(defn render-score [& [word word-points]]
  (text ($ "#points") (:points @user-info))
  (when word
    (append! (sel1 :#used-words) (used-word word (or word-points 0)))))

(defn reset-score []
  (swap! user-info assoc-in [:usedwords] #{})
  (swap! user-info assoc-in [:points] 0)
  (render-score))

(defn sorted-rankings [scores]
  (into (sorted-map-by (fn [key1 key2]
                         (compare
                           [(get-in scores [key2 :score]) key2]
                           [(get-in scores [key1 :score]) key1])))
    scores))

(defn render-ranking [scores]
  (let [sorted-scores (sorted-rankings scores)
        ranking-list (sel1 :#ranking)]
    (empty ($ "#ranking"))
    (doseq [score sorted-scores]
      (let [username (name (key score))
            points (:score (clojure.core/val score))]
        (append! ranking-list (user-score username points))))))

(defn render-words-in-board [words]
  (let [words-list (sel1 :#words-in-board )]
    (empty ($ "#words-in-board"))
    (doseq [word-and-points words]
      (let [word (first word-and-points)
            points (second word-and-points)]
        (append! words-list (used-word word points))))))

(defn round-start
  [d]
  (log "round-start")
  (reset-score)
  (reset-words)
  (show ($ "#word-input"))
  (add-class ($ ".results") "hidden")
  (reset-round d))

(defn pause-start
  [d]
  (log "pause-start")
  (hide ($ "#word-input"))
  (render-ranking (:scores d))
  (render-words-in-board (:words-in-board-with-points d))
  (remove-class ($ ".results") "hidden")
  (reset-round d))

(defn update-score
  [d]
  (let [points-awarded (:points d)
        word (:word d)]
    (when (> points-awarded 0)
      (swap! user-info update-in [:points] #(+ points-awarded %))
      (swap! user-info update-in [:usedword] conj word)
      (render-score word points-awarded))))

(defn handle-round-msg
  [d]
  (log "got message " d)
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
  (.send @round-websocket (to-json-string {:msgtype "word" :word (clojure.string/upper-case word) :round-id (:id @current-round) :user (:username @user-info)})))

(defn is-enter-or-space? [event]
  (let [keycode (or (aget event "which") (aget event "keyCode"))]
    (= keycode 13)))

(defn setup-input []
  (let [input-field ($ "#word-input")]
    (-> input-field
      (.asEventStream "keyup")
      (.filter is-enter-or-space?)
      (.map #(val input-field))
      (.doAction reset-input)
      (.onValue submit-word))))

(defn set-username []
  "FIXME, this can and will clash at some point"
  (let [username (str "anon" (rand-int 10000000))]
    (swap! user-info assoc-in [:username] username)
    (text ($ "#username") username)))

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
