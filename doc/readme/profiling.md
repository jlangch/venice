#Profiling


All profiling did run on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz) 
with a Java 11 server VM.



## Profiling

Venice supports simple code profiling to analyze execution performance.

The functions `perf` and `prof` work hand in hand.
The `perf` macro profiles Venice functions and `prof` controls and prints 
the gathered profile metrics.


### perf

```clojure
(perf expr warmup-iterations test-iterations)
```

Runs a profiling session on the given expression. 

Runs the profiling in three phases: 
   1. Run the expression in a warm-up phase to allow the JIT compiler to do optimizations. 
   2. Run the garbage collector to isolate timings from GC state prior to testing 
   3. Runs the expression under profiling.


### prof

```clojure
(prof opts)
```

Controls the code profiling.

- `(prof :on)`   turn profiler on  
- `(prof :off)`   turn profiler off  
- `(prof :status)`   returns the profiler on/off staus  
- `(prof :clear)`   clear profiling data captured so far  
- `(prof :data)`   returns the profiling data as map  
- `(prof :data-formatted)`   returns the profiling data as formatted text  
- `(prof :data-formatted "Metrics test")`   returns the profiling data as formatted text with a title  



### Example: profiling a loop

The profiler runs the sum function 100 times as warm-up followed by 100 times to profile it. 

```clojure
(do
   (defn sum [n]
      (loop [i 0 acc 0]
         (if (< i n)
            (recur (inc i) (+ acc i))
            acc)))
            
   (perf (sum 100000) 100 100)
   
   (println (prof :data-formatted "Metrics: loop")))
```

_Note: a faster way to sum integers is using a reduction:_ `(reduce + (range 100000))`

The metrics table shows four columns with the function name, the number of calls, the 
total and average time for the function's calls:

```text
-----------------------------------------------
Metrics: loop
-----------------------------------------------
user/_test  [       1]:  7509.04 ms            
user/sum    [     100]:  7508.21 ms    75.08 ms
+           [10000000]:   551.81 ms       55 ns
inc         [10000000]:   530.40 ms       53 ns
<           [10000100]:   493.70 ms       49 ns
-----------------------------------------------
```

Analysis loop-recur performance:

* `(sum 100000)` takes 75.1ms
* the functions `<`, `inc` and `+` take 100'000 * 157ns = 15.7ms
* the loop-recur overhead is (75.1ms - 15.7ms) / 100'000 = 594ns
* every loop-recur iteration takes 594ns to process the `if` logic, initiate a new 
  iteration, and setup the local environment with the loop variables.

**Profiler overhead**

To compare what the profiler costs

```text
(time (dorun 100 (sum 100000)))

;; Elapsed time: 5.21 s
```

The profiler adds an overhead of 50% to collect the profile data.



### Example: profiling fibonacci (case macro)

The profiler runs the sum function 5000 times as warm-up followed by 100 times to profile it. 

```clojure
(do
   (defn fib [x]
     (loop [n x, a 0N, b 1N]
       (case n
         0 a
         1 b 
         (recur (dec n) b (+ a b)))))

   (perf (fib 50) 5000 100)
   
   (println (prof :data-formatted "Metrics: fibonacci")))
```

Metrics:

```text
----------------------------------------------
Metrics: fibonacci
----------------------------------------------
macroexpand  [ 34800]:   165.50 ms     4.76 us
user/_test   [     1]:   149.26 ms            
user/fib     [   100]:   149.16 ms     1.49 ms
cond[m]      [ 14900]:    68.70 ms     4.61 us
case[m]      [  5000]:    62.22 ms    12.44 us
when[m]      [ 14900]:    25.90 ms     1.74 us
mapcat       [  5000]:    18.82 ms     3.76 us
cons         [164500]:    11.59 ms       70 ns
concat       [ 19900]:     5.44 ms      273 ns
rest         [ 44700]:     3.22 ms       72 ns
partition    [  5000]:     2.03 ms      406 ns
first        [ 14900]:     1.23 ms       82 ns
not-empty?   [ 14900]:     1.10 ms       73 ns
second       [ 14900]:   892.99 us       59 ns
list         [ 14900]:   837.29 us       56 ns
odd?         [ 10000]:   760.59 us       76 ns
==           [ 10000]:   712.49 us       71 ns
gensym       [  5000]:   709.53 us      141 ns
count        [ 10000]:   653.92 us       65 ns
+            [  4900]:   577.24 us      117 ns
butlast      [  5000]:   532.42 us      106 ns
last         [  5000]:   518.53 us      103 ns
dec          [  4900]:   492.42 us      100 ns
----------------------------------------------
```

Metrics with upfront macro expansion:

```text
--------------------------------------------
Metrics: fibonacci
--------------------------------------------
user/_test  [    1]:    10.37 ms            
user/fib    [  100]:    10.29 ms   102.86 us
==          [10000]:   827.60 us       82 ns
+           [ 4900]:   597.74 us      121 ns
dec         [ 4900]:   434.54 us       88 ns
--------------------------------------------
```


### Example: profiling fibonacci (nested if)

The profiler runs the sum function 5000 times as warm-up followed by 100 times to profile it. 

```clojure
(do
   (defn fib [x]
      (loop [n x, a 0N, b 1N]
        (if (zero? n)
          a
          (if (= n 1)
            b 
            (recur (dec n) b (+ a b))))))

   (perf (fib 50) 5000 100)
   (println (prof :data-formatted "Metrics: fib")))
```

Metrics:

```text
-------------------------------------------
Metrics: fib
-------------------------------------------
user/_test  [   1]:     8.99 ms            
user/fib    [ 100]:     8.93 ms    89.33 us
+           [4900]:   613.33 us      125 ns
dec         [4900]:   479.36 us       97 ns
zero?       [5000]:   412.55 us       82 ns
=           [5000]:   401.40 us       80 ns
-------------------------------------------
```
