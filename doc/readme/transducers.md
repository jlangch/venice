# Transducers

Transducers isolate transforming reducing functions (like `map` or `filter` in their transducer version) from sequential iteration. They are independent from their input and output sources. As a consequence transducers can be composed like any other function.

Traditional `map` or `filter` functions operate sequentially on input and output collections:

```clojure
(->> (range 0 1000)
     (filter odd?)      
     (map #(* 100 %))
     (map #(+ 10 %)))
```

In this example `filter` applies `odd?` to each element of the collection returning the filtered collection if all items have been processed. So are doing the two mapping functions thus creating many intermediate collections.

Transducers overcome this problem and decouple the transforming completely from input and output. Thus transducers can not only be applied to a collection of elements but to any kind of data sources that generate elements.

Transducer example:

```clojure
(do
  (def xform
    (comp
      (filter odd?)
      (map #(* 100 %))))
    
  (transduce xform conj (range 0 1000)))  
  
  ; => [100 300 500 700 900 ....  99900]
```


### Transduce function

The `transduce` function acts as the processor that applies the transforming and reducing transducer functions to the collection elements.

```clojure
(transduce xform f coll)
(transduce xform f init coll)
```
_Reduce with a transformation of a reduction function f (xf). If init is not supplied, (f) will be called to
produce it. f should be a reducing step function that accepts both 1 and 2 arguments. Returns the result of
applying (the transformed) xf to init and the first item in coll, then applying xf to that result and the 2nd
item, etc. If coll contains no items, returns init and f is not called._


### Reducing functions

Beside the standard reducing functions like `conj`, `+`, `-`, `max`, and `min` Venice provides some additional functions `rf-first`, `rf-every?`,  and `rf-any?`


## Examples:

```clojure
(do
  (def xform
    (comp
      (filter odd?)
      (map #(* 100 %))
      (map #(+ 10 %))))
    
  (transduce xform conj (range 0 1000)))  
  
  ; => [110 310 510 710 910 ....  99910]
```

```clojure
(do
  (def xform (comp (drop 2) (take 3)))
  
  (transduce xform conj [1 2 3 4 5 6]))  ; => [3 4 5]
```

```clojure
(do
  (def xform (map #(+ % 1)))
  
  (transduce xform + 10 [1 2 3 4])))  ; => 24
```
