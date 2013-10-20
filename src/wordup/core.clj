(ns wordup.core
  (:gen-class)
  (:require [wordup.gameplay.round :as round]
            [wordup.logic.board :as board]
            [wordup.web.routes :as routes]
            [org.httpkit.server :as httpkit]
            [clojure.tools.logging :as log]
            [clj-logging-config.log4j :as log-config]))

(defn init-logging
  []
  ;(log-config/set-logger! :pattern "%d %-5p %c: %m%n" :level :debug)
  (log-config/set-logger!)
  (log/info "logging initialized"))

(defn init-app
  []
  (init-logging)
  (round/start-round!))

(defn start-server!
  []
  (init-app)
  (httpkit/run-server routes/app {:port 3000}))

(defn -main
  [& args]
  (start-server!))
