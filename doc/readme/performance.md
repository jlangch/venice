# Performance comparison


Quantifying the absolute performance of a language is an impossible task. This 
section instead attempts to give an idea of what performance differences to 
expect when choosing between the three languages. This is done by comparing
execution times of several small example programs implemented in the three 
languages *Java*, *Clojure*, and *Venice*.

While *Clojure* forms are always compiled before being executed even when run 
from the REPL, *Venice* is always interpreted, so interpreted *Venice* is
compared against compiled *Clojure* and *Java* code.


## Hardware and Software used

**Hardware**: 2017 MacBook Pro, Mac OSX 11.0.1, Core i7 2.8 GHz

**Java**: OpenJDK 64-Bit Server VM 1.8.0_275-b01

**Venice**: 1.9.5

**Clojure**: 1.10.1



## Measuring performance

On Java [JMH](https://github.com/openjdk/jmh) (Java Microbenchmark Harness) is used
to measure the performance of the sample programs. [Criterium 0.4.6](https://github.com/hugoduncan/criterium) 
drives the Clojure benchmark, and on Venice the *benchmark* module is used.

**Java VM options:** 

```text
-server  -Xmx6G
```

**Leiningen config**

~/.lein/profiles.clj:

```text
{:user {:dependencies [[criterium "0.4.1"]]
        :jvm-opts ["-Xmx6G" "-server"] }}
```


## Sample programs

### Map creation

**Java**

```java
@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.MICROSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class CreateMap_Benchmark {
    @Benchmark
    public Object create_mutable_map() {
        final ConcurrentHashMap<Long,Long> map = new ConcurrentHashMap<>();
        for(long ii=0; ii<2000; ii++) {
            map.put(Long.valueOf(ii), Long.valueOf(ii*2));
        }
        return map;
    }

    @Benchmark
    public Object create_persistent_map() {
        io.vavr.collection.HashMap<Long,Long> map = io.vavr.collection.HashMap.empty();
        for(long ii=0; ii<2000; ii++) {
            map = map.put(Long.valueOf(ii), Long.valueOf(ii*2));
        }
        return map;
    }
}
```

**Clojure**

```clojure
(require '[criterium.core :as criterium])

(defn create−persistent-map [size] 
  (loop [m {}, i size]
     (if (zero? i)
         m
         (recur (assoc m i (* 2 i)) (dec i)))))
         
(criterium/quick-bench (create−persistent-map 2000))
```

**Venice**

```clojure
(do
  (load-module :benchmark)
  
  (defn create−persistent-map [size] 
    (loop [m (hash-map), i size]
       (if (zero? i)
           m
           (recur (assoc m i (* 2 i)) (dec i)))))
         
  (bench/benchmark (create−persistent-map 2000) 1000 500))
```

### Results

**Java**

```text
Java Benchmark                Mode  Cnt  Score    Error    Units
-----------------------------------------------------------------
create_mutable_map            avgt    3  126.334  ± 16.018  µs/op
create_persistent_map         avgt    3  129.435  ± 24.465  µs/op
```

**Clojure**

```text
WARNING: Final GC required 18.03001544260917 % of runtime
Evaluation count : 750 in 6 samples of 125 calls.
             Execution time mean : 808.485571 µs
    Execution time std-deviation : 6.572947 µs
   Execution time lower quantile : 802.515968 µs ( 2.5%)
   Execution time upper quantile : 816.447943 µs (97.5%)
                   Overhead used : 8.172397 ns
```

**Venice**

```text
Warmup...
GC...
Sampling...
Analyzing...
                      Samples :     500
          Execution time mean :   1.747 ms
 Execution time std-deviation : 132.233 µs
Execution time lower quartile :   1.677 ms (25%)
Execution time upper quartile :   1.826 ms (75%)
Execution time lower quantile :   1.649 ms (2.5%)
Execution time upper quantile :   2.163 ms (97.5%)
                     Outliers :       7
```


### Filter-Map-Reduce

**Java**

```java
@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.MICROSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class JavaFilterMapReduceBenchmark {

    public JavaFilterMapReduceBenchmark() {
        list = new ArrayList<>(2000);
        for(long ii=0; ii<2000; ii++) {
            list.add(Long.valueOf(ii));
        }
    }

    @Benchmark
    public Object filter_map_reduce() {
        return list.stream()
                   .filter(v -> v % 2L == 0L)
                   .map(v -> v * 10L)
                   .reduce(0L, (sum, v) -> sum + v);
    }

    private final List<Long> list;
}
```

**Clojure**

```clojure
(require '[criterium.core :as criterium])

(def data (doall (range 2000)))

(def xform (comp
             (filter #(even? %))
             (map #(* 10 %))))
         
(criterium/quick-bench (transduce xform + 0 data))
```

**Venice**

```clojure
(do
  (load-module :benchmark)
  
  (def data (range 2000))

  (def xform (comp
               (filter #(even? %))
               (map #(* 10 %))))
                        
  (bench/benchmark (transduce xform + 0 data) 1000 500))
```

### Results

**Java**

```text
Java Benchmark     Mode  Cnt   Score   Error  Units
---------------------------------------------------
filter_map_reduce  avgt    3  13.831 ± 0.662  us/op
```

**Clojure**

```text
WARNING: Final GC required 11.646503780833351 % of runtime
Evaluation count : 5490 in 6 samples of 915 calls.
             Execution time mean : 108.718647 µs
    Execution time std-deviation : 1.446998 µs
   Execution time lower quantile : 106.694702 µs ( 2.5%)
   Execution time upper quantile : 110.302163 µs (97.5%)
                   Overhead used : 8.543009 ns
```

**Venice**

```text
Warmup...
GC...
Sampling...
Analyzing...
                      Samples :     500
          Execution time mean : 701.954 µs
 Execution time std-deviation :  41.779 µs
Execution time lower quartile : 698.038 µs (25%)
Execution time upper quartile : 742.992 µs (75%)
Execution time lower quantile : 662.792 µs (2.5%)
Execution time upper quantile :   1.007 ms (97.5%)
                     Outliers :      35
```

## Benchmark Summary

| Benchmark               |  Java |  Clojure |  Venice |
| :---                    |  ---: |     ---: |    ---: |
| map creation            | 126µs |    808µs |  1747µs |
| filter-map-reduce       |  13µs |    108µs |   715µs |



