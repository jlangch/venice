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
user/_test  [       1]:    14,37 s             
user/sum    [     100]:    14,37 s    143,73 ms
+           [10000000]:   770,81 ms       77 ns
inc         [10000000]:   682,99 ms       68 ns
<           [10000100]:   657,00 ms       65 ns
-----------------------------------------------
```

Analysis loop-recur performance:

* `(sum 100000)` takes 143.7ms
* the functions `inc` and `<` take 100'000 * 210ns = 21.0ms
* the loop-recur overhead is (143.7ms - 21.0ms) / 100'000 = 1.2µs
* every loop-recur iteration takes 1.2µs to process the `if` logic, initiate a new 
  iteration, and setup the local environment with the loop variables.



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
macroexpand      [ 55200]:   420,21 ms     7,61 us
user/_test       [     1]:   387,97 ms            
user/fib         [   100]:   387,77 ms     3,88 ms
math/bigint-add  [  4900]:   129,71 ms    26,47 us
math/bigint      [ 10000]:   123,25 ms    12,33 us
mapcat           [  5000]:    39,74 ms     7,95 us
cons             [215500]:    32,25 ms      149 ns
math/bigint?     [ 10000]:    23,20 ms     2,32 us
instance?        [ 10000]:    14,02 ms     1,40 us
concat           [ 30100]:    11,33 ms      376 ns
rest             [ 75300]:     7,99 ms      106 ns
list             [ 25100]:     5,37 ms      214 ns
first            [ 25100]:     3,12 ms      124 ns
not-empty?       [ 25100]:     2,85 ms      113 ns
second           [ 25100]:     2,75 ms      109 ns
partition        [  5000]:     2,43 ms      485 ns
.                [  5100]:     2,08 ms      408 ns
count            [ 10000]:     1,08 ms      107 ns
odd?             [ 10000]:   955,93 us       95 ns
gensym           [  5000]:   948,41 us      189 ns
==               [ 10000]:   914,16 us       91 ns
butlast          [  5000]:   858,13 us      171 ns
dec              [  4900]:   626,30 us      127 ns
last             [  5000]:   601,89 us      120 ns
long?            [   200]:    25,38 us      126 ns
--------------------------------------------------
```

Metrics with upfront macro expansion:

```text
-------------------------------------------------
Metrics: fibonacci
-------------------------------------------------
user/_test       [    1]:    46,33 ms            
user/fib         [  100]:    46,24 ms   462,37 us
math/bigint-add  [ 4900]:    31,55 ms     6,44 us
math/bigint      [10000]:    24,47 ms     2,45 us
math/bigint?     [10000]:    17,08 ms     1,71 us
instance?        [10000]:     9,33 ms      933 ns
.                [ 5100]:     1,43 ms      281 ns
==               [10000]:   777,80 us       77 ns
dec              [ 4900]:   522,03 us      106 ns
long?            [  200]:    20,72 us      103 ns
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
user/_test       [    1]:   157,33 ms            
user/fib         [  100]:   157,17 ms     1,57 ms
macroexpand      [20400]:   142,73 ms     7,00 us
math/bigint-add  [ 4900]:   136,19 ms    27,79 us
math/bigint      [10000]:   131,69 ms    13,17 us
math/bigint?     [10000]:    23,15 ms     2,31 us
instance?        [10000]:    13,75 ms     1,38 us
cons             [51000]:    11,06 ms      216 ns
concat           [10200]:     4,56 ms      446 ns
rest             [30600]:     3,63 ms      118 ns
list             [10200]:     2,14 ms      210 ns
.                [ 5100]:     2,08 ms      407 ns
first            [10200]:     1,34 ms      131 ns
second           [10200]:     1,15 ms      113 ns
not-empty?       [10200]:     1,05 ms      102 ns
=                [10000]:   837,96 us       83 ns
dec              [ 4900]:   474,63 us       96 ns
long?            [  200]:    23,18 us      115 ns
-------------------------------------------------
```


Metrics with upfront macro expansion:

```text
-------------------------------------------------
Metrics: fib
-------------------------------------------------
user/_test       [    1]:    44,72 ms            
user/fib         [  100]:    44,62 ms   446,22 us
math/bigint-add  [ 4900]:    31,93 ms     6,52 us
math/bigint      [10000]:    24,02 ms     2,40 us
math/bigint?     [10000]:    16,28 ms     1,63 us
instance?        [10000]:     9,44 ms      944 ns
.                [ 5100]:     1,78 ms      349 ns
=                [10000]:   765,12 us       76 ns
dec              [ 4900]:   522,94 us      106 ns
long?            [  200]:    20,49 us      102 ns
-------------------------------------------------
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

