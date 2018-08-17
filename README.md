[![](https://cdn.rawgit.com/jlangch/venice/4c324e53/license.svg)](./LICENSE)
[![](https://cdn.rawgit.com/jlangch/venice/4c324e53/maven-central.svg)](http://mvnrepository.com/artifact/com.github.jlangch/venice)


# Venice

Venice, a sandboxed Lisp with Java interoperability serving as 
a safe scripting language.


## Overview

Venice's goal is not to build just another Lisp, it's born from the need of 
having a safe, powerful scripting and expression language that can be used 
to implement scriptable extension points and rules for applications.

Venice supports macros, comes with Java interoperability and a configurable 
sandbox.

Because Venice does not depend on any runtime libraries (other than the JVM) you 
can easily add it as standalone .jar to your classpath.

Venice requires Java 8 or newer.

 
## Cheat Sheet

[Cheat Sheet HTML](https://cdn.rawgit.com/jlangch/venice/4c324e53/cheatsheet.html)

[Cheat Sheet PDF](https://cdn.rawgit.com/jlangch/venice/4c324e53/cheatsheet.pdf)


## REPL

Venice comes with a simple REPL.

Start the REPL with `java -jar venice-0.8.2.jar`

```text
venice> (+ 1 1)
=> 2
venice>
```

## Venice as expression engine

```java
import com.github.jlangch.venice.Venice;

final Venice venice = new Venice();

System.out.println(venice.eval("(+ 1 1)"));
```


### Passing parameters to Venice

Venice expects Java data types (Long, Double, String, List, Map, ...) as 
parameters and returns Java data types as the expression result. It boxes 
and unboxes Java to/from Venice data types implicitly.

Java bean parameters expose its getters as Map keys in Venice, so the 
getters can be accessed simply through `(get bean :getterName)`

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.Parameters;

final Venice venice = new Venice();

System.out.println(
    venice.eval(
        "(+ x y 3)", 
        Parameters.of("x", 6, "y", 3L)));
```


### Venice stdout redirection

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.util.CapturingPrintStream;

final Venice venice = new Venice();

// prevent writing to stdout by redirect to the <null> device
venice.eval(
    "(println [1 2])", 
    Parameters.of("*out*", null));

// capturing data written to stdout
final CapturingPrintStream ps = CapturingPrintStream.create();
venice.eval(
    "(do (println 1) (println [4 5]))", 
    Parameters.of("*out*", ps));
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

for(long ii=0; ii<100; ii++) {
    System.out.println(venice.eval(precompiled, Parameters.of("x", ii)));
}
```


## Java Interop

Venice supports calling constructors, static and instance methods as well as 
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
   (. (. :java.time.ZonedDateTime :now) :plusDays 5)

   ;; class object
   (. :java.lang.Math :class)
   (. (. :java.time.ZonedDateTime :now) :class)
)
```


Java enum values can be passed as simple or scoped keywords:

```clojure
(do
   (import :java.time.LocalDate)

   (. :LocalDate :of 1994 :JANUARY 21)
   
   (. :LocalDate :of 1994 :java.time.Month.JANUARY 21)
)
```


Java VarArgs:

```clojure
(. :java.lang.String :format "%s: %d" ["abc" 100])
```


Try with resources

```clojure
(do
   (import :java.io.FileInputStream)
   
   (let [file (io/temp-file "test-", ".txt")]
        (spit file "123456789" :append true)
        (try-with [is (. :FileInputStream :new file)]
           (io/slurp-stream is :binary false)))
)
```


Java Callbacks:

```clojure
;; File filter
(do
   (import :java.io.File :java.io.FilenameFilter)

   (def file-filter
        (fn [dir name] (string/ends-with? name ".txt")))

   (let [dir (. :File :new "/tmp")]
        (. dir :list (proxify :FilenameFilter {:accept file-filter})))
)
```

```clojure
;; Swing GUI (demonstrates passing parameters across callbacks)
(do
   (import :java.lang.Runnable)
   (import :javax.swing.JPanel)
   (import :javax.swing.JFrame)
   (import :javax.swing.JLabel)
   (import :javax.swing.SwingUtilities)

   (def swing-open-window
        (fn [title]
            (let [frame (. :JFrame :new title)
                  label (. :JLabel :new "Hello World")
                  closeOP (. :JFrame :EXIT_ON_CLOSE)]
                 (. frame :setDefaultCloseOperation closeOP)
                 (. frame :add label)
                 (. frame :setSize 200 200)
                 (. frame :setVisible true))))

   (def swing-gui
        (fn [title]
            (. :SwingUtilities :invokeLater
               (proxify :Runnable { :run (fn [] (swing-open-window title)) } ))))

   (swing-gui "Test"))
```

A larger example:

```clojure
(do
   (import :org.test.User :java.time.LocalDate)

   ;; convert a Java list to a Venice list and return the 
   ;; first item
   (first
      (into '() 
         (doto (. :java.util.ArrayList :new)
               (. :add 1)
               (. :add 2))))

   (def users [
        (. :User :new "john" 24 (. :LocalDate :of 1994 7 21)))
        (. :User :new "pete" 48 (. :LocalDate :of 1970 1 12))) ])

   (str (filter (fn [u] (> (get u :age) 30)) users))
)
```

## Sandbox

The Venice sandbox allows a program to execute _Venice_ in a restricted sandbox 
environment regarding Java interop. It is useful for applications that want 
to provide some degree of scriptability to users, without allowing them to 
execute `System.exit(0)` or any other undesirable operations.


#### Multi-Threading

The sandbox is local to a thread. This allows multi-threaded applications to 
isolate execution properly, but it also means you cannot let Venice to create 
threads, or else it will escape the sandbox.

The easiest way to do this is to ensure you prohibit the use of threads.


#### No blacklisting

Unlike a sandbox provided by _Java SecurityManager_, this sandboxing is only a 
skin deep. In other words, even if you prohibit Venice from executing a Java 
operation X, if an attacker finds another Java method Y that calls into X, he 
can execute X.

This in practice means you have to whitelist what's OK, as opposed to blacklist 
things that are problematic, because you'll never know all the static methods 
that are available to the script in the JVM!


#### Example

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.*;

final JavaInterceptor interceptor =
    new JavaSandboxInterceptor(
        new SandboxRules()
              .rejectAllVeniceIoFunctions()
              .add(
                "java.lang.Math:min", 
                "java.time.ZonedDateTime:*", 
                "java.util.ArrayList:new",
                "java.util.ArrayList:add"));

final Venice venice = new Venice(interceptor);

// => OK (static method)
venice.eval("(. :java.lang.Math :min 20 30)"); 
    
// => OK (constructor & instance method)
venice.eval("(. (. :java.time.ZonedDateTime :now) :plusDays 5))"); 
 
// => OK (constructor & instance method)
venice.eval(
    "(doto (. :java.util.ArrayList :new)  " +
    "      (. :add 1)                     " +
    "      (. :add 2))                    ");

// => FAIL (invoking non white listed static method)
venice.eval("(. :java.lang.System :exit 0)"); 

// => FAIL (invoking rejected Venice I/O function)
venice.eval("(slurp \"/tmp/file\")"); 
```

Prohibit Venice I/O functions and Java Interop:

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.*;

final Venice venice = new Venice(new RejectAllInterceptor());

...

```


## Extension Modules

Through extension modules Venice provides specific functionality
that not all application require thus keeping load time and 
resource usage low if the modules are not used.

Extension Modules are plain Venice scripts and must be loaded 
explicitely.

Venice supports

  - JSON
  - Protocol/Logger
  - Charts
  
through extension modules
  

### JSON

Venice supports JSON if the Jackson libs are on the runtime classpath:

 - jackson-core-2.9.x.jar
 - jackson-databind-2.9.x.jar
 - jackson-datatype-jdk8-2.9.x.jar (optional Jdk8 module)
 
The Jackson Jdk8 module is loaded automatically if it is available
 

```clojure
(do
   ;; load the Venice JSON extension module
   (load-module :json)
   
   ;; build json from a map (returns a json string)
   (json/to-json {:a 100 :b 100 c: [10 20 30]})
   (json/to-pretty-json [{:a 100 :b 100}, {:a 200 :b 200}])

   ;; pretty print json (returns a json string)
   (json/pretty-print (json/to-json {:a 100 :b 100}))

   ;; parse json from a string (returns a map/list)
   (json/parse (json/to-json {:a 100 :b 100 c: [10 20 30]}))
   (json/parse (json/to-json [{:a 100 :b 100}, {:a 200 :b 200}]))
)
```

### Protocol/Logger

Venice provides a simple protocol facility.

```clojure
(do
   (load-module :protocol)
 
   ;; open
   (protocol/open)
   
   (protocol/log :INFO "test info")
   (protocol/log :WARN "test warn")
   (protocol/log :ALERT "test alert")
   
   ;; return the protocol as a string
   (protocol/to-string))
```

```clojure
(do
   (load-module :protocol)
 
   ;; open and attach to the current out stream
   (protocol/open)
   (protocol/attach-os *out*)
   
   (protocol/log :INFO "test info")
   (protocol/log :WARN "test warn")
   (protocol/log :ALERT "test alert"))
```


### Charts

_TODO: document_



## Build Dependencies


#### Gradle

```groovy
dependencies {
    compile 'com.github.jlangch:venice:0.8.2'
}
```


#### Maven

```xml
<dependencies>
    <dependency>
        <groupId>com.github.jlangch</groupId>
        <artifactId>venice</artifactId>
        <version>0.8.2</version>
    </dependency>
</dependencies>
```


