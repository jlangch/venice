# Recursion

Imperative languages like *Java* offer looping constructs like *while* or *for* loops.
These looping constructs are based on mutable data.

In this *Java* example the mutable variable *isDone* is used to exit the loop:

```java
void doSomething() {
  boolean isDone = false;
  while (!isDone) {
    isDone = true;
  }
}
```

But how is it possible to write a *while* loop if the value that the loop is 
testing is immutable and functions are pure?

The answer is: recursion!

```java
void doSomething() {
  doSomething(false);
}

void doSomething(final boolean isDone) {
  if (!isDone) {
    doSomething(true);
  }
}
```

The drawback of this simple recursion is the large amount of memory overhead 
because of added stack frames for each recursion iteration.

Functional languages with immutable data structures support **tail call optimization**
(TCO) to provide memory efficient recursion. While Venice supports 
automated tail call optimization and self recursion through the
*loop..recur* syntax. The latter is a way to mimic TCO. 

In addition Venice provides the *trampoline* function for mutual recursion for more 
involved forms of recursion.


## simple recursion

To illustrate the problem with simple recursion consuming stack frames for each 
iteration, let's look at a simple recursion example to compute the factorial for a
number.

The computation of factorial numbers is defined as
- factorial 1 -> 1
- factorial n -> n * factorial (n - 1)

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

Simple recursion with [multimethods](multi-methods-and-protocols.md#multimethods):

```clojure
(do
  (defmulti factorial identity)
  (defmethod factorial 0 [_] 1)
  (defmethod factorial :default [n] (* n (factorial (dec n))))

  (factorial 5)     ; -> 120
  (factorial 4000)  ; => boooom...
)
```

Simple recursion a few thousand calls deep throws a *StackOverflowError*.

*Note: The recursive call to 'factorial' in this example is not in tail position. Thus it can not be tail call optimized!*			


## self-recursive calls (loop - recur)

Venice self-recursive calls do not consume a new a stack frame for every new 
recursion iteration and have a constant memory usage. It's the only non-stack-consuming 
looping construct in Venice. To make it work the `recur` expression must be in 
*tail position*. This way Venice can turn the recursive *loop..recur* construct 
behind the scene into a plain loop.

*Definition:*  The tail position is a position which an expression would return 
a value from. There are no more forms evaluated after the form in the tail 
position is evaluated.

*Remember:*  Venice offers various alternative solutions to recursion to solve
loops like `(+ 1 2 3 4 5 6)` to sum up a list of numbers or the powerful `reduce` 
function: `(reduce + [1 2 3 4 5])`. Many Venice functions accept an arbitrary 
number of arguments to prevent you from writing loops.


Example 1: Recursively sum up the numbers 0..n:

```clojure
;; Definition:
;;   sum 0 -> 0
;;   sum n -> n + sum (n - 1)
(do
   (defn sum [n]
      ;; the transformed recursion uses an accumulator for intermediate results
      (loop [cnt n, acc 0]
         (if (zero? cnt)
             acc
             (recur (dec cnt) (+ acc cnt)))))

   (sum 100000)) ; => 5000050000
```


Example 2: Recursively compute the factorial of a number:

```clojure
;; Definition:
;;   factorial 1 -> 1
;;   factorial n -> n * factorial (n - 1)
(do
   (defn factorial [x]
      ;; the transformed recursion uses an accumulator for intermediate results
      (loop [n x, acc 1N]
         (if (== n 1)
             acc
             (recur (dec n) (* acc n)))))
    
   (factorial 5)      ; => 120N
   (factorial 10000)) ; => 284625968091...00000N  (35661 digits)
```


Example 3: Recursively compute the fibonacci numbers (0 1 1 2 3 5 ...):

```clojure
;; Definition:
;;   fib 0 -> 0
;;   fib 1 -> 1
;;   fib n -> fib (n - 2) + fib (n - 1)
(do
   (defn fib [x]
      (loop [n x, a 0N, b 1N]
         (case n
            0  a
            1  b
            (recur (dec n) b (+ a b)))))
    
   (fib 6)       ; => 8N
   (fib 100000)) ; => 259740693472217...28746875N  (20901 digits)
```


## Recursion with lazy sequences

Example 1: Lazy Fibonacci number sequence computed by a recursive function:

```clojure
(do
   (defn fib
     ([]    (fib 0N 1N))
     ([a b] (cons a #(fib b (+ a b)))))

   (doall (take 7 (fib))))  ; => (0 1 1 2 3 5 8)
```


Example 2: Factorial numbers:

```clojure
(do
   (defn factorial
      ([]      (factorial 1 1N))
      ([x]     (first (drop (dec x) (factorial))))
      ([n acc] (cons acc #(factorial (inc n) (* acc (inc n))))))

   (factorial 5)      ; => 120N 
   (factorial 1000))  ; => 284625968091...00000N  (35661 digits)
```


## Mutually recursive calls (trampoline)

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

Venice has support for automated tail call optimization. The recursive 
call must be in tail position.


```clojure
(do
  (defn factorial
    ([n]     (factorial n 1N))
    ([n acc] (if (== n 1)
               acc
               (factorial (dec n) (* acc n)))))
 
  (factorial 5))
```


## Recursion vs Folding

Tail call recursive functions, can always be written in terms of a reducing (folding) 
function. E.g.:

```clojure
(do
  (defn factorial [n]
    ;; reducing factorial
    (reduce * 1N (range 1 (inc n))))
    
(factorial 5)) 
```

But not all recursive functions can be transformed into a tail recursive function
and translated into a loop. The [Ackermann's function](https://en.wikipedia.org/wiki/Ackermann_function)
is such an example of a non [primitive recursive function](https://en.wikipedia.org/wiki/Primitive_recursive_function) that can not be de-recursed into loops.


## Recursion and Memoization

While *memoization* is doing a good job computing fibonacci numbers using 
simple recursion it has to raise its arms with the *Ackermann* function.

```clojure
(do
  (def fibonacci
    (memoize
      (fn [n]
        (cond
          (<= n 0) 0
          (< n 2) 1
          :else (+ (fibonacci (- n 1)) (fibonacci (- n 2)))))))

  (time (fibonacci 25)))
```


## Compare recursion efficiency

To see how efficient tail call optimization for recursion is we compare 
simple recursion with self recursion applied to computing fibonacci numbers. 
See the execution time and the number of function calls the profiler reveals.

*Note: both examples run with upfront macro expansion enabled.*

```clojure
(do
  (defn fib-simple [n]
    (if (< n 2)
      n
      (+ (fib-simple (- n 1)) (fib-simple (- n 2)))))
 
  (defn fib-tco [x]
    (loop [n x, a 0N, b 1N]
      (case n
        0  a
        1  b
        (recur (dec n) b (+ a b)))))

  (perf (fib-simple 20) 100 100)
  (println (prof :data-formatted "Metrics: fib-simple"))
 
  (println)
  
  (perf (fib-tco 20) 100 100)
  (println (prof :data-formatted "Metrics: fib-tco")))
```

The profiler reveals that the TCO variant is way more efficient. The simple recursion 
computes the same fibonacci number over and over again. Only with memoization it almost
competes with the TCO variant. Moreover the simple recursion suffers from a memory problem 
and stack overflow when applied for larger numbers.

The TCO variant is more than 200 times faster than the simple recursion.

```text
Elapsed time for a single invocation
  fib-simple:   16.61 ms   (1661.10 ms / 100)
  fib-tco:      70.43 us
```

The simple recursive functions 'fib-simple' is called 13'529 times to calculate
(fib-simple 20) a single time! This is the reason for being so much slower - it's
the price for the elegance.

The number of recursive calls to 'fib-simple':

```text
                  value   #calls  
(fib-simple 1)       1        1
(fib-simple 2)       1        3
(fib-simple 3)       2        5 
(fib-simple 4)       3        9
(fib-simple 5)       5       15
(fib-simple 6)       8       25
(fib-simple 7)      13       41
(fib-simple 8)      21       67
(fib-simple 9)      34      109
(fib-simple 10)     55      177
(fib-simple 11)     89      287
(fib-simple 12)    144      465
(fib-simple 13)    233      753
(fib-simple 14)    377     1219
(fib-simple 15)    610     1973
```


```text
---------------------------------------------------
Metrics: fib-simple    100 calls to (fib-simple 20)
---------------------------------------------------
user/fib-simple  [1352900]:    21.02 s     15.54 us
user/_test       [      1]:  1661.10 ms           
-                [1352800]:    97.24 ms       71 ns
<=               [1352900]:    94.24 ms       69 ns
+                [ 676400]:    47.95 ms       70 ns
---------------------------------------------------

---------------------------------------------------
Metrics: fib-tco          100 calls to (fib-tco 20)
---------------------------------------------------
user/_test       [   1]:        7.20 ms           
user/fib-tco     [ 100]:        7.04 ms    70.43 us
==               [4000]:      390.69 us       97 ns
+                [1900]:      310.55 us      163 ns
dec              [1900]:      251.60 us      132 ns
---------------------------------------------------
```

*Please note that the Venice profiler is also accumulating the elapsed time recursively for simple recursive functions resulting in a wrong value! The Venice profiler always reports the 'time with children' for a function.*

