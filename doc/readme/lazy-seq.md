# Lazy Sequences

Venice supports lazy evaluated sequences. Lazy sequences are sequences which elements
are produced only when needed and memorized for further access, thus lazy sequences
can be infinite. The evaluation of sequence elements is called realization.



## Producing Lazy Sequences

### Lazy sequences produced by an element generating function

(theoretically) infinite lazy sequence with random numbers

```clojure
(lazy-seq rand-long) ; => (...)
 ```
 
 
(theoretically) infinite lazy sequence with positive numbers

```clojure
; 1, 2, 3, 4, ...
(lazy-seq 1 inc) ; => (...)
 ```


(theoretically) infinite lazy sequence with cons'ing a value

```clojure
; -1, 0, 1, 2, 3, 4, ...
(cons -1 (lazy-seq 0 inc)) ; => (...)
 ```


### Finite Lazy Sequences

Empty lazy sequence

```clojure
(lazy-seq) ; => (...)

(empty? (lazy-seq)) ; => true
 ```

Finite lazy sequence from lists and vectors

```clojure
; 1, 2, 3, 4
(lazy-seq '(1 2 3 4)) ; => (...)
 ```

```clojure
; 1, 2, 3, 4
(lazy-seq [1 2 3 4]) ; => (...)
 ```

Finite lazy sequence from a function returning `nil` to end the sequence

```clojure
; 1, 2, 3, 4, 5
(lazy-seq 1 #(if (< % 5) (inc %) nil)) ; => (...)
```



## Realizing Lazy Sequences

Lazy sequences must be explicitly realized. At this moment the elements are
computed.

Single elements of a lazy sequence can be realized with one of the functions 
`first`, `second`, `third`, `fourth`, or `nth`

```clojure
(first (lazy-seq 1 inc)) ; => 1

(second (lazy-seq 1 inc)) ; => 2

(first (cons -1 (lazy-seq 0 #(+ % 1)))) ; => -1 

(second (cons -1 (lazy-seq 0 #(+ % 1)))) ; => 0 
 ```
 

Realizing a lazy sequence to a list is done by applying the `doall` function. 

**Be aware that your runtime system will not survive realizing an infinite lazy sequence.**

```clojure
;;; !!! DO NOT RUN THIS !!!
(doall (lazy-seq 1 inc)) ; continues realizing elements until the memory is exhausted
 ```

Realizing finite lazy sequences

```clojure
(->> (lazy-seq rand-long)  ; infinite lazy seq producing random numbers
     (take 4)              ; finite lazy seq with 4 elements not yet realized
     (doall))              ; realize the 4 elements
     
; => (1818406514169153152 8927930230538774116 713188723202483350 1539851250757480188)
```

```clojure
(->> (lazy-seq 1 inc)      ; infinite lazy seq of positive numbers
     (map #(* 10 %))       ; map elements, no elements realized yet
     (drop 2)              ; drop the first 2 elements producing a new infinite lazy seq
     (take 2)              ; finite lazy seq with 2 elements not yet realized
     (doall))              ; realize the 2 elements
     
; => (30 40)
```

Implicitly realizing elements of a lazy sequence

```clojure
(interleave [:a :b :c] (lazy-seq 1 inc))  ; => (:a 1 :b 2 :c 3)
```


### Realizing Finite Lazy Sequences

Finite lazy sequences are built from element producing functions (eg.: `#(if (< % 5) (inc %) nil)`) returning `nil` to end the sequence. 

```clojure
(doall (lazy-seq 1 #(if (< % 5) (inc %) nil)))
; => (1 2 3 4 5)
```

```clojure
(->> (lazy-seq 1 #(if (< % 5) (inc %) nil))
     (drop 2)
     (take 6)
     (doall))
; => (3 4 5)
```

Note that the producing function receives the last element as input to produce the next element. This is why the function `#(if (< % 5) (inc %) nil)` produces the elements `..,3,4,5` (up to 5). The last input element that matches the expression `(< % 5)` is 4, hence 5 is the last produced element (It's simply a producing function and not a filter).


## Implicit Memoization

Remember that elements are just realized once and then memorized for further access

Example 1:

```clojure
(do
  (def ls (lazy-seq 0 (fn [x] (let [n (+ x 1)]
                                (println "realized" n)
                                n))))

  (first ls)
  ; => 0, the first value is the passed seed value -> no evaluation

  (second ls)
  ;realized 1
  ;=> 1, the second value is accessed -> it is evaluated

  (second ls)
  ;=> 1, the second value has already been evaluated -> the memorized value is returned
)
```


Example 2:

```clojure
(do
  (def ls (lazy-seq 0 (fn [x] (let [n (+ x 1)]
                                (println "  realized" n)
                                n))))

  (println "/1/:")
  (->> (map #(* 10 %) ls)
       (take 40)     
       (take 2)
       (doall))

  ; /1/:
  ;   realized 1
  ; => (0 10)
     
  (println "/2/:")
  (->> (map #(* 10 %) ls)
       (take 40)
       (take 4)
       (doall))
       
  ; /2/:
  ;   realized 2
  ;   realized 3
  ; => (0 10 20 30)
)
```


## Recursive Lazy Sequences

Lazy sequences can be recursively defined by cons'ing a recursive function that 
returns a lazy sequence.


### Infinite Recursive Lazy Sequences

Lazy sequence with all positive numbers:

```clojure
(do
  (defn positive-numbers
    ([]  (positive-numbers 1))
    ([n] (cons n #(positive-numbers (inc n)))))

  (doall (take 4 (positive-numbers))))
  
  ; => (1 2 3 4)
```

Lazy Fibonacci number sequence computed by a recursive function:

```clojure
(do
  (defn fib
    ([]    (fib 0 1))
    ([a b] (cons a #(fib b (+ a b)))))

  (doall (take 7 (fib))))
  
  ; => (0 1 1 2 3 5 8)
```


### Finite Recursive Lazy Sequences


Finite recursive lazy sequence producing a sequence of decremented numbers:

```clojure
(do
  (defn number-seq [x]
    (when (> x 0)
      (cons x #(number-seq (dec x)))))
      
  (doall (take 3 (number-seq 5)))
  ; => (5 4 3)
      
  (doall (take 10 (number-seq 5)))
  ; => (5 4 3 2 1)
      
  (doall (number-seq 5))
  ; => (5 4 3 2 1)
)
```

Finite recursive lazy sequence (reading text lines from a Reader)

```clojure
(do
  (defn line-seq [rdr]
    (when-let [line (. rdr :readLine)]
      (cons line #(line-seq rdr))))

  (doall (take 3 (line-seq (io/buffered-reader "1\n2\n3\n4"))))
  ; => ("1" "2" "3")
 
  (doall (take 10 (line-seq (io/buffered-reader "1\n2\n3\n4"))))
  ; => ("1" "2" "3" "4")

  (doall (line-seq (io/buffered-reader "1\n2\n3\n4")))
  ; => ("1" "2" "3" "4")
)
```

Alternative finite recursive lazy sequence (reading text lines from a Reader)

```clojure
(do
  (defn line-seq [rdr]
    (if-let [line (. rdr :readLine)]
      (cons line #(line-seq rdr))
      (lazy-seq)))

  (doall (take 3 (line-seq (io/buffered-reader "1\n2\n3\n4"))))
  ; => ("1" "2" "3")

  (doall (line-seq (io/buffered-reader "1\n2\n3\n4")))
  ; => ("1" "2" "3" "4")
)
```



## Functions working with Lazy Sequences

Creating and realizing lazy sequences:
 
	- lazy-seq
	- doall
 

Functions that return lazy sequences when their input is a lazy sequence:
 
	- cons
	- map
	- filter
	- remove
	- take
	- take-while
	- drop
	- drop-while
	- rest
	- repeat

Functions that return realized elements from a lazy sequences:
	
	- first
	- second
	- third
	- fourth
	- nth
