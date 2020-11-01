# Benchmarks & Profiling

* [Benchmark](#benchmark)
* [Profiling](#profiling)
* [Benchmarks with JMH](#benchmarks-with-jmh)


All benchmarks and profiling did run on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz) 
with a Java 11 server VM.


## Benchmark

Venice provides out of-the-box benchmarks using the *benchmark* module to measures the 
execution time of an expression. 

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

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/benchmark/benchmark1.png" width="300">


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

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/benchmark/benchmark2.png" width="300">


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
user/_test  [       1]:    12,97 s             
user/sum    [     100]:    12,97 s    129,70 ms
+           [10000000]:   638,12 ms       63 ns
inc         [10000000]:   612,97 ms       61 ns
<           [10000100]:   535,07 ms       53 ns
-----------------------------------------------
```

Analysis loop-recur performance:

* `(sum 100000)` takes 143.7ms
* the functions `inc` and `<` take 100'000 * 173ns = 17.3ms
* the loop-recur overhead is (134.8ms - 17.3ms) / 100'000 = 1.2µs
* every loop-recur iteration takes 1.2µs to process the `if` logic, initiate a new 
  iteration, and setup the local environment with the loop variables.



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
macroexpand  [ 34800]:   228.45 ms     6.56 us
user/_test   [     1]:   197.93 ms            
user/fib     [   100]:   197.78 ms     1.98 ms
cond[m]      [ 14900]:    96.25 ms     6.46 us
case[m]      [  5000]:    83.27 ms    16.65 us
when[m]      [ 14900]:    39.77 ms     2.67 us
mapcat       [  5000]:    28.65 ms     5.73 us
cons         [164500]:    12.11 ms       73 ns
concat       [ 19900]:     4.55 ms      228 ns
rest         [ 44700]:     3.45 ms       77 ns
partition    [  5000]:     1.90 ms      379 ns
first        [ 14900]:     1.52 ms      102 ns
list         [ 14900]:     1.37 ms       91 ns
not-empty?   [ 14900]:     1.31 ms       88 ns
second       [ 14900]:   950.98 us       63 ns
==           [ 10000]:   862.88 us       86 ns
odd?         [ 10000]:   852.88 us       85 ns
count        [ 10000]:   807.81 us       80 ns
gensym       [  5000]:   708.94 us      141 ns
+            [  4900]:   578.20 us      118 ns
last         [  5000]:   564.23 us      112 ns
butlast      [  5000]:   517.84 us      103 ns
dec          [  4900]:   368.96 us       75 ns
----------------------------------------------
```

Metrics with upfront macro expansion:

```text
--------------------------------------------
Metrics: fibonacci
--------------------------------------------
user/_test  [    1]:    14.51 ms            
user/fib    [  100]:    14.41 ms   144.11 us
==          [10000]:   977.34 us       97 ns
+           [ 4900]:   727.36 us      148 ns
dec         [ 4900]:   473.84 us       96 ns
--------------------------------------------

```


### Example: profiling fibonacci (nested if)

The profiler runs the sum function 5000 times as warm-up followed by 100 times to profile it. 

```clojure
(do
   (load-module :math)
     
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
user/_test  [   1]:    10.96 ms            
user/fib    [ 100]:    10.87 ms   108.69 us
+           [4900]:   560.98 us      114 ns
dec         [4900]:   476.27 us       97 ns
zero?       [5000]:   425.86 us       85 ns
=           [5000]:   406.24 us       81 ns
-------------------------------------------
```



## Benchmarks with JMH

The most accurate benchmarks can be done using [JMH](http://openjdk.java.net/projects/code-tools/jmh/) 
(the Java Microbenchmark Harness). JMH has been added to the JDK starting with JDK 12; 
for earlier versions, the dependencies have to be added explicitly.


**Results Java 8 Server VM:**

| Benchmark                     | Mode |  Cnt |    Score |      Error | Units |
| :---                          | ---: | ---: |     ---: |       ---: |  ---: |
| no_precompilation             | avgt |    3 | 3691.460 | ± 1149.737 | us/op |
| precompilation_no_macroexpand | avgt |    3 |   45.714 | ±    1.468 | us/op |
| precompilation_macroexpand    | avgt |    3 |    7.022 | ±    0.415 | us/op |

**Results Java 11 Server VM (-XX:+UseParallelGC):**

| Benchmark                     | Mode |  Cnt |    Score |      Error | Units |
| :---                          | ---: | ---: |     ---: |       ---: |  ---: |
| no_precompilation             | avgt |    3 | 3949,762 |  ± 570,639 | us/op |
| precompilation_no_macroexpand | avgt |    3 |   45,350 |  ±  13,561 | us/op |
| precompilation_macroexpand    | avgt |    3 |    7,273 |  ±   1,652 | us/op |


### Code

```java
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.github.jlangch.venice.*;
import org.openjdk.jmh.annotations.*;


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Threads(1)
public class PrecompileBenchmark {
    @Benchmark
    public Object no_precompilation(State_ state) {
        return state.venice.eval("test", state.expr, state.parameters);
    }

    @Benchmark
    public Object precompilation_no_macroexpand(State_ state) {
        return state.venice.eval(state.precompiledNoMacroExpand, state.parameters);
    }
    
    @Benchmark
    public Object precompilation_macroexpand(State_ state) {
        return state.venice.eval(state.precompiledMacroExpand, state.parameters);
    }
  
    @State(Scope.Benchmark)
    public static class State_ {
        public String expr = "(+ (cond (< x 0) -1 (> x 0) 1 :else 0) " +
                             "   (cond (< y 0) -1 (> y 0) 1 :else 0) " +
                             "   (cond (< z 0) -1 (> z 0) 1 :else 0))";

        public Venice venice = new Venice();
        public PreCompiled precompiledNoMacroExpand = venice.precompile("example", expr, false);
        public PreCompiled precompiledMacroExpand = venice.precompile("example", expr, true);
        public Map<String,Object> parameters = Parameters.of("x", -10, "y", 0, "z", 10);
    }
}
```

