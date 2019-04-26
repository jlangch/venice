# Java Interop

Venice supports calling Java constructors, static and instance methods as well as 
accessing static class and instance fields.

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

```clojure
(. :java.lang.String :format "%s: %d" ["abc" 100])
```


## Java Callbacks

```clojure
;; File filter
(do
   (import :java.io.FilenameFilter)

   (defn file-filter [dir name] (str/ends-with? name ".txt"))

   ;; create a dynamic proxy for the interface FilenameFilter
   ;; and implement its function 'accept' by 'file-filter'
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


## Mixing Venice functions with Java streams:

```clojure
(do
    (import :java.util.function.Predicate)
    (import :java.util.stream.Collectors)

    (-> (. [1 2 3 4] :stream)
        (. :filter (proxify :Predicate { :test #(> % 2) }))
        (. :collect (. :Collectors :toList))))
```


Another example:

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
