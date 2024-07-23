# Filter-Map-Reduce

Filter, map, and reduce are three fundamental operations in functional programming, 
commonly used to process and transform collections of data. These operations form 
a powerful pattern for data processing pipelines.


## Filter

Filter processes a sequence of data and produces a new list with elements
that meet a predicate.

```clojure
(filter even? [1 2 3 4 5 6 7 8 9 10])
; => (2 4 6 8 10)
```


## Map

Map transforms a sequence of data by applying a function to each element 
of it. Map produces a new list with the transformed items.

```clojure
(map #(* 100 %) [1 2 3])
; => (100 200 300)
```

It’s possible to have multiple collections that map to each other:

```clojure
(map + [1 2 3] [100 100 100])
; => (101 102 103)
```

Map returns a list with the same number of elements that is given to it, 
it has the same number of inputs to outputs.



## Reduce

Reduce reduces a sequence of data into a single result by applying a 
function on it.

```clojure
(reduce + [1 2 3 4 5 6 7 8 9 10])
; => 55
```

It takes the first two items in the list, applies the function to them, 
then takes that result along with the next item in the list and applies
the function to them and so on.

There are two different types of reduce in Venice, one that takes an 
initial starting argument and one that takes no starting value and 
applies the function to the starting values in the list.

One with no starting value:

```clojure
(reduce + [1 2 3])
; => 6
```

One with a starting value:

```clojure
(reduce + 6 [1 2 3])
; => 12
```



## Combine Filter-Map-Reduce

Filter, map and reduce operations can be combined to perform complex data 
processing tasks in a concise and readable manner.


Combining Filter-Map-Reduce leads to deeply nested operations:

```clojure
(reduce + (map #(+ 10 %) (filter even? [1 2 3 4 5 6 7 8])))
; => 60
```

The thread last macro `->>` converts nested function calls into a linear 
flow of function calls, improving readability.

```clojure
(->> [1 2 3 4 5 6 7 8]
     (filter even?)
     (map #(+ 10 %))
     (reduce +))
; => 60
```



