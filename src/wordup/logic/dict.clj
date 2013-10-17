(ns wordup.logic.dict
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(def words (atom (sorted-set)))

(defn load-dict!
  [filename]
  (with-open [rdr (io/reader filename)]
    (doseq [line (line-seq rdr)]
      (swap! words conj (string/upper-case line)))))

(defn in-dict?
  [word]
  (contains? @words (string/upper-case word)))



