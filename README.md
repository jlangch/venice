# Venice

Venice, a sandboxed Lisp interpreter running on Java.


## Overview

Venice's goal is not to build another Lisp, it's born from the need of 
having a safe and powerful expression language that can be used to implement 
configurable extension points and rules for applications.

Venice supports macros, comes with Java interoperability, and with a configurable sandbox.

Because Venice does not depend on any libraries (other than the JVM) you can 
easily add it as standalone .jar to your classpath.

 
## Cheat Sheet

[Cheat Sheet](https://cdn.rawgit.com/jlangch/venice/b418b204/cheatsheet.html)


## REPL

Venice comes with a simple REPL that does not provide command line editing.

Start the REPL with `java -jar venice.jar`

```sh
venice> (+ 1 1)
=> 2
venice>
```

## Venice as expression engine

```java
import org.venice.Venice;

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
import org.venice.Venice;
import org.venice.Parameters;

final Venice venice = new Venice();

System.out.println(venice.eval("(+ x y 3)", Parameters.of("x", 6, "y", 3L)));
```


### Precompiling Venice

Precompiling Venice speeds up evaluation significantly when calling many 
times an expression with different parameters:

```java
import org.venice.Venice;
import org.venice.PreCompiled;
import org.venice.Parameters;

final Venice venice = new Venice();

final PreCompiled precompiled = venice.precompile("(+ 1 x)");

for(long ii=0; ii<100; ii++) {
    System.out.println(venice.eval(precompiled, Parameters.of("x", ii)));
}
```


## Java Interop

Venice supports calling constructors, static and instance methods as well as static class
fields and instance fields.

The Venice types long, double, and decimal are coerced to Java's primitive and and object types 
byte, short, int, long, float, double, Byte, Short, Integer, Long, Float, Double, and BigDecimal.


```java
import org.venice.Venice;

final Venice venice = new Venice();

System.out.println(venice.eval("(. :java.lang.Math :PI)"));
System.out.println(venice.eval("(. :java.lang.Math :min 20 30)"));
System.out.println(venice.eval("(. (. :java.time.ZonedDateTime :now) :plusDays 5)"));
```


```java
import org.venice.Venice;

final Venice venice = new Venice();

final String script =
         "(do                                                                  " +
         "   (import :org.test.User :java.time.LocalDate)                      " +
         "                                                                     " +
         "   (def users [                                                      " +
         "        (. :User :new \"john\" 24 (. :LocalDate :of 1994 7 21)))     " +
         "        (. :User :new \"pete\" 48 (. :LocalDate :of 1970 1 12))) ])  " +
         "                                                                     " +
         "   (str (filter (fn [u] (> (get u :age) 30)) users))                 " + 
         ")                                                                    ";
         
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

The easiest way to do this is to ensure you prohibit the use of Thread, and 
prevent Venice from accessing Executor-like services that let closures executed 
on different threads.

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

// => FAIL (static method) with Sandbox SecurityException
venice.eval("(. :java.lang.System :exit 0)"); 

// => FAIL Venice I/O function
venice.eval("(slurp \"/tmp/file\")"); 
```



