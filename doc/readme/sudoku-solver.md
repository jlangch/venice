# Example: Sudoku Solver


```clojure
(do
  (def board-1 [[7 8 0 4 0 0 1 2 0]
                [6 0 0 0 7 5 0 0 9]
                [0 0 0 6 0 1 0 7 8]
                [0 0 7 0 4 0 2 6 0]
                [0 0 1 0 5 0 9 3 0]
                [9 0 4 0 6 0 0 0 5]
                [0 7 0 3 0 0 0 1 2]
                [1 2 0 0 0 7 4 0 0]
                [0 4 9 2 0 6 0 0 7]])

  (def board-2 [[5 3 0 0 7 0 0 0 0]
                [6 0 0 1 9 5 0 0 0]
                [0 9 8 0 0 0 0 6 0]
                [8 0 0 0 6 0 0 0 3]
                [4 0 0 8 0 3 0 0 1]
                [7 0 0 0 2 0 0 0 6]
                [0 6 0 0 0 0 2 8 0]
                [0 0 0 4 1 9 0 0 5]
                [0 0 0 0 8 0 0 7 9]])

  (defn read-board [s]
    (vector* (->> (seq s)
                  (replace {#\. #\0})
                  (map #(- (long %) (long #\0)))
                  (partition 9)
                  (map vector*))))

  (defn read-boards [file]
    (->> (io/slurp-lines file)
         (map read-board)))

  (defn print-board [board]
    (println)
    (->> (postwalk-replace {0 "·"} board)
         (map #(flatten (interpose "|" (partition 3 %))))
         (partition 3)
         (interpose (seq "---+---+---"))
         (flatten)
         (partition 11)
         (docoll #(apply println %))))

  (defn first-empty-cell [board]
    (first (list-comp [x (range 9)
                       y (range 9)
                       :when (== 0 (get-in board [y x]))]
                      [x y])))

  (defn value-not-used? [val coll]
    (nil? (some #{val} coll)))

  (defn grid-3x3-vals [board x y]
    (let [xs  (-> x (/ 3) (* 3))
          ys  (-> y (/ 3) (* 3))]
      (list-comp [x1 (range xs (+ xs 3))
                  y1 (range ys (+ ys 3))]
        (get-in board [y1 x1]))))

  (defn possible? [board x y val]
    (and (== 0 (get-in board [y x]))                         ; cell [x y]
         (value-not-used? val (nth board y))                 ; row y
         (value-not-used? val (map #(nth % x) board))        ; col x
         (value-not-used? val (grid-3x3-vals board x y))))   ; 3x3 grid

  (defn solve [board]
    (if-let [[x y] (first-empty-cell board)]
      (list-comp [v (range 1 10) :when (possible? board x y v)]
                 (solve (assoc-in board [y x] v)))
      (print-board board)))


  (when-not (macroexpand-on-load?)
    (print-msg-box :warn
                   """
                   macroexpand-on-load is not activated. To get a better
                   performance activate it before loading this script.

                   From the REPL run the command: !macroexpand
                   """))

  (let [board board-1]
    (print-board board)
    (solve board)
    (println)))
```


Prints

```
7 8 · | 4 · · | 1 2 ·
6 · · | · 7 5 | · · 9
· · · | 6 · 1 | · 7 8
- - - + - - - + - - -
· · 7 | · 4 · | 2 6 ·
· · 1 | · 5 · | 9 3 ·
9 · 4 | · 6 · | · · 5
- - - + - - - + - - -
· 7 · | 3 · · | · 1 2
1 2 · | · · 7 | 4 · ·
· 4 9 | 2 · 6 | · · 7
```

with the solution

```
7 8 5 | 4 3 9 | 1 2 6
6 1 2 | 8 7 5 | 3 4 9
4 9 3 | 6 2 1 | 5 7 8
- - - + - - - + - - -
8 5 7 | 9 4 3 | 2 6 1
2 6 1 | 7 5 8 | 9 3 4
9 3 4 | 1 6 2 | 7 8 5
- - - + - - - + - - -
5 7 8 | 3 9 4 | 6 1 2
1 2 6 | 5 8 7 | 4 9 3
3 4 9 | 2 1 6 | 8 5 7
```
