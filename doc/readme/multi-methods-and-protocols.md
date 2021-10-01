# Multi-Methods and Protocols


### Expression Problem

The **Expression Problem** refers to the problem of making a language extensible. 
Software manipulates data types using operations. Sometimes, we want to add 
new operations, and have them work on existing data types. Sometimes, we want 
to add new data types, which will work with existing operations.

Object-oriented languages make it easy to add new data types (classes), by 
extending existing ones. One can make a new type that extends **java.util.List**, 
and have access to the operations it defines. But to add a new method to all 
the different **List** types, a lot of existing code has to be touched.

Functional languages tend towards the opposite: it’s easy to add new operations 
(functions), but harder to adapt the operation to various types. To write a function 
in Venice that works on both **lists** and **maps**, we’ll probably have to write two 
implementations and include an **if** in the calling function. If we then want to 
extend it to work on **vectors**, we’ll have to write a third implementation and 
add another condition to the calling function.


Definition from Wikipedia:

*The [Expression Problem](https://en.wikipedia.org/wiki/Expression_problem) is a challenge problem in programming languages that concerns the extensibility and modularity of statically typed data abstractions. The goal is to define a data abstraction that is extensible both in its representations and its behaviors, where one can add new representations and new behaviors to the data abstraction, without recompiling existing code, and while retaining static type safety (e.g., no casts). It exposed deficiencies in programming paradigms and programming languages, and it is still not definitively solved, although there are many proposed solutions.* 


*Venice* provides two mechanisms to deal with the expression problem:

* [Multimethods](#multimethods)
* [Protocols](#protocols)



## Multimethods

A *multimethod* is a special kind of function. It is is a combination of a dispatching 
function, and one or more methods. The dispatch function takes the arguments to the 
function and routes to the specific method responsible for the arguments. Thus 
*multimethods* are a powerful mechanism for runtime polymorphism, they can dispatch 
on any argument or combination of arguments.


`defmulti` creates a new multimethod with the associated dispatch function.

`defmethod` creates a new method for a multimethod associated with a dispatch-value.


```clojure
(do
  (ns foo)
  
  (def pi (. :java.lang.Math :PI))
  
  (deftype :rect [width :double, height :double])
  (deftype :circle [radius :double])

  ; defmulti with dispatch function 
  (defmulti area (fn [s] (type s)))

  ; defmethod provides a function implementation for a particular dispatch value 
  ; in the examples the dispatch value s are :rect, :circle, and :default for
  ; the default dispatch
  (defmethod area :foo/rect [r] (* (:width r) (:height r)))
  (defmethod area :foo/circle [c] (* pi (square (:radius c))))
  (defmethod area :default [s] 0) 
 
  (area (rect. 4.0 13.0))  ; -> 52.0
  (area (circle. 12.0))    ; -> 452.3893421169302
)
```

```clojure
(do
  (ns foo)

  (def pi (. :java.lang.Math :PI))
  
  (defn rect [w h] {:shape :rect, :width w, :height h})
  (defn circle [radius] {:shape :circle, :radius radius})

  ; defmulti with dispatch function 
  (defmulti area (fn [s] (:shape s)))

  ; defmethod provides a function implementation for a particular dispatch value 
  ; in the examples the dispatch value s are :rect, :circle, and :default for
  ; the default dispatch
  (defmethod area :rect [r] (* (:width r) (:height r)))
  (defmethod area :circle [c] (* pi (square (:radius c))))
  (defmethod area :default [s] 0) 
 
  (area (rect 4.0 13.0))  ; -> 52.0
  (area (circle 12.0))    ; -> 452.3893421169302
)
```

Keyword as dispatch function:

```clojure
(do
  (ns foo)
  
  (def pi (. :java.lang.Math :PI))

  (defn rect [w h] {:shape :rect, :width w, :height h})
  (defn circle [radius] {:shape :circle, :radius radius})

  (defmulti area2 :shape)
  (defmethod area2 :rect [r] (* (:width r) (:height r)))
  (defmethod area2 :circle [c] (* pi (square (:radius c))))
    
  (area2 (rect 4.0 13.0))  ; -> 52.0
  (area2 (circle 12.0))    ; -> 452.3893421169302
)
```


## Protocols

*Protocols* are more similar to an object orientated way of solving polymorphism. 
Where as a *multimethod* is just one polymorphic operation a *protocol* offers the 
flexibility of implementing a collection of one or more polymorphic functions. 
Protocols are setup in a similar way to interfaces in Java. 
Where *multimethods* can dispatch on any argument type, value, or combination of it, 
protocols dispatch on the type of the first argument to determine which behavior 
of the function to use.


Define a protocol with two polymorphic functions and extend it with `extend`:

```clojure
(do
   (ns foo)
   
   (deftype :complex [re :long, im :long])
   
   (defprotocol XMath (+ [x y])
                      (- [x y]))
                      
   (extend :foo/complex XMath
           (+ [x y] (complex. (core/+ (:re x) (:re y))
                              (core/+ (:im x) (:im y))))
           (- [x y] (complex. (core/- (:re x) (:re y))
                              (core/- (:im x) (:im y)))))
                              
   (extend :core/long XMath 
           (+ [x y] (core/+ x y))
           (- [x y] (core/- x y))) 
           
   (foo/+ 2 3)
   (foo/+ (complex. 1 1) (complex. 4 5)))
```


Define a protocol with two polymorphic functions and extend it within 
a *custom type* definition:

```clojure
(do
   (ns foo)
   
   (defprotocol Lifecycle (start [c]) 
                          (stop [c]))
   
   (deftype :component [name :string]
            Lifecycle (start [c] (println "'~(:name c)' started"))
                      (stop [c] (println "'~(:name c)' stopped")))
   
   (let [c          (component. \"test\")
         lifecycle? (extends? (type c) Lifecycle)] 
     (println "'~(:name c)' extends Lifecycle protocol: ~{lifecycle?}")
     (start c) 
     (stop c)))
```

