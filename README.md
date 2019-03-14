[![](https://github.com/jlangch/venice/blob/master/doc/license.svg)](./LICENSE)
[![](https://github.com/jlangch/venice/blob/master/doc/maven-central.svg)](http://mvnrepository.com/artifact/com.github.jlangch/venice)


# Venice

Venice, a Clojure inspired sandboxed Lisp dialect with Java interoperability serving as 
a safe scripting language.


## Overview

Venice's goal is not to build just another Lisp, it's born from the need of 
having a safe, powerful scripting and expression language that can be used 
to implement scriptable extension points and rules for applications.

Venice supports macros, tail-recursion, dynamic code loading, dynamic 
(thread-local) binding. It comes with excellent Java interoperability, and a 
configurable sandbox that can prevent all sorts of dangerous JVM interactions 
like reading/writing files, invoking _System.exit(0)_ or any other malicious 
action.

Venice's immutable persistent data structures together with Clojure style atoms, 
futures, promises, and agents greatly simplify writing concurrent code.

Because Venice does not depend on any runtime libraries (other than the JVM) you 
can easily add it as standalone .jar to your classpath.

Venice requires Java 8 or newer.

 
## Cheat Sheet

[Cheat Sheet HTML](https://cdn.rawgit.com/jlangch/venice/51d9af7/cheatsheet.html)

[Cheat Sheet PDF](https://cdn.rawgit.com/jlangch/venice/51d9af7/cheatsheet.pdf)


## REPL

Start the REPL with `java -jar venice-1.3.6.jar -colors`

```text
venice> (+ 1 1)
=> 2
venice>
```

Type `!` from the REPL to get the help. Browse through the history expressions 
with the up/down arrows.

A history of the last three result values is kept by the REPL, accessible through 
the symbols `*1`, `*2`, `*3`, `**`.

The REPL supports multi-line editing and copy/paste of multi-line code
snippets.

```text
venice> (defn sum [x y]
      |    (+ x y))
venice> (sum 1 4)
=> 5
venice>
```

If the REPL colors don't harmonize well with your terminal color schema 
omit the '-colors' option or place a 'repl.json' config file with customized 
ANSI escape code colors on the working dir. The REPL command `!config` shows
as sample 'repl.json' that can be modified.



## Executing scripts

```text
foo> java -jar venice-1.3.6.jar -script "(+ 1 1)"
=> 2
```

```text
foo> echo "(+ 1 1)" > script.venice
foo> java -jar venice-1.3.6.jar -file script.venice
=> 2
```

## Embedding Venice in Java

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;

try {
   final Venice venice = new Venice();
   System.out.println(venice.eval("(+ 1 1)"));
} 
catch(VncException ex) {
   ex.printVeniceStackTrace();
}
catch(RuntimeException ex) {
   ex.printStackTrace();
}
```


### Passing parameters to Venice

Venice expects Java data types (Long, Double, String, List, Map, ...) as 
parameters and returns Java data types as the expression result. It coerces 
Java to/from Venice data types implicitly.

Java bean parameters expose its getters as Map keys in Venice, so the 
getters can be accessed simply through `(:getterName bean)`

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.Parameters;
import java.awt.Color;

final Venice venice = new Venice();

System.out.println(
    venice.eval(
        "(+ x y 3)", 
        Parameters.of("x", 6, "y", 3L)));
        
// (:blue (. :Color :PINK))
System.out.println(
    venice.eval(
        "(:blue color)", 
        Parameters.of("color", Color.PINK)));
```


### Venice stdout redirection

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.util.CapturingPrintStream;

final Venice venice = new Venice();

// redirect stdout to the null device
venice.eval(
    "(println [1 2])", 
    Parameters.of("*out*", null));

// capture stdout within the script and return it as the result
System.out.println(
    venice.eval(
        "(do (with-out-str (println 1) (println [4 5])))"));

// capturing stdout preserving the script result
final CapturingPrintStream ps = CapturingPrintStream.create();
System.out.println(
    venice.eval(
        "(do (println 1) (println [4 5]) 100)", 
        Parameters.of("*out*", ps)));
System.out.println(ps.getOutput());
```


### Precompiling Venice

Precompiling Venice speeds up evaluation significantly when calling an expression multiple times with different parameters:

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.PreCompiled;
import com.github.jlangch.venice.Parameters;

final Venice venice = new Venice();

final PreCompiled precompiled = venice.precompile("example", "(+ 1 x)");

IntStream.range(0, 100).forEach(
  ii -> System.out.println(
          venice.eval(
             precompiled, 
             Parameters.of("x", ii))));
```


### Stack traces

Venice generates user friendly stack traces

```clojure
(do
   (defn fn1 [x] (fn2 x))
   (defn fn2 [x] (fn3 x))
   (defn fn3 [x] (/ 1 x))
   (fn1 0))
   
=>
Exception in thread "main" VncException: / by zero
    at: / (script: line 4, col 19)
    at: fn3 (script: line 3, col 19)
    at: fn2 (script: line 2, col 19)
    at: fn1 (script: line 5, col 5)
Caused by: java.lang.ArithmeticException: / by zero
```


## Functions

### Creating functions

```clojure
(do
   (defn add [x y] (+ x y))
   
   (def mul (fn [x y] (* x y)))
   
   (add 1 2)
   (mul 3 4)
   (let [f (fn [x y] (- x y))] (f 5 3)))
```

### Variadic functions

```clojure
(do
   (defn log
      [message & args]
      (apply println (cons message (cons ": " args))))

   (log "message from" "192.0.0.76" "12:00:00"))
```

### Multi-Arity functions

```clojure
(do
   (defn arity
      ([] (println "arity 0"))
      ([a] (println "arity 1"))
      ([a b] (println "arity 2"))
      ([a b c] (println "arity 3"))
      ([a b c & z] (println "arity 3+")))
      
   (arity 1 2))
```

### Anonymous functions

```clojure
(do
   (map (fn [x] (* 2 x)) (range 0 10))   
   (map #(* 2 %) (range 0 10)) 
   (map #(* 2 %1) (range 0 10))  
   (let [f #(+ %1 %2 %3)] (f 1 2 3)))
```

### Functions with preconditions

```clojure
(do
   (defn sum 
         [x y] 
         { :pre [(> x 0) (> y 0)] } 
         (+ x y)))
```


### Maps and Keywords as functions

```clojure
(do
   ; instead of (get {:a 1 :b 2} :b)
   ; maps/keys work as functions
   ({:a 1 :b 2} :b)
   (:b {:a 1 :b 2}))
```


## Destructuring

### Sequential Destructuring

Sequential destructuring breaks up a sequential data structure as a Venice 
list or vector within a let binding.

```clojure
(do
   (let [[x y z] [1 2 3]]
      (println x y z))
      ;=> 1 2 3

   (let [[x y z] '(1 2 3)]
      (println x y z))
      ;=> 1 2 3

   ;; for strings, the elements are destructured by character.
   (let [[x y z] "abc"]
     (println x y z))
     ;=> a b c
)
```

The destructured collection must not be of same size as the number of binding names

```clojure
(do
   (let [[a b c d e f] '(1 2 3)]
      (println a b c d e f))
      ;=> 1 2 3 nil nil nil
      
   (let [[a b c] '(1 2 3 4 5 6 7 8 9)]
      (println a b c))
      ;=> 1 2 3
)
```

Working with tail elements `&` and ignoring bindings `_`

```clojure
(do
   (let [[a b c & z] '(1 2 3 4 5 6 7 8 9)]
      (println a b c z))
      ;=> 1 2 3 (4 5 6 7 8 9)

   (let [[a _ b _ c & z] '(1 2 3 4 5 6 7 8 9)]
      (println a b c z))
      ;=> 1 3 5 (6 7 8 9)
)
```

Binding the entire collection with `:as`

```clojure
(do
   (let [[a b c & z :as all] '(1 2 3 4 5 6 7 8 9)]
      (println a b c z all))
      ;=> 1 2 3 (4 5 6 7 8 9) (1 2 3 4 5 6 7 8 9)
)
```

Nested bindings

```clojure
(do
   (def line [[5 10] [10 20]])
   (let [[[x1 y1][x2 y2]] line]
      (printf "Line from (%d,%d) to (%d,%d)" x1 y1 x2 y2))
      ;=> "Line from (5,10) to (10,20)"
)
```

`:as` or `&` can be used at any level

```clojure
(do
   (def line [[5 10] [10 20]])
   (let [[[a b :as group1] [c d :as group2]] line]
     (println a b group1)
     (println c d group2))
     ;=> 5 10 [5 10]
     ;=> 10 20 [10 20])
)
```


### Associative Destructuring

Associative destructuring breaks up an associative (key/value) data structure 
as a Venice map within a let binding.

```clojure
(do
   (let [{a :a, b :b, c :c} {:a "A" :b "B" :d "D"}]
      (println a b c))
      ; => A B nil
)
```

```clojure
(do
   (def map_keyword {:a "A" :b "B" :c 3 :d 4})
   (def map_strings {"a" "A" "b" "B" "c" 3 "d" 4})
   
   (let [{:keys [a b c]} map_keyword]
      (println a b c))
      ; => A B 3
      
   (let [{:strs [a b c]} map_strings]
      (println a b c))
      ; => A B 3
)
```

Binding the entire collection with `:as`

```clojure
(do
   (def map_keyword {:a "A" :b "B" :c 3 :d 4})

   (let [{:keys [a b c] :as all} map_keyword]
      (println a b c all))
      ; => A B 3 {:a A :b B :c 3 :d 4}
)
```

Binding with defaults `:or`

```clojure
(do
  (defn configure [options]
     (let [{:keys [port debug verbose] :or {port 8000, debug false, verbose false}} options]
     (println "port =" port " debug =" debug " verbose =" verbose)))
     ;=> port 8000, debug false, verbose false

  (configure {:debug true})
)
```

Associative destructuring can be nested and combined with sequential destructuring

```clojure
(do
   (def users
      {:peter {:role "clerk"
               :branch "Zurich"
               :age 40}
               
       :magda {:role "head of HR"
               :branch "Bern"
               :age 45}
               
       :kurt  {:role "assistant"
               :branch "Lucerne"
               :age 32}})

   (let [{{:keys [role branch]} :peter} users]
      (println "Peter is a" role "located at" branch))
      ;=> Peter is a clerk located at Zurich
)
```



## Advanced String features

### Triple quoted, multi-line strings

```clojure
(do
   (def s1 """{ "fruit": "apple", "color": "red" }""")
   
   (def s2 """{ 
                "fruit": "apple",
                "color": "red" 
              }""")

   ; strip-indent removes the indentation on multi-line strings. The indentation
   ; will be determined from the first line's indentation. Escaping the first 
   ; line of the multi-line string with '\' makes strip-indent work as expected.  
   (def s3 (str/strip-indent """\
                {
                  "fruit": "apple",
                  "color": "red"
                }"""))
               
   (println s1))
   (println s2))
   (println s3))
```

### Interpolation 

Interpolation is controlled using `~{}` and `~()` forms. The former is 
used for simple value replacement while the latter can be used to
embed the results of arbitrary function invocation into the produced 
string.

_Interpolation is implemented as a reader macro. It's parsed at read time and turned into a_ 
`(str args)` _function._

```clojure
(do
   (let [x 100] 
      (println "x: ~{x}")
      (println "f(x): ~(inc x)")))
```

```clojure
(do
   (let [x 100] 
      (println """x: ~{x}""")
      (println """f(x): ~(inc x)""")))
```


## Recursion:

```clojure
(do
   (defn sum [n]
      (loop [cnt n, acc 0]
         (if (zero? cnt)
            acc
            (recur (dec cnt) (+ acc cnt)))))

   (sum 100000))
```



## Java Interop

Venice supports calling Java constructors, static and instance methods as well as 
accessing static class and instance fields.

The Venice types long, double, and decimal are coerced to Java's primitive and
object types byte, short, int, long, float, double, Byte, Short, Integer, Long, 
Float, Double, and BigDecimal.


```clojure
(do
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
```


Java enum values can be passed as simple or scoped keywords:

```clojure
(do
   (import :java.time.LocalDate)

   (. :LocalDate :of 1994 :JANUARY 21)   
   (. :LocalDate :of 1994 :java.time.Month.JANUARY 21))
```


Java VarArgs:

```clojure
(. :java.lang.String :format "%s: %d" ["abc" 100])
```


Exception handling

```clojure
(do
   (import :java.lang.RuntimeException)
   (import :java.io.IOException)
  
   (try
      (throw (. :RuntimeException :new "a message"))
      (catch :IOException ex (:message ex))
      (catch :RuntimeException ex (:message ex))
      (finally (println "... finally."))))
```

```clojure
(do
   (try
      (throw [1 2 3])  ; ValueException
      (catch :ValueException ex (pr-str (:value ex)))))
```


Try with resources

```clojure
(do
   (import :java.io.FileInputStream)
   
   (let [file (io/temp-file "test-", ".txt")]
        (io/spit file "123456789" :append true)
        (try-with [is (. :FileInputStream :new file)]
           (io/slurp-stream is :binary false))))
```

Java Callbacks:

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


Mixing Venice functions with Java streams:

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


## Concurrency


### Atoms

```clojure
(do
   (def counter (atom 2))
   (swap! counter + 2))
   (deref counter))
```

### Futures & Promises

```clojure
(do
   (defn task [] (sleep 1000) 200)
   (deref (future task))        
```

```clojure
(do
   (def p (promise))
   (defn task [] (sleep 500) (deliver p 123))

   (future task)
   (deref p))
```


### Delays

```clojure
(do  
   (def x (delay (println "realizing...") 100))
   (sleep 1000)
   (deref x))
```


### Agents

while agents accept functions to process the agent's state...

```clojure
(do
   (def x (agent 100))
   (send x + 5)
   (await-for 100 x)
   @x)
```

actors accept data to be processed by the actor's function

```clojure
;; simple actors implemented on top of agents
(do
   (def actors (atom {}))

   (defn wait [timeout] (apply await-for timeout (vals @actors)))

   (defn make! [name state handler]
         (let [actor (agent {:state state :handler handler})]
            (swap! actors assoc name actor)))

   (defn invoke-handler [context msg]
         (let [{:keys [state handler]} context
               new-state (handler state msg)]
            (assoc context :state new-state)))

   (defn send! [name & args]
         (let [actor (get @actors name)]
            (send actor invoke-handler args)
            nil))

   (make! :printer nil (fn [_ msg] (apply println msg)))
       
   (send! :printer "hello world")
 
   (wait 200)
   
   nil)
```


## Sandbox

The Venice sandbox allows a program to execute _Venice_ in a restricted sandbox 
environment regarding Java interop. It is useful for applications that want 
to provide some degree of scriptability to users, without allowing them to 
read/write files, execute `System.exit(0)`, or any other undesirable operations.


#### Multi-Threading

The sandbox is local to a thread. This allows multi-threaded applications to 
isolate execution properly, but it also means you cannot let Venice to create 
threads, or else it will escape the sandbox.

To ensure this you should prohibit the use of threads. The only safe way to 
work with threads and respecting the sandbox is by using Venice' built-in futures
and agents.


#### No blacklisting

Unlike a sandbox provided by _Java SecurityManager_, this sandboxing is only a 
skin deep. In other words, even if you prohibit Venice from executing a Java 
operation X, if an attacker finds another Java method Y that calls into X, he 
can execute X.

This in practice means you have to whitelist what's OK, as opposed to blacklist 
things that are problematic, because you'll never know all the static methods 
that are available to the script in the JVM!


#### Features

 - whitelist Java classes down to individual methods and fields
 - whitelist Java system property access down to individual properties
 - blacklist all or individual Venice I/O functions like spit, slurp, ...
 - prohibit calls to all Venice I/O functions and Java fully
 - limiting the execution time of a script
 

#### Example

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.*;

// for details see javadoc of class "com.github.jlangch.venice.javainterop.SandboxRules"
final IInterceptor interceptor =
    new SandboxInterceptor(
        new SandboxRules()
              .rejectAllVeniceIoFunctions()
              .allowAccessToStandardSystemProperties()
              .withSystemProperties("db.name", "db.port")
              .withMaxExecTimeSeconds(5)
              .withClasses(
                "java.lang.Math:PI"
                "java.lang.Math:min", 
                "java.time.ZonedDateTime:*", 
                "java.awt.**:*", 
                "java.util.ArrayList:new",
                "java.util.ArrayList:add"));

final Venice venice = new Venice(interceptor);

// rule: "java.lang.Math:PI"
// => OK (static field)
venice.eval("(. :java.lang.Math :PI)"); 

// rule: "java.lang.Math:min"
// => OK (static method)
venice.eval("(. :java.lang.Math :min 20 30)"); 
    
// rule: "java.time.ZonedDateTime:*
// => OK (constructor & instance method)
venice.eval("(. (. :java.time.ZonedDateTime :now) :plusDays 5))"); 
 
// rule: "java.util.ArrayList:new" and "java.util.ArrayList:add"
// => OK (constructor & instance method)
venice.eval(
    "(doto (. :java.util.ArrayList :new)  " +
    "      (. :add 1)                     " +
    "      (. :add 2))                    ");
	
// rule: "java.awt.**:*"
// => OK
venice.eval(
    "(-<> (. :java.awt.color.ColorSpace :CS_LINEAR_RGB)      " +
    "     (. :java.awt.color.ICC_ColorSpace :getInstance <>) " +
    "     (. <> :getMaxValue 0))                             ");

// => FAIL (invoking non whitelisted static method)
venice.eval("(. :java.lang.System :exit 0)"); 

// => FAIL (invoking rejected Venice I/O function)
venice.eval("(io/slurp \"/tmp/file\")"); 

// => FAIL exceeded max exec time of 5s
venice.eval("(sleep 30000)"); 

// => FAIL (accessing non whitelisted system property)
venice.eval("(system-prop \"db.password\")"); 
```


Prohibit Venice I/O functions and Java Interop for completely safe 
scripting:

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.*;

final Venice venice = new Venice(new RejectAllInterceptor());

...

```


## A larger sample

Alternative to UNIX shell scripts:

```clojure
;; ----------------------------------------------------------------------------------
;; Zips the last month's Tomcat log files
;;
;; > java -jar venice-1.3.6.jar -file zip-tomcat-logs.venice ./logs
;; ----------------------------------------------------------------------------------
(do
   (defn tomcat-log-file-filter [prefix year month]
         (let [regex (str/format "%s[.]%d-%02d-[0-9][0-9][.]log" prefix year month)]
            (fn [dir name] (match name regex))))

   (defn tomcat-log-file-zip [prefix dir year month]
         (io/file dir (str/format "%s.%d-%02d.zip" prefix year month)))

   (defn find-log-files [dir filter]
         (map #(io/file dir %)
              (. dir :list (proxify :java.io.FilenameFilter {:accept filter}))))

   (defn zip-files [dir zip files]
         (with-sh-throw
            (with-sh-dir dir
               (apply sh (concat ["zip" (:name zip)] (map #(:name %) files))))))

   (defn zip-tomcat-logs [prefix dir year month]
         (try
            (let [zip (tomcat-log-file-zip prefix dir year month)
                  filter (tomcat-log-file-filter prefix year month)
                  logs (find-log-files dir filter)]
               (printf "Compacting %s ...\n" prefix)
               (printf "   Found %d log files\n" (count logs))
               (when-not (empty? logs)
                  (zip-files dir zip logs)
                  (printf "   Zipped to %s\n" (:name zip))
                  (apply io/delete-file logs)
                  (printf "   Removed %d files\n" (count logs))))
            (catch :com.github.jlangch.venice.ShellException ex
               (printf "Error compacting %s: %s" prefix (:message ex)))))

   (defn first-day-of-month [offset]
         (-> (time/local-date) 
             (time/first-day-of-month) 
             (time/plus :month offset)))

   (let [dir (io/file (nth *ARGV* 2))
         date (first-day-of-month -1)
         year (time/year date)
         month (time/month date)]
      (if (io/exists-dir? dir)
         (do
            (printf "Compacting %d-%02d logs from '%s' ...\n" year month dir)
            (zip-tomcat-logs "localhost_access_log" dir year month)
            (zip-tomcat-logs "host-manager" dir year month)
            (zip-tomcat-logs "manager" dir year month)
            (zip-tomcat-logs "localhost" dir year month)
            (zip-tomcat-logs "catalina" dir year month)
            (println "Done."))
         (printf "Error: The Tomcat log dir '%s' does not exist" dir))))
```


## Extension Modules

Through extension modules Venice provides specific functionality
that not all application require thus keeping load time and 
resource usage low if the modules are not used.

Extension Modules are plain Venice scripts and must be loaded 
explicitely.

Venice supports

  - JSON
  - Charts
  - WebDAV
  
through extension modules
  

### JSON

Venice supports JSON if the [Jackson](https://github.com/FasterXML/jackson) libs are on the runtime classpath:

 - jackson-core-2.9.x.jar
 - jackson-databind-2.9.x.jar
 - jackson-datatype-jdk8-2.9.x.jar (optional Jdk8 module)
 
The Jackson _jdk8_ module is loaded automatically if it is available
 

```clojure
(do
   ;; load the Venice JSON extension module
   (load-module :json)
   
   ;; build json from a map (returns a json string)
   (json/to-json {:a 100 :b 100 :c [10 20 30]})
   (json/to-pretty-json [{:a 100 :b 100}, {:a 200 :b 200}])

   ;; pretty print json (returns a json string)
   (json/pretty-print (json/to-json {:a 100 :b 100}))

   ;; parse json from a string (returns a map/list)
   (json/parse """{"a": 100, "b": 100, "c": [10,20,30]}""")
   (json/parse """[{"a": 100,"b": 100}, {"a": 200, "b": 200}]"""))
```


### Charts

Venice supports rendering charts if the [XChart](https://knowm.org/open-source/xchart/) library is on the runtime 
classpath:

 - xchart-3.5.x.jar
 

##### Line Chart Example

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/line-chart.png" width="300">


```clojure
(do
   (load-module :xchart)

   (xchart/write-to-file
      (xchart/xy-chart
         { "y(x)" { :x [0.0 1.0 2.0]
                    :y [0.0 0.8 2.0] } }
         { :title "Line Chart"
           :render-style :line   ; :step
           :x-axis { :title "X" :decimal-pattern "#0.0"}
           :y-axis { :title "Y" :decimal-pattern "#0.0"}
           :theme :xchart } )
      :png ; write as PNG
      120  ; render with 120 dpi
      (. :java.io.File :new "line-chart.png")))
```

##### Area Chart Example

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/area-chart.png" width="300">

```clojure
(do
   (load-module :xchart)

   (xchart/write-to-file
      (xchart/xy-chart
         { "a" { :x [0.0  3.0  5.0  7.0  9.0]
                 :y [0.0  8.0 12.0  9.0  8.0] }
           "b" { :x [0.0  2.0  4.0  6.0  9.0]
                 :y [2.0  9.0  7.0  3.0  7.0] }
           "c" { :x [0.0  1.0  3.0  8.0  9.0]
                 :y [1.0  2.0  4.0  3.0  4.0] } }

         { :title "Area Chart"
           :render-style :area   ; :step-area
           :legend {:position :inside-ne}
           :x-axis { :title "X" :decimal-pattern "#0.#"}
           :y-axis { :title "Y" :decimal-pattern "#0.#"}
           :theme :xchart } )
      :png ; write as PNG
      120  ; render with 120 dpi
      (. :java.io.File :new "area-chart.png")))
```

##### Scatter Chart Example

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/scatter-chart.png" width="300">

```clojure
(do
   (load-module :xchart)

   (defn rand-list [count max] (map (fn [x] (rand-long max)) (range count)))

   (xchart/write-to-file
      (xchart/xy-chart
         { "Rand 1" { :x (rand-list 8 10)
                      :y (rand-list 8 10) }
           "Rand 2" { :x (rand-list 8 10)
                      :y (rand-list 8 10) } }
         { :title "Scatter Chart"
           :render-style :scatter
           :marker { :size 20 }
           :x-axis { :title "X" :decimal-pattern "#0.0" :min 0.0 :max 10.0 }
           :y-axis { :title "Y" :decimal-pattern "#0.0" :min 0.0 :max 10.0 }
           :theme :xchart } )
      :png ; write as PNG
      120  ; render with 120 dpi
      (. :java.io.File :new "scatter-chart.png")))
```

##### Bubble Chart Example

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/bubble-chart.png" width="300">

```clojure
(do
   (load-module :xchart)

   (def series1
        [ {:x  1 :y  2 :bubble 30}
          {:x 10 :y  8 :bubble 12}
          {:x 12 :y 16 :bubble 15}
          {:x 20 :y 25 :bubble 24} ])
   (def series2
        [ {:x 10 :y  4 :bubble 30}
          {:x  5 :y  5 :bubble 36}
          {:x  6 :y 20 :bubble 50}
          {:x 18 :y 20 :bubble  9} ])
   (defn bubblify [series]
         {:x (map (fn [t] (:x t)) series)
          :y (map (fn [t] (:y t)) series)
          :bubble (map (fn [t] (:bubble t)) series)})

   (xchart/write-to-file
      (xchart/bubble-chart
         {"Series 1" (bubblify series1)
          "Series 2" (bubblify series2) }
         { :title "Bubble Chart"
           :legend {:position :inside-sw}
           :x-axis {:title "Series 2"}
           :y-axis {:title "Series 1"}
           :theme :xchart } )
      :png ;; write as PNG
      120  ;; render with 120 dpi
      (. :java.io.File :new "bubble-chart.png")))
```

##### Bar Chart Example

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/bar-chart.png" width="300">

```clojure
(do
   (load-module :xchart)

   (xchart/write-to-file
      (xchart/category-chart
         { "Bananas" {"Mon" 6, "Tue" 2, "Fri" 3, "Wed" 1, "Thu" 3}
           "Apples" {"Tue" 3, "Wed" 5, "Fri" 1, "Mon" 1}
           "Pears" {"Thu" 1, "Mon" 3, "Fri" 4, "Wed" 1} }           
         { :title "Weekly Fruit Sales"
           :theme :xchart 
           :x-axis {:order ["Mon" "Tue" "Wed" "Thu" "Fri"] } } )
      :png ;; write as PNG
      120  ;; render with 120 dpi
      (. :java.io.File :new "bar-chart.png")))
```

##### Pie Chart Example

<img src="https://github.com/jlangch/venice/blob/master/doc/charts/pie-chart.png" width="300">

```clojure
(do
   (load-module :xchart)

   (xchart/write-to-file
      (xchart/pie-chart
         { "A" 400
           "B" 310
           "C" 50 } 
         { :title "Pie Chart"
           :render-style :pie
           :theme :xchart } )
      :png ;; write as PNG
      120  ;; render with 120 dpi
      (. :java.io.File :new "pie-chart.png")))
```

### WebDAV

Venice supports WebDAV if the [Sardine](https://github.com/lookfirst/sardine) libs are on the runtime classpath:

 - sardine-5.9.jar
 
transitive dependencies:
 
  - httpclient-4.5.2.jar
  - httpcore-4.4.4.jar
  - httpcore-nio-4.4.4.jar
  - httpmime-4.5.2.jar
  - commons-logging-1.2-api.jar
  - commons-logging-1.2.jar
 
```clojure
(do
  (load-module :webdav)

  (webdav/with {:username "jon.doe" :password "123456"}
     (let [url "http://0.0.0.0:8080/foo/webdav/document.doc" ]
          (do
             (webdav/exists? url)
             (webdav/list "http://0.0.0.0:8080/foo/webdav/" 1)
             (webdav/delete! "http://0.0.0.0:8080/foo/webdav/foo.doc")
             (webdav/get-as-file url "download.doc")
             (webdav/put-file! url "upload.doc" "application/msword")))))
```


## Build Dependencies


#### Gradle

```groovy
dependencies {
    compile 'com.github.jlangch:venice:1.3.6'
}
```


#### Maven

```xml
<dependencies>
    <dependency>
        <groupId>com.github.jlangch</groupId>
        <artifactId>venice</artifactId>
        <version>1.3.6</version>
    </dependency>
</dependencies>
```


