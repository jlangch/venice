# Recursion


## Introduction

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
(TCO) to provide memory efficient recursion. Venice supports 
tail call optimization and self recursion through the
*loop..recur* syntax. Self recursion is a way to mimic TCO.  

In addition Venice provides the *trampoline* function for mutual recursion for more 
involved forms of recursion.


## Simple Recursion

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
  (factorial 4000)  ; => boooom...  (stack overflow)
)
```

Simple recursion with [multimethods](multi-methods-and-protocols.md#multimethods):

```clojure
(do
  (defmulti factorial identity)
  (defmethod factorial 0N [_] 1N)
  (defmethod factorial :default [n] (* (bigint n) (factorial (dec (bigint n)))))

  (factorial 2)     ; => 2N
  (factorial 5)     ; => 120N
  (factorial 200)   ; => 78865786736479050355236...00000000N (375 digits)
  (factorial 4000)  ; => boooom...  (stack overflow)
)
```

Simple recursion a few thousand calls deep throws a *StackOverflowError*.

*Note: The recursive call to 'factorial' in this example is not in tail position. Thus it can not be tail call optimized!*			


## Self-Recursive Calls (loop - recur)

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


Example 3: Recursively compute the Fibonacci numbers (0 1 1 2 3 5 8 ...):

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

   (factorial 5)       ; => 120N 
   (factorial 10000))  ; => 284625968091...00000N  (35661 digits)
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


## Tail Call Optimization (TCO) 

Venice has support for automatic *tail call optimization*. The recursive call must be in tail 
position.


```clojure
(do
  (defn factorial
    ([n]     (factorial n 1N))
    ([n acc] (if (== n 1)
               acc
               (factorial (dec n) (* acc n)))))
 
  (factorial 5)       ; => 120N 
  (factorial 10000))  ; => 284625968091...00000N  (35661 digits)
```


## Recursion vs Folding

Tail call recursive functions, can always be written in terms of a reducing (folding) 
function. E.g.:

```clojure
(do
  (defn factorial [n]
    ;; reducing factorial
    (reduce * 1N (range 1 (inc n))))
    
  (factorial 5)       ; => 120N 
  (factorial 10000))  ; => 284625968091...00000N  (35661 digits)
```

But not all recursive functions can be transformed into a tail recursive function
and translated into a loop. The [Ackermann's function](https://en.wikipedia.org/wiki/Ackermann_function)
is such an example of a non [primitive recursive function](https://en.wikipedia.org/wiki/Primitive_recursive_function) that can not be de-recursed into loops.


## Recursion and Memoization

For some recursive algorithms *memoization* can speed up computation dramatically:

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


Please not that this naive approach is not working:

```clojure
(do
  (defn fib-simple [n]
    (if (< n 2)
      (max n 0)
      (+ (fib-simple (- n 1)) (fib-simple (- n 2)))))

  (def fib-memoize (memoize fib-simple))
  
  (fib-memoize 30))
```

*memoization* is doing a good job in computing fibonacci numbers using 
simple recursion. It eliminates the recurring computation of the predecessors
values.

Nevertheless there are recursive algorithms like the *Ackermann* function
where memoization has to raise its arms.



## Compare recursion efficiency

To see how efficient tail call optimization for recursion is we compare 
simple recursion with self recursion applied to computing Fibonacci numbers. 

_Note: all examples run with upfront macro expansion enabled._

```clojure
(do
  (defn fib-simple [n]
    (if (< n 2)
      n
      (+ (fib-simple (- n 1)) (fib-simple (- n 2)))))

  (defn fib-tco 
    ([n]  
      (fib-tco n 0N 1N))
    ([n a b]
      (case n
        0  a
        1  b
        (fib-tco (dec n) b (+ a b)))))
 
  (defn fib-loop-recur [x]
    (loop [n x, a 0N, b 1N]
      (case n
        0  a
        1  b
        (recur (dec n) b (+ a b)))))

  (def fib-memoize
    (memoize
      (fn [n]
        (case n
          0  0
          1  1
          (+ (fib-memoize (- n 1)) (fib-memoize (- n 2))))))))
         
   ; (load-module :benchmark ['benchmark :as 'b])
   ; (b/benchmark (fib-simple 30) 5 5)
   ; (b/benchmark (fib-tco 30) 5000 1000)
   ; (b/benchmark (fib-loop-recur 30) 5000 1000)
   ; (time (fib-memoize 30))  ;; note: memoizing functions cannot be benchmarked!
   
   
  ;; run on MacBook Air M2, with 'macroexpand' enabled
  ;; +----------------------+------------+
  ;; | (fib-simple 30)      |    1.171s  |  
  ;; | (fib-tco 30)         |   31.286µs |   
  ;; | (fib-loop-recur 30)  |   27.946µs |  
  ;; | (fib-memoize 30)     |    2.54ms  |     
  ;; +----------------------+------------+   
```

