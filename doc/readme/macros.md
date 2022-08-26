# Macros


* [Overview](#overview)
* [Macros vs Functions](#macros-vs-functions)
* [When to use Macros](#when-to-use-macros)
* [The Macro toolbox](#the-macro-toolbox)
* [Macro Hygiene](#macro-hygiene)



## Overview

Macros give Venice a great power. They allow you to extend the language in a way
that is not possible with most other languages. In Java for example 
you are limited to the features the language provides like the special forms `if`, 
`do..while`, `for` loops, or `new`. Venice macros allow the creation of new
control flow constructs and to bend the language to your needs.

Macros are very powerful but come at the price to be not always simple. With
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


### Namespace

Macros evaluate in the namespace they are called from, not in the namespace
they are defined. This is different from functions that evaluate in the 
namespace which they are defined.


## When to use macros

As basic rule: **Macros should be avoided whenever possible**

The reason is that macros are not first-class citizens in Venice. They cannot be 
accessed at runtime. You cannot pass them as an argument to a function, nor do any 
of the other powerful things functional programming offers. 

Macros are very powerful. But their power comes at a price: they are only available at 
read/compile time. The use of macros should be reserved for those special occasions 
when their power is needed. Functions should always be preferred to macros.

There are two circumstances where they are required:


### 1. Access to unevaluated arguments is required

Macros are useful to create new control flow constructs. 

As an example the simplified `when` macro takes a test predicate and a form. 
The form is only to be executed if the predicate evaluates to `true`.

Let's first implement `when` with a function. 

```clojure
(defn when [test form]
   (if test form nil))
```

|        | predicate *true*             | predicate *false* |
| :---   | :---                         | :---                           |
| expr   | `(when true (do (print 99) 3))` | `(when false (do (print 99) 3))` |
| stdout | `99`                         | `99`  *should not be printed!*  |
| return | `3`                          | `nil`                           |

The *form* is evaluated eagerly in both cases whether the test predicate is *true* 
or *false*, because Venice evaluates expressions before passing them as arguments 
to a function. Nevertheless the returned valued is in both cases correct.

**when implemented as a macro:**

```clojure
(defmacro when [test form]
   (list 'if test form nil))
```

|        | predicate *true*              | predicate *false* |
| :---   | :---                          | :---                          |
| expr   | `(when true (do (print 99) 3))` | `(when false (do (print 99) 3))` |
| stdout | `99`                          |                               |
| return | `3`                           | `nil`                          |

Now everything is fine. Macros can postpone the evaluation of the macro arguments
to the time the expanded macro body is evaluated whereas functions evaluate its
arguments eagerly.


### 2. The code has to run at read/compile time

Macros are very flexible in controlling which parts are evaluated at read/compile time
or at runtime.

This is a macro that is completely evaluated at read/compile time. 

```clojure
(defmacro build-time []
  (str (time/local-date-time)))
```

Another example for this kind of macros is performing expensive calculations at 
read/compile time as an optimization.


## The Macro toolbox

The Venice *Reader* provides a few special forms to deal with macros:

* Quote (')
* Syntax quote (`)
* Unquote (~)
* Unquote splicing (~@)

The functions `macroexpand` and `macroexpand-all` are your best friends when writing 
and verifying macros.


### Quote

There are two equivalent ways to quote a form either with `quote` or with `'`. 
They prevent the quoted form from being evaluated.

Regular quotes work recursively with any kind of forms and types: strings, maps, lists, vectors…

```clojure
'(a :a 1)  ; => (a :a 1)
```

```clojure
(quote (a :a 1))  ; => (a :a 1)
```

```clojure
'(+ 1 2)  ; => (+ 1 2)
```

```clojure
'(a (b (c d (+ 1 2))))  ; => (a (b (c d (+ 1 2))))
```

```clojure
'{:a (1 2 3) b (c d "x")}   ; => {:a (1 2 3) b (c d "x")}
```

With *quotes* in our toolbox we can write our first macro. The `when` macro evaluates 
a *test* predicate. If logical *true*, it evaluates the *body*.

```clojure
(defmacro when [test form]
   (list 'if test form nil))
```

At macro expansion time `(when true (println 100))` is transformed to 
`(if true (println 100) nil)`


* By quoting `'if` Venice is prevented from evaluating `if` at macro expansion
  time. The `if` should be transformed as is to the output expression.

* `test`and `form` do not need to be quoted because they are macro arguments and
  substituted at macro expansion time without evaluation.

* `nil` does not need to be quoted either it evaluates to itself. You can quote
  it though, but it makes the macro less readable.

* The macro expansion can be tested with `(macroexpand-all '(when true (println 100)))`


### Syntax Quote / Unquote

*Syntax quotes* (a backquote) supress evaluation of the form that 
follows it and all the nested forms. It is possible to *unquote* part of 
the form that is quoted with `~`. Unquoting allows you to evaluate parts of 
the *syntax quoted* expression.

Without evaluation:

```clojure
`(16 17 (inc 17))  ; => (16 17 (inc 17))
```

With evaluation:

```clojure
`(16 17 ~(inc 17))  ; => (16 17 18)
```

```clojure
`(16 17 ~(map inc [16 17]))  ; => (16 17 (17 18))
```

*Syntax quotes* allow writing macros in a more elegant way regarding evaluation 
rules at macro expansion time:

```clojure
(defmacro when [test form]
   `(if ~test ~form nil))
```

At macro expansion time `(when true (println 100))` is transformed to 
`(if true (println 100) nil)`.

The syntax quote is similar to templating languages where parts of the template 
are *fixed* and parts are *inserted*  (evaluated). 
The syntax quote makes the form that follows it a *template*.

The unquote which is a tilde `~` then is how parts of the template are forced to 
be evaluated. It acts similarly to variable replacement in templates.


#### Pitfalls with unquote

This is ok:

```clojure
(defmacro sum [x y] 
   `(+ ~x ~y ~(inc 3)))
```
   
   
Nested unquotes do not work:

```clojure
(defmacro sum [x y] 
   `(+ ~x ~y ~(inc ~y)))
```

Throws a _SymbolNotFoundException_: Symbol 'unquote' not found.
   
   
One might be tempted to write:

```clojure
(defmacro sum [x y] 
  `(+ ~x ~y ~(inc y)))
```

But this uses an unevaluated y argument. So `(sum 1 2)` correctly yields 3, but 
`(sum 1 (* 3 4))` fails because the function `inc` gets a list `'(* 3 4)` as 
argument.
   
   
Rewrite it to get it work:

```clojure
(defmacro sum [x y] 
   `(+ ~x ~y (inc ~y)))
```


### Unquote-splicing

Unquote evaluates to a collection of values and inserts the collection into the
quoted form. But sometimes you want to unquote a list and insert its elements 
(not the list) inside the quoted form. This is where `~@` (unquote-splicing) 
comes to rescue:

Without splicing:

```clojure
`(16 17 ~(map inc [16 17]))  ; => (16 17 (17 18))
```

With splicing:

```clojure
`(16 17 ~@(map inc [16 17]))  ; => (16 17 17 18)
```

Other examples:

```clojure
`(1 2 ~@#{1 2 3})  ; => (1 2 1 2 3)
```

```clojure
`(1 2 ~@{:a 1 :b 2 :c 3})  ; => (1 2 [:a 1] [:b 2] [:c 3])
```

Arbitrary `let` bindings:

```clojure
`(let [~@(interleave ['a 'b 'c] [1 2 3])] (+ a b))

;; expands to:

(let (a 1 b 2 c 3) (+ a b)) ;; => 3
```


So far our macro accepts a single form. What happens if we're going to extend the macro
to use a body with multiple forms?


#### A first approach

Using *syntax quote* and *unquote* we can write it as:

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

The *body* argument holds a list and thus a list is inserted in the template
for the variable "body" resulting in `((println 100) (println 200))`. That is not 
a function that can be executed. We do not want the surrounding parenthesis. 
What we actually want is "body" to be splice into the `(do ...)` list as values.

Unquote-splicing is exactly doing that. 


#### A second approach with unquote-splicing

Rewriting the macro to (see "core.venice")

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

While developing macros, it is important to be able to test the macro and see what code
the macro expands to. Venice provides two functions for this:

* `macroexpand`
* `macroexpand-all`

`macroexpand` expands a single macro form while `macroexpand-all` recursively 
expands all macros in a form.

```clojure
(macroexpand '(-> c (+ 3) (* 2)))
  
; expanded   
(* (+ c 3) 2)
```

```clojure
(macroexpand-all '(let [n 5] 
                    (cond 
                      (< n 0) -1 
                      (> n 0)  1 
                      :else    0)))
     
; expanded   
(let [n 5] 
   (if (< n 0) 
       -1 
       (if (> n 0) 
           1 
           (if :else 0 nil))))
```


## Macro hygiene

So far we haven't used local variables within macros. Locals var names in macros
provide some pitfalls you have to be aware of when writing macros.

Venice supports a simple `time` macro to measure the execution time of an expression 
that uses local vars. It prints the execution time and returns the evaluated 
expression.

Let's rebuild the macro:

```clojure
(defmacro time-1 [expr]
  `(let [start (nano-time)
         ret   ~expr
         end   (nano-time)]
     (printf "Elapsed time: %s%n" (- end start))
     ret))
```

and testing it

```clojure
(time-1 (+ 1 2))
  
; Elapsed time: 32810
; => 3
```

When writing a macro, there is a possibility that the macro will interact with vars or 
locals outside of it in unexpected ways, for example by shadowing them. Such macros are 
known as unhygienic macros.


### Symbol capturing

Let's see what happens when the macro interacts with vars outside of the macro:

```clojure
(let [start 1 end 2] 
  (time-1 (+ start end)))
      
Elapsed time: 40438
=> 424855845202275
```

Surprisingly the result of `(+ 1 2)` is now `424855845202275` instead of `3`. The 
phenomenon happened here is called *symbol capturing*. The *start* var is captured
by the macro itself.

Expanding the call with `macroexpand-all` shows what happens:

```clojure
(macroexpand-all '(let [start 1 end 2] 
                    (time-1 (+ start end))))
```

results to

```clojure
(let [start 1 end 2] 
  (let [start (nano-time) ; 'start' is shadowing the outer 'start'
        ret   (+ start end)  
        end   (nano-time)] 
     (printf "Elapsed time: %s%n" (- end start)) 
     ret))
```

To solve the problem the macro should use safe local var names.

Venice provides two ways to create safe local var names for macros:

1. manually generate symbol names
2. auto generate symbols


### Manually generate safe symbol names

The `(gensym)` function lets you  manually create safe symbol names:

```clojure
(defmacro time-2 [expr]
  (let [start (gensym "start__")
        ret   (gensym "ret__")
        end   (gensym "end__")]
    `(let [~start (nano-time)
           ~ret   ~expr
           ~end   (nano-time)]
       (printf "Elapsed time: %s%n" (- ~end ~start))
       ~ret)))
```


### Auto generate symbols

By suffixing a symbol with a `#` within a *syntax quote*, Venice will 
create safe var names automatically while expanding the macro:

```clojure
`(aa#)  ; => (aa__28__auto)
```

```clojure
`{:a a# :b b# :c b#}  ; => {:a a__31__auto :b b__32__auto :c b__32__auto}
```

Applied to our macro ...

```clojure
(defmacro time-3 [expr]
  `(let [start# (nano-time)
         ret#   ~expr
         end#   (nano-time)]
     (printf "Elapsed time: %s%n" (- end# start#))
     ret#))
```

... and expanding it with `macroexpand-all`


```clojure
(macroexpand-all '(let [start 1 end 2] 
                    (time-3 (+ start end))))
```

... it results to

```clojure
(let [start 1 end 2] 
   (let [start__89__auto (nano-time) 
         ret__90__auto   (+ start end) 
         end__91__auto   (nano-time)] 
     (printf "Elapsed time: %s%n" (- end__91__auto start__89__auto)) 
     ret__90__auto))
```

... showing how the problem is solved 



### Limitations of auto generated symbols

Auto generated symbols are not working with nested *syntax quotes*:

```clojure
`(let [x# 1]
   ~@(map
       (fn [n] `(+ x# ~n))
       (range 3)))
```

Because x# is going to expand to a different *gensym* in the two different 
contexts. 

To work around this explicitly create a symbol name with `gensym`:

```clojure
(let [x-sym (gensym "x")]
  `(let [~x-sym 1]
     ~@(map
         (fn [n] `(+ ~x-sym ~n))
         (range 3))))
```

Beware of code like this:

```clojure
`(let [a# 100] ~(dec a#))
```

It will result in a *SymbolNotFoundException* for the `dec` argument. The reason for 
this behavior is that `(dec a#)` is executed before the symbol a# is created and 
assigned the value 100 in `(let [a# 100] ...`
