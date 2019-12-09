# Macros


* [Overview](#overview)
* [Macros vs Functions](#macros-vs-functions)
* [When to use Macros](#when-to-use-macros)
* [Toolbox](#toolbox)
* [Macro Hygiene](#macro-hygiene)



## Overview

Macros give Venice a great power. Macros allow you to extend the language in a way
that is not possible with most other languages. In Java for example 
you are limited to the features the language provides like the special forms `if`, 
`do..while`, `for` loops, or `new`. Venice Macros allow the creation of new
control flow constructs and to bend the language to your needs.

Macros are very powerful and come at the price to be not always simple. With
great power comes great responsibility to the creator to not misuse it.


## Macros vs Functions

A simple explanation of the difference between functions and macros is:

* a function transforms values into other values
* a macro transforms code into other code

In more detail function and macros can be compared regarding the aspects:

* when the arguments are evaluated?
* is the return value evaluated?

Regarding these two aspects macros and functions can be compared as:

|           | arguments evaluation                           | return value evaluated? |
| :---      | :---                                           | :---                    |
| functions | before function code execution                 | not evaluated           |
| macros    | only when macro code evaluates them explicitly | evaluated               |



## When to use Macros

As basic rule: **Macros should be avoided whenever possible**

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

As an example the simplified `when` macro takes a test predicate and a form. 
The form is only to be executed if the predicate evaluates to `true`.

Let's first implement `when` with a function. 

```clojure
(defn when [test form]
   (if test form nil))
```

|        | predicate _true_             | predicate _false_ |
| :---   | :---                         | :---                           |
| expr   | `(when true (do (print 99) 3))` | `(when false (do (print 99) 3))` |
| stdout | `99`                         | `99`  _should not be printed!_  |
| return | `3`                          | `nil`                           |

The _form_ is evaluated eagerly in both cases whether the test predicate is _true_ or _false_, 
because Venice evaluates expressions before passing them as arguments to a function.
Nevertheless the returned valued is in both cases correct.


**when implemented as a macro:**

```clojure
(defmacro when [test form]
   (list 'if test form nil))
```

|        | predicate _true_              | predicate _false_ |
| :---   | :---                          | :---                          |
| expr   | `(when true (do (print 99) 3))` | `(when false (do (print 99) 3))` |
| stdout | `99`                          |                               |
| return | `3`                           | `nil`                          |


## Toolbox

The Venice _Reader_ provides a few special forms to deal with macros:

* Quote (')
* Syntax quote (`)
* Unquote (~)
* Unquote splicing (~@)

The functions `macroexpand` and `macroexpand-all` are your best friends when writing 
and verifying macros.


### Quote

```clojure
(defmacro when [test form]
   (list 'if test form nil))
```

E.g.: at macro expansion time `(when true (println 100))` is transformed to 
`(if true (println 100) nil)`


* By quoting `'if` Venice is prevented from evaluating `if` at macro expansion
  time. The `if` should be transformed as is to the output expression.

* `test`and `form` do not need to be quoted because they are macro arguments and
  substituted at macro expansion time without evaluation.

* `nil` does not need to be quoted either it evaluates to itself. You can quote
  it though, but it makes the macro less readable.


### Syntax Quote / Unquote

Syntax quotes allow writing macros in a more elegant way regarding evaluation 
rules at macro expansion time.

```clojure
(defmacro when [test form]
   `(if ~test ~form nil))
```

E.g.: at macro expansion time `(when true (println 100))` is transformed to 
`(if true (println 100) nil)`

The syntax quote which is a backquote (`) supresses evaluation of the form that 
follows it and all the nested forms. It is similar to templating languages where 
parts of the template are _fixed_ and parts are _inserted_ (evaluated). 
The syntax quote makes the form that follows it a _template_.

The unquote which is a tilde (~) then is how parts of the template are forced to 
be evaluated. It acts similarly to variable replacement in templates in templating 
languages.


### Unquote-splicing

So far our macro accepts a single form. What happens if we're going to extend the macro
to use a body with multiple forms?

#### A first approach

Using syntax quote and unquote we can write it as:

```clojure
(defmacro when [test & body]
   `(if ~test (do ~body) nil))
```

If we expand a macro call

```clojure
(macroexpand '(when true (println 100) (println 200)))
```

to see the transformed expressions, we get 

```clojure 
(if true (do ((println 100) (println 200))) nil)
```

The _body_ argument holds a list and thus a list is inserted in the template
for the variable "body" resulting in `((println 100) (println 200))`. That is not 
a function that can be executed. We do not want the surrounding parenthesis. 
What we actually want is "body" to be splice into the `(do ...)` list as values.

Unquote-splicing is exactly doing that. 


#### A second approach with unquote-splicing

Rewriting the macro to

```clojure
(defmacro when [test & body]
   `(if ~test (do ~@body) nil))
```

... and expanding it

```clojure
(macroexpand '(when true (println 100) (println 200)))
```

... we see that the issue is solved now

```clojure 
(if true (do (println 100) (println 200)) nil)
```


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