# Control Flow

Venice like any other lisp dialect does not have any explicit flow 
control operators as imperative languages do. Everything in Venice
is an expression and returns a value.


## do

`do` blocks sequentially execute multiple expressions. The value
of the last expression is returned.
 
```clojure
(do (println "1000")
    (println "2000")
    20)
; => 20
```


## if

`if` takes three expressions, a condition, a "then", and an "else"
expression. The "else" expression is optional:

```clojure
(str "2 is " (if (number? 2) "a number" "not a number"))
; => "2 is a number"

(if (pos? 100) "positive")
; => "positive"

(if (pos? -100) "positive")
; => nil
```

To handle larger blocks with multiple expressions presumable for 
side effects use `do`

```clojure
(if (number? 5)
  (do (println "5 is number")
      true)
  (do (println "5 is not a number")
      false))
; => false
```


## when

`when` is an `if` with only a then branch. It checks a condition and 
then evaluates any number of statements as a body in an implicit `do`. 
The value of the last expression is returned. If the condition is 
false, nil is returned.

```clojure
(let [x 6]
  (when (pos? x)
    (println x "is positive")
   x))
```

## cond

`cond` is a series of tests and expressions with an optional else part. 
Each test is evaluated in order and the expression is evaluated and 
returned for the first true test.

```clojure
(do
  (defn test [x]
    (cond
      (> x 0) "x is positive"
      (< x 0) "x is negative"))
  
  (test 10)   ; => "x is positive"
  (test -10)  ; => "x is negative"
  (test 0)    ; => nil
)
```

with an else part:

```clojure
(do
  (defn test [x]
    (cond
      (> x 0) "x is positive"
      (< x 0) "x is negative"
      :else   "x is zero"))
  
  (test 10)   ; => "x is positive"
  (test -10)  ; => "x is negative"
  (test 0)    ; => "x is zero
)
```


## case

`case` takes an expression and a list of clauses. Each clause takes the 
form of a test constant and a result expression.

```clojure
(do
  (defn test [x]
    (case (* x 10)
      10 :ten
      20 :twenty 
      30 :thirty))

  (test 1)  ; => :ten
  (test 2)  ; => :twenty
  (test 3)  ; => :thirty
  (test 4)  ; => nil
)      
```

with a default:

```clojure
(do
  (defn test [x]
    (case (* x 10)
      10 :ten
      20 :twenty 
      30 :thirty 
      :dont-know))

  (test 1)  ; => :ten
  (test 2)  ; => :twenty
  (test 3)  ; => :thirty
  (test 4)  ; => :dont-know
)      
```


## dotimes

`(dotimes binding & body)`

`dotimes` repeatedly executes a body with a name bound to integers from 0 through n-1. 
Returns nil.

```clojure
(dotimes [n 3] (println "n is" n))

; "n is 0"
; "n is 1"
; "n is 2"
; => nil
```


## docoll

`(docoll f coll)`

Applies f to the items of the collection presumably for side effects. Returns nil.

```clojure
(docoll #(println %) [1 2 3])

; 1
; 2
; 3
; => nil
```

