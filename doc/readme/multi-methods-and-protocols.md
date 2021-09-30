# Functions


### Expression Problem

From Wikipedia:

*The [Expression Problem](https://en.wikipedia.org/wiki/Expression_problem) is a challenge problem in programming languages that concerns the extensibility and modularity of statically typed data abstractions. The goal is to define a data abstraction that is extensible both in its representations and its behaviors, where one can add new representations and new behaviors to the data abstraction, without recompiling existing code, and while retaining static type safety (e.g., no casts). It exposed deficiencies in programming paradigms and programming languages, and it is still not definitively solved, although there are many proposed solutions.* 


*Venice* supports in particular:

* [Multi-Methods](#multimethods)
* [Protocols](#protocols)
* [Proxies](#proxies)



## Multimethods

Multimethods are a powerful mechanism for runtime polymorphism.

`defmulti` creates a new multimethod with the associated dispatch function.

`defmethod` creates a new method for a multimethod associated with a dispatch-value.


```clojure

(do
  (defn rect [w h] {:shape :rect, :width w, :height h})
  (defn circle [radius] {:shape :circle, :radius radius})

  ; defmulti with dispatch function 
  (defmulti area (fn [s] (:shape s)))

  ; defmethod provides a function implementation for a particular dispatch value 
  ; in the examples the dispatch value s are :rect, :circle, and :default for
  ; the default dispatch
  (defmethod area :rect [r] (* (:width r) (:height r)))
  (defmethod area :circle [c] (* (. :java.lang.Math :PI) (square (:radius c))))
  (defmethod area :default [s] 0) 
 
  (area (rect 4 13))  ; -> 52
  
  (area (circle 12))  ; -> 452.3893421169302
)
```

Keyword as dispatch function:

```clojure
(do
  (defn rect [w h] {:shape :rect, :width w, :height h})
  (defn circle [radius] {:shape :circle, :radius radius})

  (defmulti area2 :shape)
  (defmethod area2 :rect [r] (* (:width r) (:height r)))
  (defmethod area2 :circle [c] (* (. :java.lang.Math :PI) (square (:radius c))))
    
  (area2 (rect 4 13))  ; -> 52
  
  (area2 (circle 12))  ; -> 452.3893421169302
)
```

Simple recursion with multimethods:

```clojure
(do
  (defmulti factorial identity)
  (defmethod factorial 0 [_] 1)
  (defmethod factorial :default [n] (* n (factorial (dec n))))

  (factorial 5)  ; -> 120
)
```

**Note:** Simple recursion suffers from Java's stack depth limit. See 
[Recursion](recursion.md) how to apply it in Venice.  


## Protocols

TODO


## Proxies

TODO

