# Functions

* [Creating Functions](#creating-functions)
* [Variadic Functions](#variadic-functions)
* [Multi-Arity Functions](#multi-arity-functions)
* [Anonymous Functions](#anonymous-functions)
* [Applying Functions](#applying-functions)
* [Locals and Closures](#locals-and-closures)
* [Functions with preconditions](#functions-with-preconditions)
* [Collections and Keywords as functions](#collections-and-keywords-as-functions)
* [Function resolved from a string](#function-resolved-from-a-string)
* [Partial Functions](#partial-functions)
* [Function Composition](#function-composition)
* [Function threading macros](#function-threading-macros)
* [Functions calling each other](#functions-calling-each-other)



## Function composition


## Creating Functions

```clojure
(do
   (defn add [x y] (+ x y))
   
   (def mul (fn [x y] (* x y)))
   
   (add 1 2)
   (mul 3 4)
   (let [f (fn [x y] (- x y))] 
      (f 5 3)))
```


## Variadic Functions

A variadic function is a function of indefinite arity, accepting 
a variable number of arguments.

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


## Passing Values by Name to Functions 

Values can be passed by name using a map literal.

The function `foo` expects the named arguments `x` and `y`. The `:or` clause specifies 
a default for `y`.

```clojure
(do
   (defn foo [{:keys [x y] :or {y 10}}] 
      (list x y))
      
   (foo {:x 1 :y 2})  ;; => (1 2)
   (foo {:x 1}))      ;; => (1 10)
```


## Anonymous Functions

```clojure
(do
   (map (fn [x] (* 2 x)) (range 0 5))   ; => (0 2 4 6 8)
   (map #(* 2 %) (range 0 5))           ; => (0 2 4 6 8)
   (map #(* 2 %1) (range 0 5))          ; => (0 2 4 6 8)
   (let [f #(+ %1 %2 %3)] (f 1 2 3))    ; => 6
   ((fn [x] (* x 10)) 5))               ; => 50
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
  (apply str 1 2 3 '(4)))   ;; same as (str 1 2 3 4) 
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

A closure is a function that remembers the environment at which it was 
created.


```clojure
(do
  (defn pow [n]
    (fn [x] (apply * (repeat n x))))  ; closes over n

  ;; n is provided here as 2 and 3, then n goes out of scope
  (def square (pow 2))
  (def cubic (pow 3))
  
  ;; n value still available because square and cubic are closures
  (square 4) ; => 16   effectively as (apply * (repeat 2 4)) 
  (cubic 4)) ; => 64   effectively as (apply * (repeat 3 4))
```

Even global functions can remember the context they have been created:

```clojure
(do
  (let [x 100]
    (defn test [] (str "x: " x)))
    
  (test))  ; => "x: 100"
```


## Functions with preconditions

```clojure
(do
   (defn sum [x y] 
      { :pre [(number? x) (number? y)] } 
      (+ x y)))
```


## Collections and Keywords as functions

Vectors, maps, sets, and keywords are functions too.


**Vectors**

```clojure
(do
   ([1 2 3] 1) ; -> 2
   
   ;; with default
   ([1 2 3] 5 10)) ; -> 10
```

**Maps**

```clojure
(do
   ;; instead of (get {:a 1 :b 2} :b)
   ({:a 1 :b 2} :b)  ; -> 2
   ({:a 1 :b 2} :c)  ; -> nil
   
   ;; with default
   ({:a 1 :b 2} :c 9)  ; -> 9

   ;; accessing nested maps
   (let [m {:a {:b {:c 3}}}]
      (-> m :a :b :c)))  ; -> 3
```

**Sets**

```clojure
(do
   (#{:a :b} :b)  ; -> :b
   (#{:a :b} :c)  ; -> nil
   
   ;; with default
   (#{:a :b} :c 9))  ; -> 9
```

**Keywords**

```clojure
(do
   (:b {:a 1 :b 2})  ; -> 2
   (:c {:a 1 :b 2})  ; -> nil
   (:b #{:a :b})  ; -> :b
   (:c #{:a :b})  ; -> nil
 
   ;; with defaults
   (:c {:a 1 :b 2} 9)  ; -> 9
   (:c #{:a :b} 9))    ; -> 9
```


## Function resolved from a string

```clojure
(let [add (resolve (symbol "+"))]
   (add 2 5))
```


## Partial Functions

_In computer science, partial application (or partial function application) refers to the process of fixing a number of arguments to a function, producing another function of smaller arity._


```clojure
(do
  (defn add [x y] (+ x y))
  
  (def add2 (partial add 2))
  (def add3 (partial add 3))
  
  (add2 4)   ;; => 6
  (add3 4))  ;; => 7
```


### Using functions with fewer arguments than they can normally take

If we did need to call `add` with fewer than the required arguments, for example 
if we are mapping `add` over a vector, then we can use `partial` to help us call 
the `add` function with the right number of arguments:

```clojure
(map (partial add2) [1 2 3 4])  ;; => (3 4 5 6)
```

In this case the _partial function_ prevents us from writing an explicit anonymous
function like `#(add 2 %)` in `(map #(add 2 %) [1 2 3 4])`


### Using functions with more arguments than they can normally take

The `reduce` function can only work on a single collection as an argument (or a value 
and a collection), so an error occurs if you wish to reduce over multiple collections.

```clojure
(reduce + [1 2 3 4])  ;; => 10
```

This returns an error due to invalid arguments:

```clojure
(reduce + [1 2 3 4] [5 6 7 8])  ;; error
```

However, by using `partial` we can take one collection at once and return the result
of reduce on each of those collections:

```clojure
(map (partial reduce +) [[1 2 3 4] [5 6 7 8]])  ;; => (10 26)
```



## Function Composition

Creates a new function composed of one or more functions. The composed
functions are executed from right to left.

`(comp not zero?)` is equivalent to `(fn [x] (not (zero? x))`


```clojure
(filter (comp not zero?) [0 1 0 2 0 3 0 4])  ;; => [1 2 3 4]
```

```clojure
(do
  (def person
    {:name "Peter Meier"
     :address {:street "Lindenstrasse 45"
               :city "Bern" 
               :zip 3000}})
  ((comp :street :address) person) ;; => "Lindenstrasse 45"
  ((comp :private :email) person)) ;; => nil
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

**Thread first** `->`

Taking an initial value as its first argument, `->` threads it through one or more 
expressions. Starting with the second form, the macro inserts the first value as 
its first argument and repeats inserting the result of the form to the first argument
of the next form. 

```clojure
(do
  (defn bigint [x] (. :java.math.BigInteger :new x))
  (-> (bigint "1000")
      (. :multiply (bigint "600"))
      (. :add (bigint "300"))))  ;; => 600300
```

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

**Thread last** `->>`

Taking an initial value as its first argument, `->>` threads it through one or more 
expressions. Starting with the second form, the macro inserts the first value as 
its last argument and repeats inserting the result of the form to the last argument
of the next form. 

```clojure
(->> (range 0 8)
     (filter odd?)
     (map #(+ 2 %)))  ;; => (3 5 7 9)
```

**Thread any** `as->`, `-<>`

```clojure
; allows to use arbitrary positioning of the argument
(as-> (range 0 8) v
      (filter odd? v)
      (reduce + v)
      (* v 2))  ;; => 32
```

```clojure
; the chosen threading symbol may be used multiple times in a form
; thus allowing the use of complex forms like if expressions
(as-> {:a 1 :b 2} m
      (update m :a #(+ % 10))
      (if true
        (update m :b #(+ % 10))
         m))  ;; => {:a 11 :b 12}
```

```clojure
; allows to use arbitrary positioning of the argument using the placeholder '<>'
; note: the threading symbol <> may only be used once in a form
(-<> (range 0 8)
     (filter odd? <>)
     (reduce + <>)
     (* <> 2))  ;; => 32
```


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

