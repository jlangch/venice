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
    1000)
; => 1000
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

## cond

## case

## dotimes

## docoll

## map-reduce

