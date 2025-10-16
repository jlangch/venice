# Functions

* [Creating Functions](#creating-functions)
* [Variadic Functions](#variadic-functions)
* [Multi-Arity Functions](#multi-arity-functions)
* [Anonymous Functions](#anonymous-functions)
* [Applying Functions](#applying-functions)
* [Locals and Closures](#locals-and-closures)
* [Functions with preconditions](#functions-with-preconditions)
* [Argument type hints](#argument-type-hints)
* [Collections and Keywords as functions](#collections-and-keywords-as-functions)
* [Function resolved from a string](#function-resolved-from-a-string)
* [Partial Functions](#partial-functions)
* [Function Composition](#function-composition)
* [Function threading macros](#function-threading-macros)
* [Functions calling each other](#functions-calling-each-other)



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

A variadic function is a function of indefinite arity, accepting a 
variable number of arguments. A variadic function can have any number
of fixed arguments.

```clojure
(do
   ;; variadic sum with a single fixed arg
   (defn sum [x & xs]
      (reduce + x xs))

   (sum 1)
   (sum 1 2)
   (sum 1 2 3 4 5 6 7 8 9 10))
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

*multi-arity* anonymous functions

```clojure
(do
  (def add (fn ([x] x) ([x y] (+ x y))))
  
  (add 1)    ; => 1
  (add 1 2)) ; => 3
```


## Applying Functions

The `apply` function invokes a function with 0 or more fixed arguments, and draws 
the rest of the needed arguments from a list or a vector. The last argument must
be a list or a vector.

```clojure
(do
  (apply max [1 5 2 8 3])   ;; same as (max 1 5 2 8 3)
  
  (apply str '(1 2 3 4))    ;; same as (str 1 2 3 4)
  (apply str 1 '(2 3 4))    ;; same as (str 1 2 3 4)
  (apply str 1 2 '(3 4))    ;; same as (str 1 2 3 4)
  (apply str 1 2 3 '(4))    ;; same as (str 1 2 3 4) 
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

Preconditions are defined in a vector and are saved in the function’s metadata. 
The precondition assertions are a vector predicates and must return true for 
the constrains to pass. If any of them evaluate to false an AssertionError 
(a Java `RuntimeException`) with the specific constrain assertion will be thrown. 

```clojure
(do
   (defn sum [x y] 
      { :pre [(number? x) (number? y)] } 
      (+ x y)))
```

Passing preconditions

```
> (sum 1 2)
=> 3
```

Failing preconditions

```
> (sum 1 "2")

Exception in thread "main" AssertionException: precondition assert failed: (number? y)

[Callstack]
    at: user/sum (user: line 3, col 27)
    at: user/sum (user: line 1, col 2)
```


## Argument type hints

Venice supports function argument type hints through argument metadata. Type hints
are available with Venice 1.11.x.

```clojure
(do
  (defn sum [^:long x ^:long y] (+ x y))
  (sum 1 2))
```

```clojure
(do
  (defn sum [^:number x ^:number y] (+ x y))
  
  (sum 1 2)
  (sum 1.0 2)
  (sum 1.1M 2.6M))
```

```clojure
(do
  (ns foo)
  (deftype :complex [real      :long
                     imaginary :long])
                     
  (defn sum [^:foo/complex x ^:foo/complex y] 
    (complex. (+ (:real x) (:imaginary x)) 
              (+ (:real y) (:imaginary y))))
     
  (sum (complex. 1 2) (complex. 5 8)))
```

Type hints with multi-arity functions:

```clojure
(do
   (defn foo
      ([] 0)
      ([^:long x] x)
      ([^:long x ^:long y] (+ x y))
      ([^:long x ^:long y & xs] (apply + x y xs)))
   (foo )
   (foo 1)
   (foo 1 2)
   (foo 1 2 3 4 5))
```


Type hints with sequential destructuring:

```clojure
(do
   (defn foo [[^:long x ^:long y]] (+ x y))
   (foo [1 2]))
```


Type hints with associative destructuring:

```clojure
(do
   (defn foo [{:keys [^:long x ^:long y]}] (+ x y))
   (foo {:x 1 :y 2}))
```
                   
                   
For datatypes of the *core* namespace the namespace can be omitted.

```clojure
;; these two function definitions are equivalent
(defn sum [^:long x ^:long y] (+ x y)))
(defn sum [^:core/long x ^:core/long y] (+ x y)))
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

If we did need to call `add` (adding two numbers) with fewer than the required 
arguments, for example if we are mapping `add` over a vector, then we can use 
`partial` to help us call the `add` function with the right number of arguments:

```clojure
(do
  (defn add [x y] (+ x y))
  (map (partial add 2) [1 2 3 4]))  ;; => (3 4 5 6)
```

In this case the _partial function_ prevents us from writing an explicit anonymous
function like `#(+ 2 %)` in `(map #(+ 2 %) [1 2 3 4])`

```clojure
(map (partial + 2) [1 2 3 4])  ;; => (3 4 5 6)
```



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

Threading macros in Venice are used to make code more readable and to simplify 
the chaining of operations. They help to “thread” an initial value through a 
series of functions or forms. There are two main threading macros in Venice:

	1.	-> (thread-first macro)
	2.	->> (thread-last macro)

Practical Use Cases:

	1.	Data Processing Pipelines: Threading macros are ideal for building data 
	     processing pipelines where each step transforms the data in a clear and 
	     readable manner.
	2.	Nested Function Calls: They help in avoiding deeply nested function calls, 
	     making the code easier to read and maintain.
	3.	Combining Transformations: When you have a series of transformations to 
	    apply to data, threading macros keep the sequence of operations clear.


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

