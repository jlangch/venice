# Introduction to Functional Programming

_THIS IS WORK IN PROGRESS_

* [Basics](#basics)
* [Functional Concepts](#functional-concepts)


## Basics

Venice is a Lisp dialect and recognizes two kinds of structures:

**1. Literal representations of data**

```clojure
1                   ; a number
"foo"               ; a string
true                ; a boolean
["abc" "de" "fgh"]  ; a vector of strings
{"a" 100 "b" 200 }  ; a map with strings as keys and numbers as values
```

**2. Operations, this is how you do things**

All operations take the form `(`, operator, operands, `)` and return always a value:

```clojure
(operator operand-1 operand-2 ... operand-n)
```

For example, if you want to add numbers 

```clojure
(+ 1 2 3 4 5)
```

or concatenate strings:

```clojure
(str "Hello" ", " "user") 
```

Operations can be nested:

```clojure
(+ 1 2 (* 3 4) (/ 20 4) 6)
```

This is all about the syntax in Venice. Control flow operations follow this structure too. 
Other languages like C, Java, Scala use different structures depending on the operator and 
the operands like for `if`, `for`, `case`, ... statements. 

Venice is structurally uniform, no matter which operator you’re using or what kind of data 
you’re operating on, the structure is the same.

In functional parlance Venice is based on symbolic expressions, in-short [s-expression](https://en.wikipedia.org/wiki/S-expression).



### Control Flow

In Venice, flow control operators are expressions too. `if` and `do` are the two basic 
control flows. More complex control flows like `case` are based on these fundamental 
operations.

#### if

`if` is the most important conditional expression. It consists of a predicate, a "then", 
and an "else" part. `if` will only evaluate the branch selected by the predicate. It
returns the value of the evaluated branch.

```
(if predicate then-expression else-expression)
```

Example:

```clojure
(if (< 10 100) 
  "lower than 100" 
  "equal or larger than 100")
```

#### do

`do` blocks sequentially execute multiple expressions. The value of the last expression 
is returned.

```clojure
(do
  (println 100) 
  (println 200) 
  (println 300)
  20)
```

`if` only takes a single expression for the "then" and "else" parts. Use `do` to build 
larger blocks that are a single expression.

```clojure
(if (even? 10) 
  (do  
    (println "10 is even")
    "even") 
  (do  
    (println "10 is odd")
    "odd"))
```


## Functional Concepts

Functional Programming is all about programming with functions.

**Features**

- Pure Functions / Referential Transparency
- Function Composition
- Anonymous Functions
- Higer Order Functions
- Partial Function Application
- Closure - returning functions from functions
- Data Immutability
- Lists are the fundamental data Structure


### Pure Functions

### Recursion

### Referential Transparency

### First-Class Functions

### Immutability

