(ns wordup.gameplay.round
  (:require [wordup.logic.board :as board]
            [wordup.logic.dict :as dict]
            [clojure.tools.logging :as log]
            [clj-time.core :as clj-time]
            [overtone.at-at :as at-at]))

(def current-round (atom {:id nil :status :paused :scores {}}))
(def ROUND_DURATION_SECS 30)
(def PAUSE_DURATION_SECS 5)
(def thread-pool (at-at/mk-pool))

(defn start-pause!
  []
  (log/info "start-pause!")
  (let [start-time (clj-time/now)
        end-time (clj-time/plus start-time (clj-time/secs PAUSE_DURATION_SECS))]
    (swap! current-round merge
      {:status :paused
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
       :num-words-in-board num-words-in-board
       :start-time start-time
       :ends-at end-time
       :duration ROUND_DURATION_SECS})))


(defn on-round-change
  [_key _ref old-value new-value]
  (let [new-status (:status new-value)
        old-status (:status old-value)
        end-time (:end-time new-value)
        change-after (* 1000 (:duration new-value))]
    (when (not= new-status old-status)
      (log/info "round status changed to " new-status ", scheduling new status. id now" (:id new-value))
      (condp = new-status
        :paused (at-at/after change-after start-round! thread-pool)
        :running (at-at/after change-after start-pause! thread-pool)))))

(add-watch current-round :rounds on-round-change)

(defn score-word
  "TODO fix probable concurrency issues (use STM?)"
  [word user round-id]
  (log/info "round" round-id (:id @current-round))
  (if (and
        (log/spy :info (= (:id @current-round) round-id))
        (log/spy :info (contains? (:words-in-board @current-round) word))
        (log/spy :info ((complement contains?) (get-in @current-round [:scores user :words]) word)))
    (let [score (board/score-word word)]
      (swap! current-round update-in [:scores user :words] #(set (conj % word)))
      (swap! current-round update-in [:scores user :score] #(+ score (or % 0)))
      (log/info "score for user now" (get-in @current-round [:scores user]))
      score)
    0))
