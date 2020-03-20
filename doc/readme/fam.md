# Functors, Applicatives and Monads in Pictures 

* [Functors](#functors)
* [Applicatives](#applicatives)
* [Monads](#monads)


This tutorial is the Venice version of the article [Functors, Applicatives and Monads in Pictures](http://adit.io/posts/2013-04-17-functors,_applicatives,_and_monads_in_pictures.html). The original article was written in Haskell, and is an excellent introduction to the very basics of functors, applicatives, and monads. Due to the differences in typing, data structures, support for varargs and currying, there are differences in how the concepts that the original article explains are implemented in Haskell and Venice.


## Introduction

### Values

Functions are applied to values returning a new value as the result:

```clojure
2
;=> 2

(+ 3 2)
;=> 5
```

### Currying

Venice does not support automatic currying, therefore `(+ 3)` would result in applying `+` to `3`, 
resulting in the number `3` instead of a function that adds `3` as in Haskell. Venice 
provides `partial` to get the equivalent behaviour.

```clojure
(+ 3)  
;=> 3

((partial + 3) 2)
;=> 5
```

### Boxes

Boxes are containers for values. Lists, vectors, and other data structures are 
kinds of "boxes" that contain other values. Haskell provides two simple boxes 

- Just - contains just some value
- Nothing - representing that no value is available

In Venice we have `just` for having just a value and `nil` for representing 
non-existence:

```clojure
(just 2)
;=> (just 2)

(deref (just 2))
;=> 2

nil
;=> nil
```

A box can be as simple as a just, a list, a map, an atom, a future or something 
more complex that has been created to serve a specific functionality. But a box
is conceptually always a container for a value.

Category theory applied to programming is interested in seeing what is common to all 
these boxes, what is general enough that can be done to all these kinds of boxes, 
in a same way without concern for the actual type of the box. So a particular box can 
be used in a number of different general ways, and depending on the ways of usage that 
it supports, the box can be given one or more of the general names: functor, 
applicative functor, monad etc. (there are more, but this tutorial cover these three).



## Functors

Here's a simple value:

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/fam/value.png">


And we know how to apply a function to this value: 

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/fam/value_apply.png">

Simple enough. Lets extend this by saying that any value can be in a context. 
For now you can think of a context as a box that you can put a value in:

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/fam/value_and_context.png">

_TODO_


## Applicatives

## Monads


