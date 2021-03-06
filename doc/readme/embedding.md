# Embedding Venice in Java

* [Overview](#overview)
* [Passing Parameters](#passing-parameters)
* [stdout-stderr Redirection](#stdout-stderr-redirection)
* [Precompiling](#precompiling)
* [Serialize-Deserialize Precompiled Scripts](#serialize-deserialize-precompiled-scripts)
* [Precompilation Benchmark](#precompilation-benchmark)
* [Sandbox](#sandbox)


## Overview

The main purpose for embedding Venice in Java applications is to use Venice as an expression
or rules engine. 

Precompiling these Venice expressions or rules results in performance improvement that can be
pretty impressive. See the Precompilation Performance Benchmark further down.

For security reasons it might be necessary to establish a **sandbox** for the Venice expressions
and rules.


### A simple example:

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;

public class Embed_01_Simple {
    public static void main(final String[] args) {
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
    }
}
```


## Passing Parameters

Venice expects Java objects as parameters and returns Java objects as the expression result. It coerces 
Java data types to/from Venice data types implicitly. Basic types as Boolean, Long, Double, String, 
BigDecimal, List, Map, ... are coerced to Venice types like long, double, decimal, string, list, ... 
All other types can be accessed through Java interop. Java bean parameters expose its getters as Map 
keys in Venice, so the getters can be accessed simply through `(:getterName bean)`

```java
import java.awt.Point;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.Parameters;

public class Embed_02_PassingParameters {
    public static void main(final String[] args) {
        final Venice venice = new Venice();

        // pass two long parameters
        // returns a long: 10
        System.out.println(
                venice.eval(
                        "(+ x y 1)", 
                        Parameters.of("x", 6, "y", 3L)));

        // up-front macro expansion, returns a long: 10 
        System.out.println(
                venice.eval(
                        "test",
                        "(+ x y 1)", 
                        true, // up-front macro expansion
                        Parameters.of("x", 6, "y", 3L)));

        // pass a java.awt.Point
        // returns a string: "Point=(x: 100.0, y: 200.0)"
        System.out.println(
                venice.eval(
                        "(str \"Point=(x: \" (:x point) \", y: \" (:y point) \")\")", 
                        Parameters.of("point", new Point(100, 200))));

        // pass two long parameters
        // returns a java.awt.Point: [x=100,y=200]
        System.out.println(
                venice.eval(
                        "(. :java.awt.Point :new x y)", 
                        Parameters.of("x", 100, "y", 200)));
    }
}
```


## stdout-stderr Redirection

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.util.CapturingPrintStream;

public class Embed_03_StdOutRedirection {
    public static void main(final String[] args) {
        final Venice venice = new Venice();

        // case 1: redirect stdout/stderr to the <null> device
        venice.eval(
           "(println [1 2])", 
           Parameters.of("*out*", null, 
                         "*err*", null));

        // case 2: capture stdout within the script and return it as the result
        System.out.println(
           venice.eval("(with-out-str (println [1 2]))"));

        // case 3: capturing stdout/stderr preserving the script result
        try(CapturingPrintStream ps_out = CapturingPrintStream.create();
        	CapturingPrintStream ps_err = CapturingPrintStream.create()
        ) {
           final Object result = venice.eval(
                                   "(do (println [1 2]) 100)", 
                                   Parameters.of("*out*", ps_out, 
                                                 "*err*", ps_err));
           System.out.println("result: " + result);
           System.out.println("stdout: " + ps_out.getOutput());
           System.out.println("stderr: " + ps_err.getOutput());
        }
    }
}
```


## Precompiling

Precompiling Venice speeds up evaluation significantly when calling an expression 
multiple times with different parameters. Running precompiled scripts is threadsafe. 
Every evaluation gets its own private Venice context.

If required precompiled scripts can be serialized/deserialized.

```java
import java.util.stream.IntStream;
import com.github.jlangch.venice.*;

public class Embed_04_Precompile {
    public static void main(final String[] args) {
        final Venice venice = new Venice();


        // turn up-front macro expansion on
        final PreCompiled precompiled = venice.precompile("example", "(+ 1 x)", true);

        // single-threaded
        IntStream.range(0, 100).sequential().forEach(
          ii -> System.out.println(
                  venice.eval(
                     precompiled, 
                     Parameters.of("x", ii))));
             
        // multi-threaded
        IntStream.range(0, 100).parallel().forEach(
          ii -> System.out.println(
                  venice.eval(
                     precompiled, 
                     Parameters.of("x", ii))));
    }
}
```


### Serialize-Deserialize Precompiled Scripts

Precompiled scripts can be serialized and deserialized to store them on
a database for example.

```java
import com.github.jlangch.venice.*;

public class Embed_11_PrecompileSerialize {
    public static void main(final String[] args) {
        final Venice venice = new Venice();

        PreCompiled precompiled = venice.precompile("example", "(+ 1 x)", true);
        
        final byte[] data = precompiled.serialize();
        
        // ...
        
        precompiled = PreCompiled.deserialize(data);

        System.out.println(venice.eval(precompiled, Parameters.of("x", 2)));
    }
}
```


## Precompilation Benchmark

The Venice benchmarks are done using [JMH](http://openjdk.java.net/projects/code-tools/jmh/) 
(Java Microbenchmark Harness). JMH has been added to the JDK starting with JDK 12; 
for earlier versions, the dependencies have to be added explicitly.

The benchmark did run on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz).

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


### Benchmark

```java
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.github.jlangch.venice.*;
import org.openjdk.jmh.annotations.*;


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.MICROSECONDS)
@State (Scope.Benchmark)
@Threads (1)
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



## Sandbox

If the expressions or rules come from an untrusted source a sandbox should be set in
place to prevent malicious actions.


### Strict Sandbox

Disables all Java calls and all Venice IO functions


```java
import com.github.jlangch.venice.*;
import com.github.jlangch.venice.javainterop.*;

public class Embed_09_StrictSandbox {
    public static void main(final String[] args) {
        final Venice venice = new Venice(new RejectAllInterceptor());

        // => FAIL (Venice IO function) with Sandbox SecurityException
        venice.eval("(println 100)"); 
    }
}
```


### Customized Sandbox

A customized sandbox allows the configuration of all aspects for Java and
Venice calls.


```java
import com.github.jlangch.venice.*;
import com.github.jlangch.venice.javainterop.*;

public class Embed_10_CustomSandbox {

    public static void main(final String[] args) {
        final IInterceptor interceptor =
                new SandboxInterceptor(
                        new SandboxRules()
                                .rejectAllVeniceIoFunctions()
                                .withClasses(
                                    "java.lang.Math:PI", 
                                    "java.lang.Math:min", 
                                    "java.lang.Math:max", 
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

        // rule: "java.lang.Math:max"
        // => OK (static method)
        venice.eval("(. :java.lang.Math :max 20 30)"); 

        // rule: "java.time.ZonedDateTime:*"
        // => OK (constructor & instance method)
        venice.eval("(. (. :java.time.ZonedDateTime :now) :plusDays 5))"); 

        // rule: "java.awt.**:*"
        // => OK (constructor & instance method)
        venice.eval("(. (. :java.awt.color.ICC_ColorSpace                  \n" +
                    "      :getInstance                                    \n" +
                    "      (. :java.awt.color.ColorSpace :CS_LINEAR_RGB))  \n" +
                    "   :getMaxValue                                       \n" +
                    "   0)                                                 ");

        // rule: "java.util.ArrayList:new"
        // => OK (constructor)
        venice.eval("(. :java.util.ArrayList :new)");

        // rule: "java.util.ArrayList:add"
        // => OK (constructor & instance method)
        venice.eval(
                "(doto (. :java.util.ArrayList :new)  " +
                "      (. :add 1)                     " +
                "      (. :add 2))                    ");

        // => FAIL (static method) with Sandbox SecurityException
        venice.eval("(. :java.lang.System :exit 0)"); 
    }
}
```

