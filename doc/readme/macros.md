# Macros

_in progress_


## Overview

_TODO_


## When to use Macros

**Macros should be avoided whenever possible.**

The reason is that macros are not first-class citizens in Venice. They cannot be 
accessed at runtime. You cannot passed them as an argument to a function, nor do any 
of the other powerful things functional programming offers. 

Macros are very powerful. But their power comes with a price: they are only available at 
parse/compile time. The use of macros should be reserved for those special occasions 
when their power is needed. Functions should always be preferred to macros.

There are two circumstances where they are required.

1. The code has to run at parse/compile time

_TODO_


2. Access to unevaluated arguments is required

_TODO_


## Quote & Syntax Quote

_TODO_

## Unquote

_TODO_

## Unquote-splicing

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
