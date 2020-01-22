# Java Interop

Venice supports calling Java constructors, static and instance methods as well as 
accessing static class and instance fields. 

Venice is using reflection to access Java methods and fields. Java Reflection is 
impressively fast, especially when caching the the reflection meta data on classes, 
methods, and fields as Venice is doing. See the benchmark further down.

The Venice types long, double, and decimal are coerced to Java's primitive and
object types byte, short, int, long, float, double, Byte, Short, Integer, Long, 
Float, Double, and BigDecimal.


## Calling Java

```clojure
(do
   (import :java.math.BigInteger)
   
   ;; static field
   (. :java.lang.Math :PI)

   ;; static method
   (. :java.lang.Math :min 20 30)

   ;; constructor and instance method
   (-> (. :java.time.ZonedDateTime :now) 
       (. :plusDays 5))

   ;; class object
   (. :java.lang.Math :class)
   (-> (. :java.time.ZonedDateTime :now) 
       (. :class))
       
   ;; constructor and instance method
   (defn bigint [x] (. :BigInteger :new x))
   (-> (bigint "100000000")
        (. :multiply (bigint "600000000"))
        (. :add (bigint "300000000"))))
```


Java enum values can be passed as simple or scoped keywords:

```clojure
(do
   (import :java.time.LocalDate)

   (. :LocalDate :of 1994 :JANUARY 21)   
   (. :LocalDate :of 1994 :java.time.Month.JANUARY 21))
```


Java types support hash codes, equals, and compare. Thereby sequences with Java types
can be sorted and Java types can be used with sets and maps.

```clojure
(do
   (import :java.math.BigInteger)
   
   (let [b1 (. :BigInteger :new "10000")
         b2 (. :BigInteger :new "10000")
         b3 (. :BigInteger :new "20000")] 
      (== b1 b2)
      (compare b1 b3)
      (sort [b1 b3 b2])
      (hash-map b1 1 b2 2 b3 3)))
```


## Java VarArgs

Java varargs are passed as list or vector:

```clojure
; Java signature: String String::format(String format, Object... args)
(. :java.lang.String :format "%s: %d" ["abc" 100])  ;; => "abc: 100"

; Java signature: Path Paths::get(String first, String... more)
(. :java.nio.file.Paths :get "a.txt" '())  ;; => a.txt
(. :java.nio.file.Paths :get "." "a.txt")  ;; => ./a.txt
(. :java.nio.file.Paths :get "/temp" "a.txt")  ;; => /temp/a.txt
(. :java.nio.file.Paths :get "/temp" '("xxx" "a.txt"))  ;; => /temp/xxx/a.txt
```


## Java Callbacks (dynamic proxies)

The Venice `proxify` function wraps one or multiple Venice function with 
a Java Dynamic Proxy:

```clojure
;; File filter
(do
   (import :java.io.FilenameFilter)

   (defn file-filter [dir name] (str/ends-with? name ".txt"))

   ;; create a dynamic proxy for the interface FilenameFilter
   ;; and implement its function 'accept' by 'file-filter'
   ;;
   ;; public interface FilenameFilter {
   ;;   boolean accept(File dir, String name);
   ;; }
   
   (. (io/file "/tmp") 
      :list 
      (proxify :FilenameFilter {:accept file-filter})))
```


```clojure
;; Swing GUI
(do
   (import :java.lang.Runnable)
   (import :javax.swing.JPanel)
   (import :javax.swing.JFrame)
   (import :javax.swing.JLabel)
   (import :javax.swing.SwingUtilities)

   (defn swing-open-window [title]
      (let [frame (. :JFrame :new title)
            label (. :JLabel :new "Hello World")
            closeOP (. :JFrame :EXIT_ON_CLOSE)]
         (. frame :setDefaultCloseOperation closeOP)
         (. frame :add label)
         (. frame :setSize 200 200)
         (. frame :setVisible true)))

   (defn swing-gui [title]
      (. :SwingUtilities :invokeLater
         (proxify :Runnable { :run #(swing-open-window title) } )))

   (swing-gui "Test"))
```


## Mixing Venice functions with Java streams

```clojure
(do
    (import :java.util.function.Predicate)
    (import :java.util.stream.Collectors)

    (-> (. [1 2 3 4] :stream)
        (. :filter (proxify :Predicate { :test #(> % 2) }))
        (. :collect (. :Collectors :toList))))
```

_Note:_ this is not the fastest way to filter collections


## Filtering Java objects:

```clojure
(do
   (import :org.test.User :java.time.LocalDate)

   ;; get the first item of a Java list
   (first
      (doto (. :java.util.ArrayList :new)
            (. :add 1)
            (. :add 2))))

   (def users [
        (. :User :new "john" 24 (. :LocalDate :of 1994 7 21)))
        (. :User :new "pete" 48 (. :LocalDate :of 1970 1 12))) ])

   (str (filter #(> (:age %) 30) users)))
```


## Performance of reflective Java calls


**Native Java calls:**

```java
    public void test_native() {
        final BigInteger[] total = new BigInteger[] {BigInteger.valueOf(0L)};
        
         new Benchmark("Native Java", 100_000, 10_000, 1).benchmark(ii -> {
            final long start = System.nanoTime();
            final BigInteger i1 = BigInteger.valueOf(ii);
            final BigInteger i2 = BigInteger.valueOf(100L);
            final BigInteger sum = i1.add(i2);               
            final long elapsed = System.nanoTime() - start;
            
            total[0] = total[0].add(sum); // prevent JIT from optimizing too much
               
            return elapsed;
        });
    }
```

**Reflective Java calls:**

```java
    public void test_reflective() throws Exception {
        final BigInteger[] total = new BigInteger[] {BigInteger.valueOf(0L)};
        
        // cache methods
        final Method mValueOf = BigInteger.class.getDeclaredMethod("valueOf", long.class);
        final Method mAdd = BigInteger.class.getDeclaredMethod("add", BigInteger.class);
        
         new Benchmark("Reflectiv Java", 1_000_000, 10_000, 1).benchmark(ii -> {
             try {
                final long start = System.nanoTime();
                final BigInteger i1 = (BigInteger)mValueOf.invoke(BigInteger.class, new Object[] {ii});
                final BigInteger i2 = (BigInteger)mValueOf.invoke(BigInteger.class, new Object[] {100L});
                final BigInteger sum = (BigInteger)mAdd.invoke(i1, new Object[] {i2});                  
                final long elapsed = System.nanoTime() - start;
                
                total[0] = total[0].add(sum); // prevent JIT from optimizing too much
                   
                return elapsed;
            }
             catch(Exception ex) {
                 throw new RuntimeException(ex);
             }
        });
    }

```

**Results:**

The benchmarks did run on a 2017 MacBook Pro (Core i7 2.8 GHz).


| Java 8 Server VM  |   Calls | Elapsed | Per Call |
| :---              |    ---: |    ---: |     ---: |
| Native Java       | 100'000 |  903 µs |    95 ns |
| Reflective Java   | 100'000 |  735 µs |    77 ns |

| Java 11 Server VM |   Calls | Elapsed | Per Call |
| :---              |    ---: |    ---: |     ---: |
| Native Java       | 100'000 |  867 µs |    91 ns |
| Reflective Java   | 100'000 |  577 µs |    60 ns |

_Warmup  1'000'000 calls, benchmarking 10'000 calls_

Reflective calls are impressively fast compared to native calls. 

