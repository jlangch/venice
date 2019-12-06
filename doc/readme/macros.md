# Macros


* [Overview](#overview)
* [Macros vs Functions](#macros-vs-functions)
* [When to use Macros](#when-to-use-macros)
* [Toolbox](#toolbox)
* [Macro Hygiene](#macro-hygiene)



## Overview

_TODO_


## Macros vs Functions

A simple explanation of the difference between functions and macros is:

* a function transforms values into other values
* a macro transforms code into other code

In a more depth analysis function and macros can be compared regarding the aspects:

* when the arguments are evaluated?
* is the return value evaluated?

Regarding these those two aspects, here are the differences between macros and functions:

|           | arguments evaluation                           | return value evaluated? |
| :---      | :---                                           | :---                    |
| functions | before function code execution                 | not evaluated           |
| macros    | only when macro code evaluates them explicitly | evaluated               |



## When to use Macros

**Macros should be avoided whenever possible.**

The reason is that macros are not first-class citizens in Venice. They cannot be 
accessed at runtime. You cannot passed them as an argument to a function, nor do any 
of the other powerful things functional programming offers. 

Macros are very powerful. But their power comes at a price: they are only available at 
parse/compile time. The use of macros should be reserved for those special occasions 
when their power is needed. Functions should always be preferred to macros.

There are two circumstances where they are required.


### 1. The code has to run at parse/compile time

Macros are very flexible in controlling which parts are evaluated at parse/compile time
or at runtime.

This is a macro that is completely evaluated at parse/compile time. 

```clojure
(defmacro build-time []
  (str (time/local-date-time)))
```

Another example for this kind of macros is performing expensive calculations at 
parse/compile time as an optimization.


### 2. Access to unevaluated arguments is required

Macros are useful to create new control flow constructs. 

As an example the `when` macro takes a test predicate and a body. The body is 
only executed if the predicate evaluates to `true`.

The macro which transforms into an _if_ with a _do_ for a _then_ without an _else_.

Let's first implement `when` with function. 

```clojure
(defn when [test & body]
   (if test (do (butlast body) (last body))))
```

predicate _true_:

```clojure 
(when true (println 100) 10)
; 100
; => 10
```

predicate _false_:

```clojure 
(when false (println 100) 10)
; 100
; => nil
```

The body is evaluated eagerly in both cases whether the test predicate is _true_ or _false_, 
because Venice evaluates expression before passing them as arguments to functions.
The returned valued is in both cases correct.


**when implemented as a macro:**

```clojure
(defmacro when [test & body]
   (list 'if test (cons 'do body)))
```

predicate _true_:

```clojure 
(when true (println 100) 10)
; 100
; => 10
```

predicate _false_:

```clojure 
(when false (println 100) 10)
; => nil
```


## Toolbox

The Venice reader provides a few special forms to deal with macros:

* Quote (')
* Syntax quote (`)
* Unquote (~)
* Unquote splicing (~@)

The functions `macroexpand` and `macroexpand-all` are your best friends when writing 
and verifying macros.


### Quote and Syntax Quote

_TODO_

### Unquote

_TODO_

### Unquote-splicing

_TODO_

### Macro expansion

_TODO_



## Macro Hygiene

_TODO_

### Symbol Capturing

_TODO_

### Auto Generate Symbols


_give an example_


### Limitations of auto generated symbols

Auto generated symbols are not working with nested _syntax quotes_:

```clojure
`(let [x# 1]
   ~@(map
       (fn [n] `(+ x# ~n))
       (range 3)))
```

Because x# is going to expand to a different _gensym_ in the two different 
contexts. To work around this explicitly create a symbol name with `gensym`:

```clojure
(let [x-sym (gensym "x")]
  `(let [~x-sym 1]
     ~@(map
         (fn [n] `(+ ~x-sym ~n))
         (range 3))))
```
