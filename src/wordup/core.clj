(ns wordup.core
  (:gen-class)
  (:require [wordup.gameplay.round :as round]
            [wordup.logic.board :as board]
            [wordup.web.handlers :as handlers]
            [org.httpkit.server :as httpkit]
            [clojure.tools.logging :as log]
            [clj-logging-config.log4j :as log-config]))

(defn init-logging
  []
  ;(log-config/set-logger! :pattern "%d %-5p %c: %m%n" :level :debug)
  (log-config/set-logger!)
  (log/info "logging initialized"))

(defn init-round
  []
  (log/info "initializing round...")
  (let [round-data (round/setup-round! 4 4)
        current-board (:board round-data)
        words-in-board (:words-in-board round-data)
        top-words (take 100 words-in-board)]
    (log/info (str "init complete, " (:num-words-in-board round-data) " words in board"))
    (board/log-board! current-board)
    (log/info top-words)))

(defn init-app
  []
  (init-logging)
  (init-round))

(defn -main
  [& args]
  (init-round)
  (httpkit/run-server handlers/app) {:port 3000})
