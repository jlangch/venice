# Recursion


Venice does not support automated tail call optimization. The _recur_ syntax 
is a way to mimic TCO for self-recursion. In addition the _trampoline_ function for 
mutual recursion is available for more involved forms of recursion.


## simple recursion

```clojure
(do
  (load-module :math)
  
  (defn factorial [n] 
    (if (<= n 1) 
        1 
        (math/bigint-mul n (factorial (dec n))))))

  (factorial 2)     ; => 2
  (factorial 5)     ; => 120
  (factorial 200)   ; => 78865786736479050355236...00000000 (375 digits)
  (factorial 4000)  ; => boooom...
)
```

```clojure
(do
  (load-module :math)

  (defmulti factorial identity)
  (defmethod factorial 0 [_] 1)
  (defmethod factorial :default [n] (math/bigint-mul n (factorial (dec n))))

  (factorial 5)     ; => 120
  (factorial 200)   ; => 78865786736479050355236...00000000 (375 digits)
  (factorial 4000)  ; => boooom...
)
```

Simple recursion a few thousand calls deep throws a _StackOverflowError_.


## self-recursive calls (loop - recur)

```clojure
(do
   (defn sum [n]
      (loop [cnt n, acc 0]
         (if (zero? cnt)
             acc
             (recur (dec cnt) (+ acc cnt)))))

   (sum 100000)) ; => 5000050000
```

```clojure
(do
   (load-module :math)
  
   (defn factorial [x]
      (loop [n x, acc (math/bigint 1)]
         (if (== n 1)
             acc
             (recur (dec n) (math/bigint-mul acc n)))))
    
   (factorial 10000)) ; => 441134000376444377299...00000000  (35660 digits)
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
 
   (defn factorial
      ([n] #(factorial n (math/bigint 1)))
      ([n acc] (if (< n 2) 
                   acc 
                   #(factorial (dec n) (math/bigint-mul acc n)))))

   (trampoline (factorial 10000)))
```
