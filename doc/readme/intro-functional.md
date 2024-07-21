# Introduction to Functional Programming

_THIS IS WORK IN PROGRESS_

* [Basics](#basics)
* [Functional Concepts](#functional-concepts)


## Basics

Venice is a Lisp dialect and recognizes two kinds of structures:

**1. Literal representations of data**

```clojure
10                  ; a number of type long (64bit)
1.45                ; a number of type double (64bit float)
1.45M               ; a arbitrary precision decimal number 
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
control flows. All other more complex control flows like `case` are based on these 
fundamental operations.

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



## Introduction for Beginners

_TODO_

### Variables

Syntax:

```clojure
(def var-name var-value)
```

Where `var-name` is the name of the variable and `var-value` is the value bound 
to the variable.


*Examples:*

```clojure
(do
  (def x 100)
  (def name "hello")
  (def status true)

  ;; print the variables
  (println "x:" x)
  (println "name:" name)
  (println "status:" status))
```

*Naming Variables:*

The name of a variable can be composed of letters, digits, and the underscore 
character. It must begin with either a letter or an underscore. 


### Functions

_TODO_

**Defining a Function**

_TODO_


**Functions with Multiple Arguments**

_TODO_


**Variadic Functions**

_TODO_


**Anonymous Functions**

_TODO_


**Applying Functions**

_TODO_




### Immutability

*Venice* data structures like strings, lists, vectors, sets, maps are all immutable, 
meaning once created, it cannot be changed. Instead of modifying data, new data 
structures are created. These data structures are effectively immutable, as their 
operations do not (visibly) update the structure in-place, but instead always yield 
a new updated structure. 

*Example:*

```clojure
(do
  (def digits-1 [1 2 3 4 5 6])
  
  ;; add 7 to the digits-1 vector by calling
  ;;  (conj digits-1 7)
  ;; this yields a new vector with the digit added
  (def digits-2 (conj digits-1 7))
  
  (println "digits-1: " digits-1)
  (println "digits-2: " digits-2))
  
  ;; => digits-1:  [1 2 3 4 5 6]
  ;;    digits-2:  [1 2 3 4 5 6 7]
```


### Let and Local Variables

_TODO_



### Filter-Map-Reduce

Filter, map, and reduce are three fundamental operations in functional programming, 
commonly used to process and transform collections of data. These operations form 
a powerful pattern for data processing pipelines.

**Filter**

The filter operation processes a collection to produce a new collection containing 
only the elements that satisfy a given predicate (a function that returns a boolean 
value).

```clojure
(filter even? [1, 2, 3, 4, 5, 6])
```

output: `(2, 4, 6)`


**Map**

The map operation transforms a collection by applying a function to each element, 
producing a new collection of the transformed elements.

```clojure
(map square [1, 2, 3, 4, 5, 6])
```
output: `(1, 4, 9, 16, 25, 36)`

or using a anonymous function

```clojure
(map #(* % %) [1, 2, 3, 4, 5, 6])
```

output: `(1, 4, 9, 16, 25, 36)`


**Reduce**

The reduce operation combines the elements of a collection into a single value 
using an associative binary operation. It applies a function cumulatively to the 
elements of a collection, from left to right, reducing the collection to a single 
value.

*Adding numbers*

```clojure
(reduce + [1, 2, 3, 4, 5, 6])
```

output: `21`

*Using an initial value*

```clojure
(reduce * 1 [1, 2, 3, 4, 5, 6])
```

output: `720`

*Using an anonymous reducing function*

```clojure
(reduce #(+ %1 (inc %2)) [1, 2, 3, 4, 5, 6])
```

output: `26`


**Combining Filter, Map, and Reduce**

These operations can be combined to perform complex data processing tasks in a concise 
and readable manner. Here’s an example that uses all three operations together:

```clojure
(->> (filter even? [1, 2, 3, 4, 5, 6])
     (map square)
     (reduce +))    
```

output: `56`


### Control Structures

_TODO_


### Loops

_TODO_

standard functions:  (apply + [1 2 3 4 5])  (reduce + [1 2 3 4 5])  (filter even? [1 2 3 4 5])

recursion:  loop-recur  



## Functional Concepts

Functional Programming is all about programming with functions.

Functional programming emphasizes declarative over imperative coding, meaning you 
focus on what to solve rather than how to solve it. By leveraging these principles 
and techniques, functional programming aims to produce clearer, more concise, and 
more robust code.



**Concepts**

- First-Class and Higher-Order Functions
- Pure Functions
- Referential Transparency
- Functional Composition
- Recursion
- Immutability
- Lazy Evaluation
- Closures
- Partial Function Application


### First-Class and Higher-Order Functions

Functions are treated as first-class citizens. This means they can be assigned to 
variables, passed as arguments to other functions, and returned as values from other 
functions.

Higher-Order are functions that take other functions as arguments or return them as 
results. Common examples include map, filter, and reduce.


### Pure Functions

A function is pure if its output is determined only by its input values, without 
observable side effects (e.g. doing I/O, altering global vars, no internal state, ...). 
This means the function's behavior is consistent and doesn't rely on or alter the 
program state.

Pure functions are easier to reason about, test, and debug. They also enable better 
optimization by the compiler.


### Referential Transparency

An expression is referentially transparent if it can be replaced with its value without 
changing the program's behavior. This property is a direct result of using pure 
functions.

If there is referential transparency the expression below is valid:

`f(x) + f(x) = 2 * f(x)`

Referential transparency enables more predictable and reliable code, making it easier 
to refactor and optimize.


### Functional Composition

Building complex functions by combining simpler ones. Functions are composed by passing 
the output of one function as the input to another.

*Example:*

If you have two functions f and g, function composition allows you to create a new 
function h such that `h(x) = g(f(x))`.


### Recursion

Recursion is the process in which a function calls itself as a subroutine. Recursion is 
often used in place of traditional looping constructs in *Functional Programming*.

*Tail Recursion:*

*Tail Recursion* is a specific form of recursion where the recursive call is the last 
operation in the function, allowing for optimization by the compiler to prevent stack 
overflow.


### Immutability

Data is immutable, meaning once created, it cannot be changed. Instead of modifying data, 
new data structures are created. Such data structures are effectively immutable, as their 
operations do not (visibly) update the structure in-place, but instead always yield a new 
updated structure. 

Immutability helps avoid side effects and makes concurrent programming much safer and 
easier.

*Persistent Data Structures:*

*Persistent Data Structures* are a data structures that always preserves the previous 
version of itself when it is modified. There are efficient implementations for lists, sets
and maps. E.g. [VAVR](https://github.com/vavr-io/vavr) provides an implementation for JAVA
which used by *Venice*.


### Lazy Evaluation

Lazy Evaluation is an evaluation strategy which delays the computation of expressions until 
their values are needed. It can help in optimizing performance by avoiding unnecessary 
calculations.


### Closures

A closure is a function that captures the bindings of free variables in its lexical 
context. This allows the function to access those variables even when it is invoked outside 
their scope.

*Closures* are often used to create function factories and for data encapsulation.


### Partial Function Application

Partial function application is a technique in functional programming where a function 
that takes multiple arguments is applied to some of its arguments, producing another 
function that takes the remaining arguments. This allows you to fix a number of arguments 
to a function without invoking it completely, creating a new function with a smaller 
arity (number of arguments).

*Benefits of Partial Application:*

1. Code Reusability: You can create more specific functions from general ones,
   improving reusability.

2. Code Clarity: By naming partial applications appropriately, you can make code more
   readable and intention-revealing.

3. Functional Composition: It facilitates composing functions by fixing arguments in 
   stages, making it easier to build complex functions from simpler ones.
   
