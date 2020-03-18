# Functors, Applicatives and Monads in Pictures 

* [Functors](#functors)
* [Applicatives](#applicatives)
* [Monads](#monads)


This tutorial is a Venice version of the article [Functors, Applicatives and Monads in Pictures](http://adit.io/posts/2013-04-17-functors,_applicatives,_and_monads_in_pictures.html). The original article was written in Haskell, and is an excellent introduction to the very basics of functors, applicatives, and monads. This article is ment to be read side-by-side with the original article. Due to the differences in typing, data structures, support for varargs and currying, there are differences in how the concepts that the original article explains are implemented in Haskell and Venice.

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


