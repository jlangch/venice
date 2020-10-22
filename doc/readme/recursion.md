# Recursion

Imperative languages like Java offer looping constructs like *while* or *for* loops.
These looping constructs are based on mutable data.

In this Java example the mutable variable *isDone* is used to exit the loop:

```java
void doSomething() {
  boolean isDone = false;
  while (!isDone) {
    isDone = true;
  }
}
```

But how is it possible to write a *while* loop when the expression that the loop is 
testing is immutable?

The answer is: through recursion!

```java
void doSomething() {
  doSomething(false);
}

void doSomething(boolean isDone) {
  if (!isDone) {
    doSomething(true);
  }
}
```

The drawback of this simple recursion is the large amount of memory overhead 
because of added stack frames for each recursion iteration.

Functional languages with immutable data structures support *tail call optimization* 
(TCO) to provide memory efficient recursion. While Venice does not support 
automated tail call optimization it supports self recursion through the
*loop..recur* syntax. This is a way to mimic TCO for self-recursion. 

In addition Venice provides the  _trampoline_  function for mutual recursion for more 
involved forms of recursion.


## simple recursion

To illustrate the problem with simple recursion consuming stack frames for each 
iteration, let's look at simple recursion example to compute the factorial for a
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

Simple recursion a few thousand calls deep throws a *StackOverflowError*.

*Note: The recursive call to 'factorial' in these two simple recursion examples is not in tail position. Recursive functions like this can not be tail call optimized!*			


## self-recursive calls (loop - recur)

Venice self-recursive calls do not consume a new a stack frame for every new 
recursion level and have a constant memory usage. It's the only non-stack-consuming 
looping construct in Venice. To make it work the `recur` expression must be in 
*tail position*. This way Venice can turn the recursive *loop..recur* construct 
behind the scene into a plain loop.

*Definition:*  The tail position is a position which an expression would return 
a value from. There are no more forms evaluated after the form in the tail 
position is evaluated.
 

Recursively sum up the numbers 0..n:

```clojure
;; Definition:
;;   sum 0 -> 0
;;   sum n -> n + sum (n - 1)
(do
   (defn sum [n]
      (loop [cnt n, acc 0]
         (if (zero? cnt)
             acc
             (recur (dec cnt) (+ acc cnt)))))

   (sum 100000)) ; => 5000050000
```

Recursively compute the factorial of a number:

```clojure
;; Definition:
;;   factorial 1 -> 1
;;   factorial n -> n * factorial (n -1)
(do
   (defn factorial [x]
      (loop [n x, acc 1N]
         (if (== n 1)
             acc
             (recur (dec n) (* acc n)))))
    
   (factorial 5)      ; => 120N
   (factorial 10000)) ; => 284625968091...00000N  (35661 digits)
```

Recursively compute the fibonacci numbers (0 1 1 2 3 5 ...):

```clojure
;; Definition:
;;   fib 0 -> 0
;;   fib 1 -> 1
;;   fib n -> fib (n - 2) + fib (n - 1)
(do
   (defn fib [x]
      (loop [n x, a 0N, b 1N]
         (case n
            0   a
            1   b
            (recur (dec n) b (+ a b)))))
    
   (fib 6)       ; => 8N
   (fib 100000)) ; => 259740693472217...28746875N  (20901 digits)
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

