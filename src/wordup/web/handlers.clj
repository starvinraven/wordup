(ns wordup.web.handlers
  (:use compojure.core)
  (:use org.httpkit.server)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defn round-handler [request]
  (with-channel request channel
    (send! channel "hello")
    (on-close channel (fn [status] (println "channel closed: " status)))
    (on-receive channel (fn [data] ;; echo it back
                          (send! channel data)))))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/api/v1/round" [] round-handler)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))