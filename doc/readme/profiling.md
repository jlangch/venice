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
  (load-module :benchmark ['benchmark :as 'b])
  (b/benchmark (+ 1 2 3 4) 120000 10000))
```

The benchmark output: 

```text
Warmup...
GC...
Sampling...
Analyzing...
                      Samples :   10000
          Execution time mean :  460.000ns
 Execution time std-deviation :   67.452ns
Execution time lower quartile :  455.000ns (25%)
Execution time upper quartile :  574.000ns (75%)
Execution time lower quantile :  420.000ns (2.5%)
Execution time upper quantile :  616.000ns (97.5%)
                     Outliers :      18
```

### Outliers

A sample is marked as an outlier if its execution time is lower than `Q1 - 3 * IQR` or greater than `Q3 + 3 * IQR`. Where Q1 is the first or lower quartile, Q3 is the third or higher quartile, and IQR (Interquartile Range) is defined as `Q3 - Q1`. 


### Create a distribution chart 

#### Short warm-up phase

```clojure
(do
  (load-module :benchmark ['benchmark :as 'b])
  (b/benchmark (+ 1 2 3 4) 1000 300 :chart true))
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
(do
  (load-module :benchmark ['benchmark :as 'b])
  (b/benchmark (+ 1 2 3 4) 120000 10000 :chart true))
```

```text
Warmup...
GC...
Sampling...
Analyzing...
                      Samples :   10000
          Execution time mean :  462.000ns
 Execution time std-deviation :   85.202ns
Execution time lower quartile :  456.000ns (25%)
Execution time upper quartile :  576.000ns (75%)
Execution time lower quantile :  421.000ns (2.5%)
Execution time upper quantile :  845.000ns (97.5%)
                     Outliers :      64
Generating chart...
Quantization step width: 141ns
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

