# Recursion


Venice does not support automated tail call optimization. The _recur_ syntax 
is a way to mimic TCO for self-recursion and the _trampoline_ function for 
mutual recursion is available for more involved forms of recursion.


## Simple recursion

```clojure
(do
  (load-module :math)
  
  (defn mul [x y] (math/bigint-mul (math/bigint x) (math/bigint y)))
  
  (defn fact [x] (if (<= x 1) 1 (mul (fact (dec x)) x))))
```

`(fact 2) -> 2`

`(fact 4) -> 32`

`(fact 4000) -> booom...`

Simple recursion a few thousand calls deep throws a _StackOverflowError_.


## self-recursive calls (loop - recur)

```clojure
(do
   (defn sum [n]
      (loop [cnt n, acc 0]
         (if (zero? cnt)
            acc
            (recur (dec cnt) (+ acc cnt)))))

   (sum 100000))
```

```clojure
(do
  (load-module :math)
  
  (defn mul [x y] (math/bigint-mul x (math/bigint y)))
  
  (defn factorial [x]
    (loop [n x, acc (math/bigint 1)]
        (if (== n 1)
            acc
            (recur (dec n) (mul acc n)))))
    
  (factorial 10000))
```


## mutually recursive calls (trampoline)

The function `trampoline` is defined simplified as

```clojure
(defn trampoline [f] 
      (loop [f f]
        (let [ret (f)]
          (if (fn? ret) (recur ret) ret)))))
```

Examples:

```clojure
(do
  (defn is-odd? [n]
    (if (zero? n)
      false
      #(is-even? (dec n))))

  (defn is-even? [n]
    (if (zero? n)
      true
      #(is-odd? (dec n))))

  (trampoline (is-odd? 10000)))
```

```clojure
(do
  (load-module :math)
 
  (defn mul [x y] (math/bigint-mul x (math/bigint y)))
 
  (defn factorial
    ([n] #(factorial n (math/bigint 1)))
    ([n acc] (if (< n 2) 
                 acc 
                 #(factorial (dec n) (mul acc n)))))

  (trampoline (factorial 10000)))
```
