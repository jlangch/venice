# Venice

Venice, a sandboxed Lisp with Java interoperability implemented in Java.


## Overview

Venice's goal is not to build just another Lisp, it's born from the need of 
having a safe, powerful scripting and expression language that can be used 
to implement scriptable extension points and rules for applications.

Venice supports macros, comes with Java interoperability and a configurable 
sandbox.

Because Venice does not depend on any runtime libraries (other than the JVM) you 
can easily add it as standalone .jar to your classpath.

 
## Cheat Sheet

[Cheat Sheet HTML](https://cdn.rawgit.com/jlangch/venice/cd3133bb/cheatsheet.html)

[Cheat Sheet PDF](https://cdn.rawgit.com/jlangch/venice/cd3133bb/cheatsheet.pdf)


## REPL

Venice comes with a simple REPL.

Start the REPL with `java -jar venice-0.4.0.jar`

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

System.out.println(venice.eval("(+ x y 3)", Parameters.of("x", 6, "y", 3L)));
```


### Precompiling Venice

Precompiling Venice speeds up evaluation significantly when calling an expression multiple times with different parameters:

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.PreCompiled;
import com.github.jlangch.venice.Parameters;

final Venice venice = new Venice();

final PreCompiled precompiled = venice.precompile("(+ 1 x)");

for(long ii=0; ii<100; ii++) {
    System.out.println(venice.eval(precompiled, Parameters.of("x", ii)));
}
```

### JSON support

Venice supports JSON if the Jackson libs are on the runtime classpath:

 - jackson-core-2.9.x.jar
 - jackson-databind-2.9.x.jar
 - jackson-datatype-jdk8-2.9.x.jar (optional Jdk8 module)
 
The Jackson Jdk8 module is loaded automatically if it is available
 

```clojure
;; build json from a map (returns a json string)
(json/to-json {:a 100 :b 100 c: [10 20 30]})
(json/to-pretty-json [{:a 100 :b 100}, {:a 200 :b 200}])

;; pretty print json (returns a json string)
(json/pretty-print (json/to-json {:a 100 :b 100}))

;; parse json from a string (returns a map/list)
(json/parse (json/to-json {:a 100 :b 100 c: [10 20 30]}))
(json/parse (json/to-json [{:a 100 :b 100}, {:a 200 :b 200}]))
```


## Java Interop

Venice supports calling constructors, static and instance methods as well as 
static class fields and instance fields.

The Venice types long, double, and decimal are coerced to Java's primitive and
object types byte, short, int, long, float, double, Byte, Short, Integer, Long, 
Float, Double, and BigDecimal.


```java
import com.github.jlangch.venice.Venice;

final Venice venice = new Venice();

System.out.println(venice.eval("(. :java.lang.Math :PI)"));
System.out.println(venice.eval("(. :java.lang.Math :min 20 30)"));
System.out.println(venice.eval("(. (. :java.time.ZonedDateTime :now) :plusDays 5)"));
```


Java enum values can be passed as simple or scoped keywords:

```java
import com.github.jlangch.venice.Venice;

final Venice venice = new Venice();

System.out.println(venice.eval("(. :java.time.LocalDate :of 1994 :JANUARY 21)"));
System.out.println(venice.eval("(. :java.time.LocalDate :of 1994 :java.time.Month.JANUARY 21)"));
```


Java VarArgs:

```java
import com.github.jlangch.venice.Venice;

final Venice venice = new Venice();

System.out.println(venice.eval("(. :java.lang.String :format \"%s: %d\" '(\"abc\" 100))"));
```


A more comprehensive example:

```java
import com.github.jlangch.venice.Venice;

final Venice venice = new Venice();

final String script =
         "(do                                                                  \n" +
         "   (import :org.test.User :java.time.LocalDate)                      \n" +
         "                                                                     \n" +
         "   (def users [                                                      \n" +
         "        (. :User :new \"john\" 24 (. :LocalDate :of 1994 7 21)))     \n" +
         "        (. :User :new \"pete\" 48 (. :LocalDate :of 1970 1 12))) ])  \n" +
         "                                                                     \n" +
         "   (str (filter (fn [u] (> (get u :age) 30)) users))                 \n" + 
         ")";
         
System.out.println(venice.eval(script));
```

## Sandbox

The Venice sandbox allows a program to execute _Venice_ in a restricted sandbox 
environment regarding Java interop. It is useful for applications that want 
to provide some degree of scriptability to users, without allowing them to 
execute System.exit(0) or any other undesirable operations.


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

// => FAIL (call to non white listed static method)
venice.eval("(. :java.lang.System :exit 0)"); 

// => FAIL (call to rejected Venice I/O function)
venice.eval("(slurp \"/tmp/file\")"); 
```


