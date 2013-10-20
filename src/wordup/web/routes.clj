(ns wordup.web.routes
  (:use compojure.core)
  (:use org.httpkit.server)
  (:use wordup.web.handlers)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/api/v1/round" [] round-handler)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))