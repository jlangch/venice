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
                      Samples : 3000
          Execution time mean : 825,000 ns
 Execution time std-deviation : 50,529 ns
Execution time lower quartile : 807,000 ns (25%)
Execution time upper quartile : 904,000 ns (75%)
Execution time lower quantile : 802,000 ns (2.5%)
Execution time upper quantile : 943,000 ns (97.5%)
                     Outliers : 25
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
                      Samples : 300
          Execution time mean : 14,877 µs
 Execution time std-deviation : 2,761 µs
Execution time lower quartile : 14,230 µs (25%)
Execution time upper quartile : 18,289 µs (75%)
Execution time lower quantile : 13,790 µs (2.5%)
Execution time upper quantile : 26,689 µs (97.5%)
                     Outliers : 5
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
                      Samples : 30000
          Execution time mean : 801,000 ns
 Execution time std-deviation : 7,907 ns
Execution time lower quartile : 798,000 ns (25%)
Execution time upper quartile : 805,000 ns (75%)
Execution time lower quantile : 778,000 ns (2.5%)
Execution time upper quantile : 905,000 ns (97.5%)
                     Outliers : 4196
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
macroexpand      [134000]:   651,27 ms     4,86 us
user/_test       [     1]:   618,24 ms            
user/fib         [   100]:   617,99 ms     6,18 ms
math/bigint-add  [  4900]:   366,27 ms    74,75 us
math/bigint      [ 10000]:   359,15 ms    35,91 us
cons             [219000]:    52,45 ms      239 ns
mapcat           [  5000]:    50,36 ms    10,07 us
list             [129000]:    27,22 ms      210 ns
rest             [193500]:    27,02 ms      139 ns
math/bigint?     [  9800]:    23,73 ms     2,42 us
instance?        [  9800]:    14,08 ms     1,44 us
first            [ 64500]:     8,97 ms      139 ns
second           [ 64500]:     8,62 ms      133 ns
partition        [  5000]:     4,40 ms      879 ns
.                [  5100]:     3,07 ms      601 ns
concat           [  5000]:     2,41 ms      481 ns
==               [ 10000]:     1,49 ms      149 ns
count            [ 10000]:     1,48 ms      147 ns
string?          [ 10000]:     1,42 ms      141 ns
long?            [ 10000]:     1,40 ms      139 ns
double?          [  9800]:     1,37 ms      140 ns
odd?             [ 10000]:     1,33 ms      132 ns
int?             [ 10000]:     1,30 ms      129 ns
gensym           [  5000]:     1,05 ms      210 ns
butlast          [  5000]:   878,54 us      175 ns
dec              [  4900]:   868,25 us      177 ns
last             [  5000]:   741,72 us      148 ns
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
          (do 
            (if (= n 1)
              b 
              (recur (dec n) b (math/bigint-add a b)))))))

   (perf (fib 50) 100 100)
   (println (prof :data-formatted "Metrics: fib")))
```

Metrics:

```text
--------------------------------------------------
Metrics: fib
--------------------------------------------------
macroexpand      [ 99200]:   413,17 ms     4,16 us
user/_test       [     1]:   399,70 ms            
user/fib         [   100]:   399,50 ms     4,00 ms
math/bigint-add  [  4900]:   378,21 ms    77,19 us
math/bigint      [ 10000]:   371,74 ms    37,17 us
cons             [ 99200]:    25,59 ms      257 ns
list             [ 99200]:    22,13 ms      223 ns
math/bigint?     [  9800]:    21,91 ms     2,24 us
rest             [148800]:    21,40 ms      143 ns
instance?        [  9800]:    12,48 ms     1,27 us
first            [ 49600]:     6,95 ms      140 ns
second           [ 49600]:     6,68 ms      134 ns
.                [  5100]:     3,15 ms      617 ns
long?            [ 10000]:     1,40 ms      139 ns
double?          [  9800]:     1,39 ms      141 ns
string?          [ 10000]:     1,39 ms      138 ns
int?             [ 10000]:     1,28 ms      127 ns
=                [ 10000]:     1,24 ms      124 ns
dec              [  4900]:   776,42 us      158 ns
--------------------------------------------------
```
