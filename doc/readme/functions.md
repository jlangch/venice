# Functions

## Creating functions

```clojure
(do
   (defn add [x y] (+ x y))
   
   (def mul (fn [x y] (* x y)))
   
   (add 1 2)
   (mul 3 4)
   (let [f (fn [x y] (- x y))] (f 5 3)))
```

## Variadic functions

```clojure
(do
   (defn log
      [message & args]
      (apply println (cons message (cons ": " args))))

   (log "message from" "192.0.0.76" "12:00" "Hello"))
```

## Multi-Arity functions

```clojure
(do
   (defn arity
      ([] (println "arity 0"))
      ([a] (println "arity 1"))
      ([a b] (println "arity 2"))
      ([a b c] (println "arity 3"))
      ([a b c & z] (println "arity 3+")))
      
   (arity 1 2))
```

## Anonymous functions

```clojure
(do
   (map (fn [x] (* 2 x)) (range 0 10))   
   (map #(* 2 %) (range 0 10)) 
   (map #(* 2 %1) (range 0 10))  
   (let [f #(+ %1 %2 %3)] (f 1 2 3)))
```

## Functions with preconditions

```clojure
(do
   (defn sum 
         [x y] 
         { :pre [(> x 0) (> y 0)] } 
         (+ x y)))
```


## Maps and Keywords as functions

```clojure
(do
   ; instead of (get {:a 1 :b 2} :b)
   ; maps/keys work as functions
   ({:a 1 :b 2} :b)
   (:b {:a 1 :b 2}))
```


## Function resolved from a string

```clojure
(let [add (resolve (symbol "+"))]
   (add 2 5))
```


## Function composition


```clojure
(filter (comp not zero?) [0 1 0 2 0 3 0 4])  ;; => [1 2 3 4]
```


## Partial functions

```clojure
(map (partial * 2) [1 2 3 4])   ;; => (2 4 6 8)
```


## Multimethods

Multimethods are a powerful mechanism for runtime polymorphism.

```clojure
(do
  ; defmulti with dispatch function 
  (defmulti math-op (fn [s] (:op s)))

  ; defmethod provides a function implementation for a particular dispatch value 
  (defmethod math-op "add" [s] (+ (:op1 s) (:op2 s)))
  (defmethod math-op "subtract" [s] (- (:op1 s) (:op2 s)))
  (defmethod math-op :default [s] 0)

  [ (math-op {:op "add"      :op1 1 :op2 5}) 
    (math-op {:op "subtract" :op1 1 :op2 5}) 
    (math-op {:op "bogus"    :op1 1 :op2 5}) ] )
```


## Recursion:

```clojure
(do
   (defn sum [n]
      (loop [cnt n, acc 0]
         (if (zero? cnt)
            acc
            (recur (dec cnt) (+ acc cnt)))))

   (sum 100000))
```
