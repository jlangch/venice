# Benchmarks & Profiling


## Benchmark

Venice's *benchmark* module measures the computation time of an expression. 

Benchmarking an expression incorporates four phases:
   1. Run the expression in a warm-up phase to allow the JIT compiler to do optimizations. 
   2. Run the garbage collector to isolate timings from GC state prior to testing 
   3. Runs the expression
   4. Statistically analyze the expression evaluations

### Signature

`(benchmark expr warmup-iterations iterations & options)`


### Example

```clojure
(do
  (load-module :benchmark)
  (bench/benchmark (+ 1 2 3 4) 1000000 30000))
```

The benchmark output: 

```text
Warmup...
GC...
Sampling...
Analyzing...
                      Samples :   30000
          Execution time mean : 672,000 ns
 Execution time std-deviation :  20,048 ns
Execution time lower quartile : 660,000 ns (25%)
Execution time upper quartile : 692,000 ns (75%)
Execution time lower quantile : 653,000 ns (2.5%)
Execution time upper quantile : 727,000 ns (97.5%)
                     Outliers :     326
```

### Outliers

A sample is marked as an outlier if its execution time is lower than `Q1 - 3 * IQR` or greater than `Q3 + 3 * IQR`. Where Q1 is the first or lower quartile, Q3 is the third or higher quartile, and IQR (Interquartile Range) is defined as `Q3 - Q1`. 


### Create a distribution chart 

#### Short warm-up phase

```clojure
(bench/benchmark (+ 1 2 3 4) 1000 300 :chart true)
```

```text
Warmup...
GC...
Sampling...
Analyzing...
                      Samples :    300
          Execution time mean : 14,877 µs
 Execution time std-deviation :  2,761 µs
Execution time lower quartile : 14,230 µs (25%)
Execution time upper quartile : 18,289 µs (75%)
Execution time lower quantile : 13,790 µs (2.5%)
Execution time upper quantile : 26,689 µs (97.5%)
                     Outliers :      5
Generating chart...
Quantization step width: 4,757 µs 
Saved chart to 'benchmark.png'.
```

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/benchmark1.png" width="300">


#### Long warm-up phase


```clojure
(bench/benchmark (+ 1 2 3 4) 100000 30000 :chart true)
```

```text
Warmup...
GC...
Sampling...
Analyzing...
                      Samples :   30000
          Execution time mean : 698,000 ns
 Execution time std-deviation :   9,098 ns
Execution time lower quartile : 694,000 ns (25%)
Execution time upper quartile : 705,000 ns (75%)
Execution time lower quantile : 687,000 ns (2.5%)
Execution time upper quantile : 727,000 ns (97.5%)
                     Outliers :     436
Generating chart...
Quantization step width: 288 ns
Saved chart to 'benchmark.png'.
```

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/benchmark2.png" width="300">


### References

- [Median](https://en.wikipedia.org/wiki/Median)
- [Quartile](https://en.wikipedia.org/wiki/Quartile)
- [Quantile](https://en.wikipedia.org/wiki/Quantile)
- [Interquartile Range](https://en.wikipedia.org/wiki/Interquartile_range)
- [Box Plot](https://en.wikipedia.org/wiki/Box_plot)


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
      (loop [i 0]
         (if (< i n)
            (recur (inc i))
            i)))
            
   (perf (sum 100000) 100 100)
   
   (println (prof :data-formatted "Metrics: loop")))
```

The metrics table shows four columns with the function name, the number of calls, the 
total and average time for the function's calls:

```text
-----------------------------------------------
Metrics: loop
-----------------------------------------------
user/_test  [       1]:    11,34 s             
user/sum    [     100]:    11,34 s    113,39 ms
inc         [10000000]:   697,57 ms       69 ns
<           [10000100]:   680,45 ms       68 ns
-----------------------------------------------
```



### Example: profiling fibonacci (case macro)

The profiler runs the sum function 5000 times as warm-up followed by 100 times to profile it. 

```clojure
(do
   (load-module :math)
  
   (defn fib [x]
     (loop [n x, a (math/bigint 0), b (math/bigint 1)]
       (case n
         0 a
         1 b 
         (recur (dec n) b (math/bigint-add a b)))))

   (perf (fib 50) 5000 100)
   
   (println (prof :data-formatted "Metrics: fibonacci")))
```

Metrics:

```text
--------------------------------------------------
Metrics: fibonacci
--------------------------------------------------
macroexpand      [ 55200]:   474,30 ms     8,59 us
user/_test       [     1]:   425,01 ms            
user/fib         [   100]:   424,83 ms     4,25 ms
math/bigint-add  [  4900]:   136,72 ms    27,90 us
math/bigint      [ 10000]:   129,93 ms    12,99 us
mapcat           [  5000]:    49,30 ms     9,86 us
cons             [215500]:    38,73 ms      179 ns
math/bigint?     [ 10000]:    24,26 ms     2,43 us
instance?        [ 10000]:    14,64 ms     1,46 us
concat           [ 30100]:    10,58 ms      351 ns
rest             [ 75300]:     8,05 ms      106 ns
list             [ 25100]:     5,40 ms      215 ns
partition        [  5000]:     3,98 ms      796 ns
first            [ 25100]:     2,99 ms      119 ns
second           [ 25100]:     2,79 ms      111 ns
.                [  5100]:     2,76 ms      540 ns
not-empty?       [ 25100]:     2,47 ms       98 ns
count            [ 10000]:     1,13 ms      112 ns
odd?             [ 10000]:   926,37 us       92 ns
==               [ 10000]:   898,61 us       89 ns
gensym           [  5000]:   892,00 us      178 ns
butlast          [  5000]:   813,65 us      162 ns
dec              [  4900]:   643,25 us      131 ns
last             [  5000]:   629,27 us      125 ns
long?            [   200]:    23,90 us      119 ns
--------------------------------------------------
```

Metrics with upfront macro expansion:

```text
-------------------------------------------------
Metrics: fibonacci
-------------------------------------------------
user/_test       [    1]:    52,60 ms            
user/fib         [  100]:    52,48 ms   524,83 us
math/bigint-add  [ 4900]:    35,54 ms     7,25 us
math/bigint      [10000]:    27,19 ms     2,72 us
math/bigint?     [10000]:    19,69 ms     1,97 us
instance?        [10000]:    10,28 ms     1,03 us
.                [ 5100]:     1,77 ms      346 ns
==               [10000]:     1,07 ms      107 ns
dec              [ 4900]:   623,26 us      127 ns
long?            [  200]:    23,65 us      118 ns
-------------------------------------------------
```


### Example: profiling fibonacci (nested if)

The profiler runs the sum function 5000 times as warm-up followed by 100 times to profile it. 

```clojure
(do
   (load-module :math)
     
   (defn fib [x]
      (loop [n x, a (math/bigint 0), b (math/bigint 1)]
        (if (= n 0)
          a
          (if (= n 1)
            b 
            (recur (dec n) b (math/bigint-add a b))))))

   (perf (fib 50) 5000 100)
   (println (prof :data-formatted "Metrics: fib")))
```

Metrics:

```text
-------------------------------------------------
Metrics: fib
-------------------------------------------------
user/_test       [    1]:   157,63 ms            
user/fib         [  100]:   157,49 ms     1,57 ms
macroexpand      [20400]:   150,93 ms     7,40 us
math/bigint-add  [ 4900]:   137,45 ms    28,05 us
math/bigint      [10000]:   131,57 ms    13,16 us
math/bigint?     [10000]:    22,08 ms     2,21 us
instance?        [10000]:    13,03 ms     1,30 us
cons             [51000]:    11,09 ms      217 ns
rest             [30600]:     3,68 ms      120 ns
concat           [10200]:     3,60 ms      352 ns
.                [ 5100]:     2,20 ms      431 ns
list             [10200]:     2,16 ms      211 ns
first            [10200]:     1,24 ms      121 ns
second           [10200]:     1,15 ms      112 ns
not-empty?       [10200]:     1,04 ms      102 ns
dec              [ 4900]:     1,02 ms      207 ns
=                [10000]:   802,28 us       80 ns
long?            [  200]:    23,35 us      116 ns
-------------------------------------------------
```


Metrics with upfront macro expansion:

```text
-------------------------------------------------
Metrics: fib
-------------------------------------------------
user/_test       [    1]:    51,94 ms            
user/fib         [  100]:    51,82 ms   518,18 us
math/bigint-add  [ 4900]:    36,35 ms     7,42 us
math/bigint      [10000]:    27,54 ms     2,75 us
math/bigint?     [10000]:    19,47 ms     1,95 us
instance?        [10000]:    10,40 ms     1,04 us
.                [ 5100]:     1,83 ms      359 ns
=                [10000]:     1,08 ms      107 ns
dec              [ 4900]:   662,50 us      135 ns
long?            [  200]:    23,84 us      119 ns
-------------------------------------------------
```
