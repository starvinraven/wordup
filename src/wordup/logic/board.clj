(ns wordup.logic.board
  (:require [clojure.set :as set])
  (:use [clojure.tools.logging :only (spy)]))

(def letters-list (map char (concat (range 65 91))))

(defn random-letter
  []
  (rand-nth letters-list))

(defn random-row
  [length]
  (-> []
    (into (repeatedly length random-letter))))

(defn board-width
  [board]
  (count (first board)))

(defn board-height
  [board]
  (count board))

(defn std-board
  [letters]
  {:pre (= (16 (count letters)))}
    (partition 4 letters))

(defn random-board
  [x y]
  (-> []
    (into (repeatedly y #(random-row x)))))

(defn str-board
  [board]
  (apply str
    (interpose "\n"
      (for [row board]
        (apply str (interpose " " row))))))

(defn- conj-if
  [coll test item]
  (if test (conj coll item) coll))

(defn is-valid-coords?
  [board xy]
  (let [x (first xy)
        y (last xy)]
    (and
      (>= x 0)
      (>= y 0)
      (< x (board-height board))
      (< y (board-width board)))))

(defn neighboring-cells
  [board xy]
  (let [x (first xy)
        y (last xy)
        xs [x (dec x) (inc x)]
        ys [y (dec y) (inc y)]
        unfiltered-coords (set (for [xcoord xs
                                     ycoord ys]
                                 (vector xcoord ycoord)))]
    (set (filter #(is-valid-coords? board %) (set/difference unfiltered-coords #{xy})))))

(defn letter-at
  [board xy]
  (let [xcoord (first xy)
        ycoord (last xy)]
    (nth (nth board ycoord) xcoord)))

(defn board-to-map
  [board]
  (let [keyed-seq (for [ycoord (range (board-height board))
                        xcoord (range (board-width board))]
                    (let [letter (letter-at board [xcoord ycoord])]
                      {[xcoord ycoord] letter}))]
    (apply merge keyed-seq)))

(defn cells-for-letter
  [board letter]
  (apply hash-set (filter (complement nil?)
    (for [y (range (board-height board))
          x (range (board-width board))]
      (let [this-letter (letter-at board [x y])]
        (if (= this-letter letter) [x y] nil))))))

(defn contains-word?
    [board word]
    (let [innerloop
      (fn innerloop [board word i allowed-cells used-cells]
        (let [letter (nth word i)
              cells-with-letter (cells-for-letter board letter)
              usable-cells-with-letter (set/intersection allowed-cells (set/difference cells-with-letter used-cells))]
          (if (empty? usable-cells-with-letter)
            false
            (if (= i (dec (count word)))
              true
              (some true? (for [cell-to-check usable-cells-with-letter]
                              (innerloop board word (inc i) (neighboring-cells board cell-to-check) (conj used-cells cell-to-check))))))))]
      (innerloop board word 0 (set (keys (board-to-map board))) #{})))

(defn all-words-in-board
  [board]
  ; TODO
  )

(defn print-board!
  [board]
  (print (str-board board)))

; (ns wordup.logic.board)
; (use 'wordup.logic.dict)
; (load-dict! "resources/words.txt")
; (contains-word? (partition 4 "PPGRAINENTRAWACH") "PANTAGRAPHIC")
; (sort #(> (count %1) (count %2)) (filter #(contains-word? (partition 4 "PPGRAINENTRAWACH") %) @words))