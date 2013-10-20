(ns wordup.cljs
  (:use [jayq.core :only [$]]
        [jayq.util :only [log]]))

(defn ^:export init []
  (log "hello"))


