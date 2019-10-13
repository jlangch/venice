# Datatypes

Venice has a rich set of data types (boolean, string, int, long, double, decimal, list, 
vector, set, and map). All data types share common features:

- they are immutable
- they support equals semantics
- they provide a hash value
- they are comparable (lower, equal, greater)
- collections support persistent manipulation
- they support meta data


## nil

_nil_ can be used for any data type in Venice. _nil_ has the same value as _null_ in Java.

```clojure
(def x nil)
(nil? x)
```


## Booleans

Booleans are defined by constants _true_ and _false_.

```clojure
(and true (== 1 1))
(and false (== 1 1))
```


## Strings

```clojure
(println "abcd")
(println "ab\"cd")
(println "PI: \u03C0")
(println """{ "age": 42 }""")
```


## Character

```clojure
(println (char "A"))
(println (char 65))
```


## Numbers

### long

Based on the Java type _Long_. long is Venice's standard integer type.

```clojure
(+ 1 2)
```

### int

Based on the Java type _Integer_.

```clojure
(+ 2I 3I)
```

### double

Based on the Java type _Double_.

```clojure
(+ 1.0 2.0)
```

### decimal

Based on the Java type _BigDecimal_.

```clojure
(+ 1.0M 2.0M)
```

## Keywords

Keywords (e.g. `:a`) are symbolic identifiers.

```clojure
{:a 100, :b 200}
```

## Symbols

Symbols are identifiers that are normally used to refer to function parameters, 
let bindings, and global vars.

```clojure
(defn sum [x y] (+ x y))
(def x 100)
```


## Collections

### list

Immutable persistent list.

```clojure
'(1 2 3)
(list 1 2 (+ 1 2))
```

### vector

Immutable persistent vector.

```clojure
[1 2 3]
(vector 1 2 (+ 1 2))
```

### hash-set

Immutable persistent hash set.

```clojure
#{1 2 3}
(set 1 2 3)
```

### sorted-set

Immutable persistent sorted set.

```clojure
(sorted-set 2 3 1)
```

### hash-map

Immutable persistent hash map.

```clojure
{:a 100 :b 200}
(hash-map :a 100 :b 200)
```

### ordered-map

Immutable persistent ordered map.

```clojure
(ordered-map :a 100 :b 200)
```

### sorted-map

Immutable persistent sorted map.

```clojure
(sorted-map :a 100 :b 200)
```

### stack

Threadsafe mutable stack based on the Java type _ConcurrentLinkedDeque_.

```clojure
(stack )
```


### queue

Threadsafe mutable queue based on the Java type _LinkedBlockingDeque_.

```clojure
(queue) ;; unbounded queue

(queue 100) ;; bounded queue
```
