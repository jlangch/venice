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

Recursively expands all macros in a form. It's implemented on top of `walk/prewalk`.

*Examples:*

```clojure
(do
  (load-module :walk)

  (walk/macroexpand-all '(and true true))

  (walk/macroexpand-all '(and true (or true false) true))

  (walk/macroexpand-all '(let [n 5] (cond (< n 0) -1 (> n 0) 1 :else 0))))
```



## Walk & Replace forms examples


### keywordize-keys

Recursively transforms all map keys from strings to keywords.

```clojure
(do
  (load-module :walk)

  (defn keywordize-keys [form]
    (let [f (fn [[k v]] (if (string? k) [(keyword k) v] [k v]))]
      ;; only apply to maps
      (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) form)))

  (keywordize-keys '(1 2 {"a" 1 "b" 2})))
```


### stringify-keys

Recursively transforms all map keys from keywords to strings.

```clojure
(do
  (load-module :walk)

  (defn stringify-keys [form]
    (let [f (fn [[k v]] (if (keyword? k) [(name k) v] [k v]))]
      ;; only apply to maps
      (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) form)))

   (stringify-keys '(1 2 {:a 1 :b 2})))
```


### replace (prewalk)

Recursively transforms form by replacing keys in key-map with
their values. Does replacement at the root of the tree first.

```clojure
(do
  (load-module :walk)

  (defn prewalk-replace [key-map form]
     (walk/prewalk (fn [x] (if (contains? key-map x) (key-map x) x)) form))

  (prewalk-replace {:a :A :b :B} '(1 2 :a :b))

  (prewalk-replace {:a :A :b :B} '(1 2 {:a 1 :b 2})))
```


### replace (postwalk)

Recursively transforms form by replacing keys in key-map with
their values. Does replacement at the leaves of the tree first.

```clojure
(do
  (load-module :walk)

  (defn postwalk-replace [key-map form]
     (walk/postwalk (fn [x] (if (contains? key-map x) (key-map x) x)) form))

  (postwalk-replace {:a :A :b :B} '(1 2 :a :b))

  (postwalk-replace {:a :A :b :B} '(1 2 {:a 1 :b 2})))
```
