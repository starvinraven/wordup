(ns wordup.core
  (:gen-class)
  (:require [wordup.gameplay.round :as round]
            [wordup.logic.board :as board]
            [wordup.web.handlers :as handlers]
            [org.httpkit.server :as httpkit]))

(defn -main
  [& args]
  (println "running...")
  (let [round-data (round/setup-round! 4 4)
        top-words (take 100 (:words-in-board round-data))]
    (board/print-board! (:board round-data))
;    (doseq [word top-words]
;      (print (str word " ")))
    (httpkit/run-server handlers/app) {:port 3000}))



