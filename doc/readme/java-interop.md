# Java Interop

Venice supports calling Java constructors, static and instance methods as well as 
accessing static class and instance fields. 

The Venice types long, double, and decimal are coerced to Java's primitive and
object types byte, short, int, long, float, double, Byte, Short, Integer, Long, 
Float, Double, and BigDecimal.


## Calling Java

Java calls follow the pattern:

constructor: `(. :class :new arg1 arg2 ...)`

instance method: `(. object :method arg1 arg2 ...)`

static method: `(. :class :method arg1 arg2 ...)`

static field: `(. :class :field)`



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

_Note:_  this is not the fastest way to filter collections


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
