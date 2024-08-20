# First Steps in Venice (A Beginner's Guide)


* [Literals and Operations](#literals-and-operations)
* [Control Flow](#control-flow)
* [Variables](#variables)
* [Functions](#functions)
* [Immutability](#immutability)
* [Let and Local Variables](#let-and-local-variables)
* [Filter-Map-Reduce](#filter-map-reduce)
* [Loops](#loops)
* [Java Interop](#java-interop)
    


## Literals and Operations

Venice is a Lisp dialect and recognizes two kinds of structures:

**1. Literal representations of data**

```clojure
10                  ; a number of type long (64bit)
1.45                ; a number of type double (64bit float)
1.45M               ; a arbitrary precision decimal number 
"foo"               ; a string
#\π                 ; a character
true                ; a boolean
(1 2 3)             ; a list of long numbers
["abc" "de" "fgh"]  ; a vector of strings
{"a" 100 "b" 200 }  ; a map with strings as keys and numbers as values
#{"a" "b" "c" }     ; a set of strings
```

**2. Operations, this is how you do things**

All operations take the form `(operator operand-1 operand-2 ... operand-n)` and return 
always a value.

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

In functional parlance Venice is based on symbolic expressions, in-short 
[s-expression](https://en.wikipedia.org/wiki/S-expression).



## Control Flow

In Venice, flow control operators are expressions too. `if` and `do` are the two basic 
control flows. All other more complex control flows like `case` are based 
on these fundamental operations.

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

#### case

Venice offers the ’case’ statement which is similar to the ‘switch’ statement available 
in the Java programming language. 


```clojure
(case (+ 1 9)
  10  "ten"
  20  "twenty"
  30  "thirty"
  "dont-know")
```

output: "ten"



## Variables

Syntax:

```
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


## Functions

#### Defining a Function

Syntax:

```
(defn fn-name [fn-args] fn-body)
```

Create a function with the name 'increment' and the argument 'x':

```clojure
(do
  (defn increment [x] (+ x 1))

  ;; calling the function
  (increment 100)) 
```


#### Functions with Multiple Arguments

```clojure
(do
  (defn add [x y] (+ x y))

  ;; calling the function
  (add 10 15)) 
```


#### Anonymous Functions

Functions accept often other functions as arguments. 


*Example: Increment each element of a collection*

`map` requires a single argument function to transform each value of the 
collection. Defining our own increment function for mapping the values is 
cumbersome for small functions:

```clojure
(do
  (defn increment [x] (+ x 1))
  
  (map increment [1 2 3 4]))  ;; => [2 3 4]
```

Anonymous functions like `#(+ %1 1)` simplify the use of small ad-hoc functions:

```clojure
(map #(+ %1 1) [1 2 3 4])  ;; => [2 3 4]
```

Note: Venice expands `#(+ %1 1)` to the anonymous function `(fn [%1] (+ %1 1))` while 
reading the source code. `%1` is the first argument of the anonymous function. The 
placeholders %1, %2, %3, ... are used for the positional arguments of the anonymous 
function.

Note: Because single argument functions are ubiquitous, the argument placeholder 
`%` is a synonym for `%1` allowing to write `#(+ % 1)` to further simplify anonymous 
functions.



#### Threading macros

Threading macros in Venice are used to make code more readable and to simplify 
the chaining of operations. They help to “thread” an initial value through a 
series of functions or forms. There are two main threading macros in Venice:

1. `->` (thread-first macro)
2. `->>` (thread-last macro)


**Thread first** `->`

Taking an initial value as its first argument, `->` threads it through one or more 
expressions. Starting with the second form, the macro inserts the first value as 
its first argument and repeats inserting the result of the form to the first argument
of the next form. 

*Example*

Let’s say we want to process a map, extract a value, convert it to a string, and then 
print it.

Without `->`:

```clojure
(println (str (get {:a 1 :b 2} :a)))
```

With `->`:

```clojure
(-> {:a 1 :b 2}   ; Start with the map
    (get :a)      ; Get the value for key :a
    str           ; Convert the value to a string
    println)      ; Print the string
```


**Thread last** `->>`

Taking an initial value as its first argument, `->>` threads it through one or more 
expressions. Starting with the second form, the macro inserts the first value as 
its last argument and repeats inserting the result of the form to the last argument
of the next form. 

*Example*

Let’s filter a list of numbers, square each number, and then sum them.

Without `->>`:

```clojure
(reduce + (map square (filter odd? [1 2 3 4 5])))
```

With `->>`:

```clojure
(->> [1 2 3 4 5]        ; Start with the list
     (filter odd?)      ; Filter odd numbers
     (map square)       ; Square each number
     (reduce +))        ; Sum the numbers
```


## Immutability

*Venice* data structures like strings, lists, vectors, sets, maps are all immutable, 
meaning once created, they cannot be changed. Instead of modifying data, new data 
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


## Let and Local Variables

When you want some lexically-scoped named values to use in a section of your code, 
you can use the *let* expression:

```clojure
(let [width   10
      height  20]
  (println "Area: " (* width height)))
```

These local names are symbols that refer directly to the values you set them to.

You can re-set the symbols in the binding vector multiple times (building it up into 
the value you need), if you find it useful:

```clojure
(let [x 2
      x (* x x)
      x (+ x 1)]
  x)
```

output: 5



## Filter-Map-Reduce

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



## Loops

Due the declarative approach of functional languages they need loop constructs 
much less than imperative languages.

The standard functions mostly accept an arbitrary numbers of arguments and 
thus prevent the need for looping constructs like *for*, *while*, ...

```clojure
  (+ 1 2 3 4)                 ;; => 10
  
  (max 1 2 3 4)               ;; => 4

  (str "hello" ", " "world")  ;; => "hello, world"
  
  (filter even? [1 2 3 4])    ;; => (2 4)
  
  (map inc [1 2 3 4])         ;; => (2 3 4 5)
  
  (reduce + [1 2 3 4])        ;; => 10
```


#### doseq

`doseq` repeatedly executes the body function (presumably for side-effects) with the bindings.

```clojure
(doseq [x [0 1 2 3 4 5]] (print x))
```

prints: "012345"


#### loop - recur

The loop-recur syntax in Venice is used to create loops with recursion in a way that is efficient 
and avoids stack overflow errors.

Basics:

1. `loop`: establishes a point to which `recur` can jump back. 
2. `recur`: jumps back to the nearest enclosing loop, re-evaluating it with new values.
	
	
	
**Example: Summing Numbers from 1 to n**
	
*Step-by-Step Explanation:*

1. Define the loop: Start a loop with initial values.
2. Perform operations inside the loop: Do the computation or check conditions.
3. Use recur to continue the loop: Jump back to the loop with new values or exit the 
   loop if a condition is met.


*Code Example:*

```clojure
(do
  (defn sum-to-n [n]
    (loop [i 1            ; Initialize loop variables: i starts at 1
           sum 0]         ; sum starts at 0
      (if (<= i n)        ; If i is less than or equal to n
        (recur (inc i)    ; Recur with incremented i and updated sum
               (+ sum i)) ; Update sum by adding i
        sum)))            ; If condition is false, return the sum        
         
   ;; call the function
   (sum-to-n 10))
```

output: `55`

**Note:** This is just an example to demonstrate loop-recur. In real world you would use:

```clojure
(reduce + (range 1 11))  ; sum numbers from 1 to 10
```

**Key Points:**

1. Tail Recursion Optimization:
    * 'recur' is optimized for tail recursion, meaning 
      it doesn’t add a new frame to the call stack, making it memory efficient.
2. Loop Variable Initialization:
    * Variables initialized in 'loop' are re-evaluated with 'recur'.
3. Exit Condition: 
    * Always ensure there’s a condition to exit the loop, or it will 
      run indefinitely.



## Java Interop

Venice supports calling Java constructors, static and instance methods as well as accessing 
static class and instance fields.


## Calling Java

Java calls follow the patterns:

constructor: `(. :class :new arg1 arg2 ...)`

instance method: `(. object :method arg1 arg2 ...)`

static method: `(. :class :method arg1 arg2 ...)`

static field: `(. :class :field)`


```clojure
(do
   ;; constructor no args
   (. :java.awt.Point :new)
   
   ;; constructor with two arguments
   (. :java.awt.Point :new 10 20)
   
   ;; instance method
   (let [r (. :java.awt.Rectangle :new 100 200)]
      (. r :translate 10 10)
      r)

   ;; instance no arg methods
   (let [r (. :java.awt.Rectangle :new 100 200)]
      (. r :isEmpty)   ; isEmpty()
      (. r :getWidth)  ; getWidth()
      (. r :getX))     ; getX()
 
   ;; static field
   (. :java.lang.Math :PI)

   ;; static method
   (. :java.lang.Math :min 20 30)

   ;; constructor and instance method
   (-> (. :java.time.LocalDate :now) 
       (. :plusDays 5))

   ;; using imports (avoids the use of qualified class names)
   (import :java.time.LocalDate)
   (. :LocalDate :now) 
)
```


## Where to head next

- browse through the [readme](https://github.com/jlangch/venice/blob/master/README.md#documentation)
- see the [cheatsheet](https://cdn.rawgit.com/jlangch/venice/277936c/cheatsheet.pdf) and use it as a quick reference
