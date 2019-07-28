# Benchmarks & Profiling


## Benchmark

Venice's *benchmark* module measures the computation time of an expression. 

Benchmarking an expression incorporates four phases:
   1. Run the expression in a warm-up phase to allow the JIT compiler to do optimizations. 
   2. Run the garbage collector to isolate timings from GC state prior to testing 
   3. Runs the expression
   4. Statistically analyze the expression evaluations


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

A sample is marked as an outlier if its execution time is lower than `Q1 - 3 * IQR` or greater than `Q3 + 3 * IQR`. Where Q1 is the first or lower quartile, Q3 is the third or higher quartile, and IQR (Interquartile Range) is defined as Q3 - Q1. 


### Create a distribution chart 

#### short warm-up phase

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


#### long warm-up phase


```clojure
   (bench/benchmark (+ 1 2 3 4) 100000 30000 :chart true)
```
```text
Warmup...
GC...
Sampling...
Analyzing...
                      Samples : 30000
          Execution time mean : 1,477 µs
 Execution time std-deviation : 390,524 ns
Execution time lower quartile : 806,000 ns (25%)
Execution time upper quartile : 1,514 µs (75%)
Execution time lower quantile : 795,000 ns (2.5%)
Execution time upper quantile : 1,839 µs (97.5%)
                     Outliers : 74
Generating chart...
Quantization step width: 33,200 µs 
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
_warmup     [       1]:     9,82 s             
_test       [       1]:     9,40 s             
sum         [     100]:     9,40 s     93,97 ms
<           [10000100]:  2850,62 ms      285 ns
inc         [10000000]:  1588,21 ms      158 ns
_warmup-gc  [       1]:    46,86 ms            
-----------------------------------------------
```
