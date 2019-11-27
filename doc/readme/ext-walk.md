# Walk

This module defines a generic tree walker for Venice data
structures.  It takes any data structure (list, vector, map, set,
...), calls a function on every element, and uses the return value
of the function in place of the original. 


## API

### Postwalk

`(postwalk f form)`

Performs a depth-first, post-order traversal of form. Calls f on
each sub-form, uses f's return value in place of the original.

*Example:*

```clojure
(do
  (load-module :walk)
  
  (walk/postwalk (fn [x] (println "Walked:" (pr-str x)) x)
                 '(1 2 {:a 1 :b [5 6]})))
```


### Prewalk

`(prewalk f form)`

Performs a depth-last, pre-order traversal of form. Calls f on
each sub-form, uses f's return value in place of the original.

*Example:*

```clojure
(do
  (load-module :walk)
  
  (walk/prewalk (fn [x] (println "Walked:" (pr-str x)) x)
                '(1 2 {:a 1 :b [5 6]})))
```


## Macro expansion

`(macroexpand-all form)`

Recursively expands all macros in a form

*Examples:*

```clojure
(do
  (load-module :walk)
  
  (walk/macroexpand-all '(and true true))
  
  (walk/macroexpand-all '(and true (or true false) true))
  
  (walk/macroexpand-all '(let [n 5] (cond (< n 0) -1 (> n 0) 1 :else 0))))
```


## Walk & Replace forms

