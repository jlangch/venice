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
  (bench/benchmark (+ 1 2 3 4) 100000 3000))
```

The benchmark output: 

```text
Warmup...
GC...
Sampling...
Analyzing...
                      Samples :    3000
          Execution time mean : 825,000 ns
 Execution time std-deviation :  50,529 ns
Execution time lower quartile : 807,000 ns (25%)
Execution time upper quartile : 904,000 ns (75%)
Execution time lower quantile : 802,000 ns (2.5%)
Execution time upper quantile : 943,000 ns (97.5%)
                     Outliers :      25
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
          Execution time mean : 801,000 ns
 Execution time std-deviation :   7,907 ns
Execution time lower quartile : 798,000 ns (25%)
Execution time upper quartile : 805,000 ns (75%)
Execution time lower quantile : 778,000 ns (2.5%)
Execution time upper quantile : 905,000 ns (97.5%)
                     Outliers :    4196
Generating chart...
Quantization step width: 30,063 µs 
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
user/_test  [       1]:    11,24 s             
user/sum    [     100]:    11,24 s    112,38 ms
inc         [10000000]:  1044,65 ms      104 ns
<           [10000100]:   993,30 ms       99 ns
-----------------------------------------------
```



### Example: profiling fibonacci (case macro)

The profiler runs the sum function 100 times as warm-up followed by 100 times to profile it. 

```clojure
(do
   (load-module :math)
  
   (defn fib [x]
     (loop [n x, a (math/bigint 0), b (math/bigint 1)]
       (case n
         0 a
         1 b 
         (recur (dec n) b (math/bigint-add a b)))))

   (perf (fib 50) 100 100)
   
   (println (prof :data-formatted "Metrics: fibonacci")))
```

Metrics:

```text
--------------------------------------------------
Metrics: fibonacci
--------------------------------------------------
user/_test       [     1]:   387,50 ms            
user/fib         [   100]:   387,18 ms     3,87 ms
macroexpand      [ 55200]:   371,49 ms     6,73 us
math/bigint-add  [  4900]:   116,72 ms    23,82 us
math/bigint      [ 10000]:   108,01 ms    10,80 us
mapcat           [  5000]:    54,60 ms    10,92 us
cons             [140200]:    33,47 ms      238 ns
math/bigint?     [ 10000]:    25,41 ms     2,54 us
instance?        [ 10000]:    14,65 ms     1,46 us
list             [ 50200]:    12,35 ms      245 ns
rest             [ 75300]:    11,53 ms      153 ns
partition        [  5000]:     4,59 ms      917 ns
first            [ 25100]:     4,16 ms      165 ns
second           [ 25100]:     3,79 ms      151 ns
.                [  5100]:     2,79 ms      547 ns
concat           [  5000]:     2,58 ms      516 ns
count            [ 10000]:     1,65 ms      164 ns
odd?             [ 10000]:     1,39 ms      138 ns
==               [ 10000]:     1,36 ms      136 ns
gensym           [  5000]:     1,09 ms      218 ns
butlast          [  5000]:   943,91 us      188 ns
dec              [  4900]:   897,80 us      183 ns
last             [  5000]:   826,45 us      165 ns
long?            [   200]:    34,05 us      170 ns
--------------------------------------------------
```



### Example: profiling fibonacci (nested if)

The profiler runs the sum function 100 times as warm-up followed by 100 times to profile it. 

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

   (perf (fib 50) 100 100)
   (println (prof :data-formatted "Metrics: fib")))
```

Metrics:

```text
-------------------------------------------------
Metrics: fib
-------------------------------------------------
user/_test       [    1]:   127,46 ms            
user/fib         [  100]:   127,32 ms     1,27 ms
math/bigint-add  [ 4900]:   107,30 ms    21,90 us
math/bigint      [10000]:   101,31 ms    10,13 us
macroexpand      [20400]:    92,28 ms     4,52 us
math/bigint?     [10000]:    22,26 ms     2,23 us
instance?        [10000]:    12,16 ms     1,22 us
cons             [20400]:     5,61 ms      274 ns
rest             [30600]:     4,89 ms      159 ns
list             [20400]:     4,33 ms      212 ns
.                [ 5100]:     2,24 ms      438 ns
first            [10200]:     1,58 ms      155 ns
second           [10200]:     1,44 ms      140 ns
=                [10000]:     1,17 ms      116 ns
dec              [ 4900]:     1,11 ms      226 ns
long?            [  200]:    30,29 us      151 ns
-------------------------------------------------
```
