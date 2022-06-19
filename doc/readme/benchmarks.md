# Benchmarks

* [Benchmark](#benchmark)
* [Benchmarks with JMH](#benchmarks-with-jmh)


All benchmarks did run on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz) 
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
  
  ;; add the numbers 0..99
  (b/benchmark (apply + (range 100)) 1_000_000 10_000))
```

The benchmark output: 

```text
Warmup...
GC...
Sampling...
Analyzing...
                      Samples :      10000
          Execution time mean :    5.114µs
 Execution time std-deviation :  224.415ns
Execution time lower quartile :    4.892µs (25%)
Execution time upper quartile :    5.242µs (75%)
Execution time lower quantile :    4.629µs (2.5%)
Execution time upper quantile :    5.441µs (97.5%)
                 Outliers low :    3.842µs
                Outliers high :    6.292µs
                     Outliers :         82
```

### Outliers

A sample is marked as an outlier if its execution time is lower than `Q1 - 3 * IQR` or greater than `Q3 + 3 * IQR`. Where Q1 is the first or lower quartile, Q3 is the third or higher quartile, and IQR (Interquartile Range) is defined as `Q3 - Q1`. 


### Create a distribution chart 

#### Short warm-up phase

```clojure
(do
  (load-module :benchmark ['benchmark :as 'b])
  (b/benchmark (apply + (range 100)) 1000 300 :chart true))
```

```text
Warmup...
GC...
Sampling...
Analyzing...
                      Samples :        300
          Execution time mean :   21.032µs
 Execution time std-deviation :    7.798µs
Execution time lower quartile :   20.371µs (25%)
Execution time upper quartile :   30.718µs (75%)
Execution time lower quantile :   17.051µs (2.5%)
Execution time upper quantile :   58.323µs (97.5%)
                 Outliers low :        0ns
                Outliers high :   61.757µs
                     Outliers :          6
Generating chart...
Quantization step width: 1.101µs (100 steps)
Saved chart to 'benchmark.png'.
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/benchmark/benchmark1.png" width="300">


#### Long warm-up phase


```clojure
(do
  (load-module :benchmark ['benchmark :as 'b])
  (b/benchmark (apply + (range 100)) 1_000_000 10_000 :chart true))
```

```text
Warmup...
GC...
Sampling...
Analyzing...
                      Samples :      10000
          Execution time mean :    4.933µs
 Execution time std-deviation :  249.539ns
Execution time lower quartile :    4.541µs (25%)
Execution time upper quartile :    4.991µs (75%)
Execution time lower quantile :    4.408µs (2.5%)
Execution time upper quantile :    5.176µs (97.5%)
                 Outliers low :    3.191µs
                Outliers high :    6.341µs
                     Outliers :         78
Generating chart...
Quantization step width: 290ns (100 steps)
Saved chart to 'benchmark.png'.
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/benchmark/benchmark2.png" width="300">


### References

- [Median](https://en.wikipedia.org/wiki/Median)
- [Quartile](https://en.wikipedia.org/wiki/Quartile)
- [Quantile](https://en.wikipedia.org/wiki/Quantile)
- [Interquartile Range](https://en.wikipedia.org/wiki/Interquartile_range)
- [Box Plot](https://en.wikipedia.org/wiki/Box_plot)



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

