# Java Interop

Venice supports calling Java constructors, static and instance methods as well as 
accessing static class and instance fields. 

The Venice types long, double, and decimal are coerced to Java's primitive and
object types byte, short, int, long, float, double, Byte, Short, Integer, Long, 
Float, Double, and BigDecimal.


## Calling Java

Java calls follow the patterns:

constructor: `(. :class :new arg1 arg2 ...)`

instance method: `(. object :method arg1 arg2 ...)`

static method: `(. :class :method arg1 arg2 ...)`

static field: `(. :class :field)`

enum value: `(. :class :name)`

get property: `(:property object)`



```clojure
(do
   ;; constructor no args
   (. :java.awt.Point :new)
   
   ;; constructor with two arguments
   (. :java.awt.Point :new 10 20)
   
   ;; instance method
   (let [r (. :java.awt.Rectangle :new 100 200)]
      (. r :translate 10 10)
      r)

   ;; instance no arg methods
   (let [r (. :java.awt.Rectangle :new 100 200)]
      (. r :isEmpty)   ; isEmpty()
      (. r :getWidth)  ; getWidth()
      (. r :getX))     ; getX()
 
   ;; accessing bean getter properties
   (let [r (. :java.awt.Rectangle :new 100 200)]
      (:empty r)  ; isEmpty()
      (:width r)  ; getWidth()
      (:x r))     ; getX()
 
   ;; static field
   (. :java.lang.Math :PI)

   ;; static method
   (. :java.lang.Math :min 20 30)

   ;; constructor and instance method
   (-> (. :java.time.LocalDate :now) 
       (. :plusDays 5))

   ;; using imports
   (import :java.time.LocalDate)
   (. :LocalDate :now) 

   ;; class from a Venice class reference
   (. :java.lang.Math :class)

   ;; class from an object
   (-> (. :LocalDate :now) 
       (. :class))
       
   ;; using doto for calling multiple methods on a mutable Java object
   (doto (. :java.util.HashMap :new)
         (. :put :a 1)
         (. :put :b 2))
)
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


## Enum types


a Java _enum_ values can be passed as simple or scoped keywords:

```clojure
(do
   (import :java.time.LocalDate)

   (. :LocalDate :of 1994 :JANUARY 21)   
   (. :LocalDate :of 1994 :java.time.Month.JANUARY 21))
```

Get a Java _enum_ value

```clojure
(. :java.time.Month :JANUARY)
```

Pass a Java _enum_ value as a function argument

```clojure
(do
   (import :java.time.LocalDate)

   (let [jan (. :java.time.Month :JANUARY)]
     (. :LocalDate :of 1994 jan 21)))
```

Get all values of a Java _enum_

```clojure
(. :java.time.Month :values)
```




## Dealing with static nested classes

*Venice*

```
(do
  (import :foo.OuterClass)
  (import :foo.OuterClass$NestedStaticClass)

  (-> (. :OuterClass :new)
      (. :message))
      
  (-> (. :OuterClass$NestedStaticClass :new)
      (. :message)))
```

*Java*

```
package foo;

public class OuterClass {
  public String message() {
    return "OuterClass::message()";
  }

  public static class NestedStaticClass {
    public String message() {
      return "NestedStaticClass::message()";
    }
  }
}
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


## Dynamic Proxies

The Venice `proxify` function creates implementations for *Java Interfaces*
at runtime time based on [Java Dynamic Proxies](https://www.baeldung.com/java-dynamic-proxies).
It wraps multiple Venice functions to implement a Java Interface:

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

Venice provides a few helper functions to simplify using Java Dynamic Proxies 
with Java functional interfaces:

- [java.lang.Runnable](https://docs.oracle.com/javase/8/docs/api/java/lang/Runnable.html) -> `(as-runnable f)`
- [java.util.concurrent.Callable](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Callable.html) -> `(as-callable f)`
- [java.util.function.Predicate](https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html) -> `(as-predicate f)`
- [java.util.function.Function](https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html) -> `(as-function f)`
- [java.util.function.Consumer](https://docs.oracle.com/javase/8/docs/api/java/util/function/Consumer.html) -> `(as-consumer f)`
- [java.util.function.Supplier](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html) -> `(as-supplier f)`
- [java.util.function.BiPredicate](https://docs.oracle.com/javase/8/docs/api/java/util/function/BiPredicate.html) -> `(as-bipredicate f)`
- [java.util.function.BiFunction](https://docs.oracle.com/javase/8/docs/api/java/util/function/BiFunction.html) -> `(as-bifunction f)`
- [java.util.function.BiConsumer](https://docs.oracle.com/javase/8/docs/api/java/util/function/BiConsumer.html) -> `(as-biconsumer f)`
- [java.util.function.BinaryOperator](https://docs.oracle.com/javase/8/docs/api/java/util/function/BinaryOperator.html) -> `(as-binaryoperator f)`

These functions are defined in the `:java` module.


**Example 1:**

Instead of 

```clojure
(proxify :java.lang.Runnable { :run #(sleep 10) })
```

use

```clojure
(j/as-runnable #(sleep 10))
```

```clojure
(do
  (load-module :java ['java :as 'j])
  (import :com.github.jlangch.venice.demo.FunctionalInterfaces)

  ;; public static void testRunnable(final Runnable r) {
  ;;   r.run();
  ;; }

  (def op (j/as-runnable (fn [] (println "running"))))
  (. :FunctionalInterfaces :testRunnable op))
```
 
**Example 2:**

Instead of 

```clojure
(proxify :java.util.function.Function { :apply #(+ % 1) })
```

use 

```clojure
(j/as-function #(+ % 1))
```

```clojure
(do
  (load-module :java ['java :as 'j])
  (import :com.github.jlangch.venice.demo.FunctionalInterfaces)

  ;; public static Long testFunction(Function<Long,Long> f, Long t) {
  ;;   return f.apply(t);
  ;; }

  (def op (j/as-function (fn [t] (+ t 1))))
  (. :FunctionalInterfaces :testFunction op 4))
```



## Mixing Venice functions with Java streams

```clojure
(do
  (load-module :java)

  (-> (. [1 2 3 4 5 6 7 8] :stream)
      (. :filter (java/as-predicate #(> % 2)))
      (. :map (java/as-function #(* % 10)))
      (. :reduce 0 (java/as-binaryoperator #(+ %1 %2)))))
```

_Note:_  this is not the fastest way to filter/map collections


## Filtering Java objects:

```clojure
(do
   (import :com.github.jlangch.venice.demo.Person)
   (import :java.time.LocalDate)

   ;; get the first item of a Java list
   (first
      (doto (. :java.util.ArrayList :new)
            (. :add 1)
            (. :add 2)))

   (def users [
        (. :Person :new "John" "Smith" (. :LocalDate :of 1994 7 21) :Male)
        (. :Person :new "Mary" "Johnson" (. :LocalDate :of 1970 1 12) :Female) ])

   (str (filter #(> (:age %) 30) users)))
```



## Java 9+:

Some public Java APIs return objects of classes that are defined in private modules. This 
causes problems when accessing methods or fields of the objects via reflection. Java 9 
changed the rules in that access to classes defined in private modules
result in severe warnings. 

Code that runs fine with Java 8 but gets problems with Java 9+ (Venice version prior to 1.7.17):

```clojure
(do
   (let [img (. :java.awt.image.BufferedImage :new 40 40 1) 
         g2d (. img :createGraphics)]
     (. g2d :fillOval 10 20 5 5)   ;; <<-- warning "illegal reflective access"
     img))
```

With plain reflection one gets these warnings with Java 9+:

```text
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor (file:/.../classes/java/main/) to method sun.java2d.SunGraphics2D.fillOval(int,int,int,int)
WARNING: Please consider reporting this to the maintainers of com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
```

`java.awt.BufferedImage::createGraphics()` returns effectively an object of type 
`sun.java2d.SunGraphics2D` while the API defines the return type as `java.awt.Graphics2D` 
(the formal type). When invoking the method `fillOval` on the graphics context 
returned from `createGraphics` one gets warnings because of reflectively accessing the 
JDK private class `SunGraphics2D`. And even worse Oracle will deny this access in 
a future Java release.

Venice 1.7.17 honors now the  _formal type_  of the values returned by methods or 
fields.

With Venice version 1.7.17+ the above code runs now fine without warnings on 
Java 8 and Java 9+.

**Venice Java Interop is handling these cases completely transparent for you. You**
**don't need to add explicit type hints or add casts. Venice knows about the formal**
**type of values returned from methods and invokes methods or fields on the formal**
**type instead of the real type.**


It's possible to do a cast explicitly, but it is not necessary:

```clojure
(do
   (import :java.awt.image.BufferedImage)
   (import :java.awt.Graphics)

   ;; cast the graphics context to 'java.awt.Graphics' instead of the 
   ;; implicit cast to 'java.awt.Graphics2D' as Venice is doing
   (let [img (. :BufferedImage :new 40 40 1)
         gd (cast :Graphics (. img :createGraphics))]
     (. gd :fillOval 10 20 5 5)
     img))
```
