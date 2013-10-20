(ns wordup.gameplay.round
  (:require [wordup.logic.board :as board]
            [wordup.logic.dict :as dict]
            [clojure.tools.logging :as log]
            [clj-time.core :as clj-time]
            [overtone.at-at :as at-at]))

(def current-round (atom {:id nil :status :paused}))
(def ROUND_DURATION_SECS 30)
(def PAUSE_DURATION_SECS 5)
(def thread-pool (at-at/mk-pool))

(defn start-pause!
  [ended-round]
  (log/info "start-pause!")
  (let [start-time (clj-time/now)
        end-time (clj-time/plus start-time (clj-time/secs PAUSE_DURATION_SECS))]
    (reset! current-round
      {:id (:id ended-round)
       :status :paused
       :board (:board ended-round)
       :words-in-board (:words-in-board ended-round)
       :num-words-in-board (count (:words-in-board ended-round))
       :start-time start-time
       :ends-at end-time
       :duration PAUSE_DURATION_SECS})))

(defn start-round!
  []
  (log/info "start-round!")
  (let [id (str (java.util.UUID/randomUUID))
        board (board/random-board 4 4)
        words (dict/get-words "resources/scrabble-short.txt")
        words-in-board (board/all-words-in-board board words)
        num-words-in-board (count words-in-board)
        start-time (clj-time/now)
        end-time (clj-time/plus start-time (clj-time/secs ROUND_DURATION_SECS))]
    (log/info "start-round! complete, round id " id ", " num-words-in-board " words in board")
    (board/log-board! board)
    (log/info (take 100 words-in-board))
    (reset! current-round
      {:id id
       :status :running
       :board board
       :words-in-board words-in-board
       :num-words-in-board num
       :start-time start-time
       :ends-at end-time
       :duration ROUND_DURATION_SECS})))


(defn on-round-change
  [_key _ref old-value new-value]
  (let [new-status (:status new-value)
        end-time (:end-time new-value)
        change-after (* 1000 (:duration new-value))]
    (log/info "round status changed to " new-status ", scheduling new status")
    (condp = new-status
      :paused (at-at/after change-after start-round! thread-pool)
      :running (at-at/after change-after (partial start-pause! old-value) thread-pool))))

(add-watch current-round :rounds on-round-change)
