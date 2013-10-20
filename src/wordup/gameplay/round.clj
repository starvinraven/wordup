(ns wordup.gameplay.round
  (:require [wordup.logic.board :as board]
            [wordup.logic.dict :as dict]
            [clojure.tools.logging :as log]))

(def current-round (atom {:id nil :status :ended}))

(defn setup-round!
  [board-x board-y]
  (let [board (board/random-board board-x board-y)
        words (dict/get-words "resources/scrabble.txt")
        words-in-board (board/all-words-in-board board words)]
    (reset! current-round
      {:id (str (java.util.UUID/randomUUID))
       :status :running
       :board board
       :words-in-board words-in-board
       :num-words-in-board (count words-in-board)})))