# Tree Walker

Venice provides a generic tree walker for data structures. It takes 
any data structure (list, vector, map, set, ...), calls a function 
on every element, and uses the return value of the function in place 
of the original. 


## API

### Postwalk

`(postwalk f form)`

Performs a depth-first, post-order traversal of form. Calls f on
each sub-form, uses f's return value in place of the original.

*Example:*

```clojure
(postwalk (fn [x] (println "Walked:" (pr-str x)) x)
         '(1 2 {:a 1 :b [5 6]}))
```


### Prewalk

`(prewalk f form)`

Performs a depth-last, pre-order traversal of form. Calls f on
each sub-form, uses f's return value in place of the original.

*Example:*

```clojure
(prewalk (fn [x] (println "Walked:" (pr-str x)) x)
         '(1 2 {:a 1 :b [5 6]}))
```


## Examples: Walk & Replace forms


### keywordize-keys

Recursively transforms all map keys from strings to keywords.

```clojure
(do
  (defn keywordize-keys [form]
    (let [f (fn [[k v]] (if (string? k) [(keyword k) v] [k v]))]
      ;; only apply to maps
      (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) form)))

  (keywordize-keys '(1 2 {"a" 1 "b" 2})))
```


### stringify-keys

Recursively transforms all map keys from keywords to strings.

```clojure
(do
  (defn stringify-keys [form]
    (let [f (fn [[k v]] (if (keyword? k) [(name k) v] [k v]))]
      ;; only apply to maps
      (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) form)))

   (stringify-keys '(1 2 {:a 1 :b 2})))
```


### replace (prewalk)

Recursively transforms form by replacing keys in key-map with
their values. Does replacement at the root of the tree first.

```clojure
(do
  (defn prewalk-replace [key-map form]
     (prewalk (fn [x] (if (contains? key-map x) (key-map x) x)) form))

  (prewalk-replace {:a :A :b :B} '(1 2 :a :b))

  (prewalk-replace {:a :A :b :B} '(1 2 {:a 1 :b 2})))
```


### replace (postwalk)

Recursively transforms form by replacing keys in key-map with
their values. Does replacement at the leaves of the tree first.

```clojure
(do
  (defn postwalk-replace [key-map form]
     (postwalk (fn [x] (if (contains? key-map x) (key-map x) x)) form))

  (postwalk-replace {:a :A :b :B} '(1 2 :a :b))

  (postwalk-replace {:a :A :b :B} '(1 2 {:a 1 :b 2})))
```
