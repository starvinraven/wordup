(ns wordup.gameplay.round
  (:require [wordup.logic.board :as board]
            [wordup.logic.dict :as dict]))

(defn setup-round!
  [board-x board-y]
  (let [board (board/random-board board-x board-y)
        words (dict/get-words "resources/scrabble.txt")
        words-in-board (board/all-words-in-board board words)]
    {:board board :words-in-board words-in-board}))