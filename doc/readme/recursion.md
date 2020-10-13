# Recursion


Venice does not support automated tail call optimization (TCO). The  _recur_  syntax 
is a way to mimic TCO for self-recursion. In addition the  _trampoline_  function for 
mutual recursion is available for more involved forms of recursion.


## simple recursion

```clojure
(do
  (defn factorial [n] 
    (if (<= n 1) 
        1N 
        (* (bigint n) (factorial (dec n)))))

  (factorial 2)     ; => 2N
  (factorial 5)     ; => 120N
  (factorial 200)   ; => 78865786736479050355236...00000000N (375 digits)
  (factorial 4000)  ; => boooom...
)
```

```clojure
(do
  (defmulti factorial identity)
  (defmethod factorial 0 [_] 1N)
  (defmethod factorial :default [n] (* (bigint n) (factorial (dec n))))

  (factorial 5)     ; => 120N
  (factorial 200)   ; => 78865786736479050355236...00000000N (375 digits)
  (factorial 4000)  ; => boooom...
)
```

Simple recursion a few thousand calls deep throws a *StackOverflowError*.

*Note: The recursive call to 'factorial' in these two simple recursion examples is not in tail position. Recursive functions like this can not be tail call optimized!*			


## self-recursive calls (loop - recur)

Venice self-recursive calls do not consume stack space. It's the only
non-stack-consuming looping construct in Venice. The `recur` expression
must be in tail position.

*Definition:*  The tail position is a position which an expression would return 
a value from. There are no more forms evaluated after the form in the tail 
position is evaluated.
 

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
   (defn factorial [x]
      (loop [n x, acc 1N]
         (if (== n 1)
             acc
             (recur (dec n) (* acc n)))))
    
   (factorial 5)      ; => 120N
   (factorial 10000)) ; => 284625968091...00000N  (35661 digits)
```


## Recursion with lazy sequences

Lazy Fibonacci number sequence computed by a recursive function:

```clojure
(do
   (defn fib
     ([]    (fib 0N 1N))
     ([a b] (cons a #(fib b (+ a b)))))

   (doall (take 7 (fib))))  ; => (0 1 1 2 3 5 8)
```

Factorial for large numbers:

```clojure
(do
   (defn factorial
      ([]      (factorial 1 1N))
      ([x]     (first (drop (dec x) (factorial))))
      ([n acc] (cons acc #(factorial (inc n) (* acc (inc n))))))

   (factorial 5)      ; => 120N 
   (factorial 1000))  ; => 284625968091...00000N  (35661 digits)
```


## mutually recursive calls (trampoline)

`trampoline` can be used to convert algorithms requiring mutual
recursion without stack consumption. Calls f, if f returns a function, 
calls that function with no arguments, and continues to repeat, until 
the return value is not a function, then returns that 
non-function value.

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
   (defn factorial
      ([n] #(factorial n 1N))
      ([n acc] (if (< n 2) 
                   acc 
                   #(factorial (dec n) (* acc n)))))

   (trampoline (factorial 10000)))
```


## Automated tail call optimization (TCO)

Venice has experimental automated tail call optimization built-in, but it is 
not yet enabled for production builds.

```clojure
(do
  (defn factorial [n] (factorial* n 1N))

  (defn factorial* [n acc] 
    (if (== n 1)
        acc
        (factorial* (dec n) (* acc n))))
        
  (factorial 200))
```

```clojure
(do
  (defn factorial2 [n] 
    (let [fact (fn [n acc]
                  (if (== n 1)
                      acc
                      (fact (dec n) (* acc n))))]
      (fact n 1N)))
      
  (factorial2 200))
```

Note: tail call recursive functions, can always be written in terms of a 
reducing (folding) function. E.g.:

```clojure
(reduce * 1N (range 1 201))  ;; reducing factorial
```

