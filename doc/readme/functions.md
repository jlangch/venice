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

## Maps, Sets, and Keywords as functions

```clojure
(do
   ; instead of (get {:a 1 :b 2} :b)
   ; maps work as functions
   ({:a 1 :b 2} :b)  ; -> 2
   ({:a 1 :b 2} :c)  ; -> nil
   
   ; maps as functions with default
   ({:a 1 :b 2} :c 9)  ; -> 9
   
   
   ; sets work as functions
   (#{:a :b} :b)  ; -> :b
   (#{:a :b} :c)  ; -> nil
   
   ; sets as functions with default
   (#{:a :b} :c 9)  ; -> 9
 
 
   ; keywords as functions
   (:b {:a 1 :b 2})  ; -> 2
   (:c {:a 1 :b 2})  ; -> nil
   (:b #{:a :b})  ; -> :b
   (:c #{:a :b})  ; -> nil
 
   ; keywords as functions with defaults
   (:c {:a 1 :b 2} 9)  ; -> 9
   (:c #{:a :b} 9)  ; -> 9
 
    
   ; accessing nested maps
   (let [m {:a {:b {:c 3}}}]
      (-> m :a :b :c))  ; -> 3
)
```

## Function resolved from a string

```clojure
(let [add (resolve (symbol "+"))]
   (add 2 5))
```

## Partial functions

```clojure
(map (partial * 2) [1 2 3 4])  ;; => (2 4 6 8)
```

## Function composition

```clojure
(filter (comp not zero?) [0 1 0 2 0 3 0 4])  ;; => [1 2 3 4]
```

```clojure
(do
  (def xform
    (comp 
      (partial take 4)
      (partial map #(+ 2 %))
      (partial filter odd?)))
    
  (xform (range 0 10)))  ;; => (3 5 7 9)
```

## Function threading macros

Thread first `->`

```clojure
(do
  (def person
    {:name "Peter Meier"
     :address {:street "Lindenstrasse 45"
               :city "Bern" 
               :zip 3000}})
  (-> person :address :street) ;; => "Lindenstrasse 45"
  (-> person :email :private)) ;; => nil
```

Thread last `->>`

```clojure
(->> (range 0 8)
     (filter odd?)
     (map #(+ 2 %)))  ;; => (3 5 7 9)
```

Thread any `as->`, `-<>`

```clojure
;allows to use arbitrary positioning of the argument
(as-> [:foo :bar] v
      (map name v)
      (first v)
      (str/subs v 1))  ;; => "oo"
```

```clojure
; allows the use of if statements in the thread
(as-> {:a 1 :b 2} m
      (update m :a #(+ % 10))
      (if true
        (update m :b #(+ % 10))
         m))  ;; => {:a 11 :b 12}
```

```clojure
; allows to use arbitrary positioning of the argument
(-<> (range 0 8)
     (filter odd? <>)
     (reduce + <>)
     (* <> 2))  ;; => "Result: 32"
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

## Functions calling each other:

Venice supports functions calling each other without needing to declare them 
or bind them with `letfn` as required with _Clojure_.

Nevertheless alternately calling functions can cause stack overflows if the 
recursion is too deep. As of now Venice does not have a `trampoline` function
to overcome this.

```clojure
(do
  (let [print-number (fn [n]
                         (println n)
                         (decrease n))
        decrease (fn [n]
                     (when (> n 0)
                       (print-number (dec n))))]
    (decrease 10)))
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
