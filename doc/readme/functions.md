# Functions

* [Creating Functions](#creating-functions)
* [Variadic Functions](#variadic-functions)
* [Multi-Arity Functions](#multi-arity-functions)
* [Anonymous Functions](#anonymous-functions)
* [Applying Functions](#applying-functions)
* [Locals and Closures](#locals-and-closures)
* [Functions with preconditions](#functions-with-preconditions)
* [Maps, Sets, and Keywords as functions](#maps-sets-and-keywords-as-functions)
* [Function resolved from a string](#function-resolved-from-a-string)
* [Partial Functions](#partial-functions)
* [Function Composition](#function-composition)
* [Function threading macros](#function-threading-macros)
* [Multimethods](#multimethods)
* [Functions calling each other](#functions-calling-each-other)



## Function composition


## Creating Functions

```clojure
(do
   (defn add [x y] (+ x y))
   
   (def mul (fn [x y] (* x y)))
   
   (add 1 2)
   (mul 3 4)
   (let [f (fn [x y] (- x y))] (f 5 3)))
```


## Variadic Functions

```clojure
(do
   (defn log
      [message & args]
      (apply println (cons message (cons ": " args))))

   (log "message from" "192.0.0.76" "12:00" "Hello"))
```


## Multi-Arity Functions

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


## Anonymous Functions

```clojure
(do
   (map (fn [x] (* 2 x)) (range 0 5))   ; => (0 2 4 6 8)
   (map #(* 2 %) (range 0 5))           ; => (0 2 4 6 8)
   (map #(* 2 %1) (range 0 5))          ; => (0 2 4 6 8)
   (let [f #(+ %1 %2 %3)] (f 1 2 3)))   ; => 6
   ((fn [x] (* x 10)) 5)                ; => 50
```


## Applying Functions

The `apply` function invokes a function with 0 or more fixed arguments, and draws 
the rest of the needed arguments from a list or a vector. The last argument must
be a list or a vector.

```clojure
(do
  (apply str '(1 2 3 4))    ;; same as (str 1 2 3 4)
  (apply str 1 '(2 3 4))    ;; same as (str 1 2 3 4)
  (apply str 1 2 '(3 4))    ;; same as (str 1 2 3 4)
  (apply str 1 2 3 '(4))    ;; same as (str 1 2 3 4) 
 )
```


## Locals and Closures

### let

`let` binds symbols to values in a "lexical scope". A lexical scope creates a
new context for names, nested inside the surrounding context. Names defined 
in a `let` take precedence over the names in the outer context.

```clojure
(let [x 1
      y (* 2 3)]
  (+ x y))
```

This `let` expression creates two local bindings for `x` and `y`. The expression 
`(+ x y)` is in the lexical scope of the `let` and resolves `x` to 1 and `y` to 6. 
Outside the "lexical scope" (`let` expression), `x` and `y` will not 
be accessible.


### defn

`defn` binds its function arguments in a new "lexical scope" as well. Unlike `let`
the local bindings for `x` and `y` get their values from the caller.

```clojure
(do
  (defn sum [x y]
     (+ x y))
   
  (sum 1 2))
```


### Closures

The `fn` special form creates a "closure". It "closes over" the surrounding 
lexical scope and captures their values beyond the lexical scope.

A function that returns a function i.e. higher order functions are nice examples
of a closure.

```clojure
(do
  (defn pow [n]
    (fn [x] (apply * (repeat n x))))  ; closes over n

  ;; n is provided here as 2 and 3, then n goes out of scope
  (def square (pow 2))
  (def cubic (pow 3))
  
  ;; n value still available because square and cubic are closures
  (square 4) ; => 16   effectively as (apply * (repeat 2 4)) 
  (cubic 4)  ; => 64   effectively as (apply * (repeat 3 4))
)
```


## Functions with preconditions

```clojure
(do
   (defn sum [x y] 
      { :pre [(number? x) (number? y)] } 
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


## Partial Functions

```clojure
(map (partial * 2) [1 2 3 4])  ;; => (2 4 6 8)
```


## Function Composition

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
  (defn rect [w h] {:shape :rect, :width w, :height h})
  (defn circle [radius] {:shape :circle, :radius radius})

  ; defmulti with dispatch function 
  (defmulti area (fn [s] (:shape s)))

  ; defmethod provides a function implementation for a particular dispatch value 
  (defmethod area :rect [r] (* (:width r) (:height r)))
  (defmethod area :circle [c] (* (. :java.lang.Math :PI) (square (:radius c))))
  (defmethod area :default [s] 0) 
 
  (area (rect 4 13))  ; -> 52
  
  (area (circle 12))  ; -> 452.3893421169302
)
```

Keyword as discriminator function:

```clojure
(do
  (defn rect [w h] {:shape :rect, :width w, :height h})
  (defn circle [radius] {:shape :circle, :radius radius})

  (defmulti area :shape)
  (defmethod area :rect [r] (* (:width r) (:height r)))
  (defmethod area :circle [c] (* (. :java.lang.Math :PI) (square (:radius c))))
    
  (area (rect 4 13))  ; -> 52
  
  (area (circle 12))  ; -> 452.3893421169302
)
```

Simple recursion with multimethods:

```clojure
(do
  (defmulti factorial identity)
  (defmethod factorial 0 [_] 1)
  (defmethod factorial :default [n] (* n (factorial (dec n))))

  (factorial 5)  ; -> 120
)
```

_Note: simple recursion suffers from Java's stack depth limit._


## Functions calling each other

Venice supports functions calling each other without needing to declare them 
or bind them with `letfn` as required with _Clojure_.

Nevertheless alternately calling functions can cause stack overflows if the 
recursion is too deep. Use the `trampoline` function or convert to self-recursion
to overcome the stack overflow problem.

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

