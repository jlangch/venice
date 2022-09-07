# Embedding Venice in Java

* [Overview](#overview)
* [Passing Parameters](#passing-parameters)
* [stdout-stderr Redirection](#stdout-stderr-redirection)
* [Precompiling](#precompiling)
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

```java
import java.util.stream.IntStream;
import com.github.jlangch.venice.*;

public class Embed_04_Precompile {
    public static void main(final String[] args) {
        final Venice venice = new Venice();


        // pre-compile and turn up-front macro expansion on
        final IPreCompiled precompiled = venice.precompile("example", "(+ 1 x)", true);

        // single-threaded
        IntStream.range(0, 100).sequential().forEach(
          ii -> System.out.println(
                  venice.eval(precompiled, Parameters.of("x", ii))));
             
        // multi-threaded
        IntStream.range(0, 100).parallel().forEach(
          ii -> System.out.println(
                  venice.eval(precompiled, Parameters.of("x", ii))));
    }
}
```


## Precompilation Benchmark

The Venice benchmarks are done using [JMH](http://openjdk.java.net/projects/code-tools/jmh/) 
(Java Microbenchmark Harness). JMH has been added to the JDK starting with JDK 12; 
for earlier versions, the dependencies have to be added explicitly.

The benchmark did run on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz).

**Results Java 8 Server VM:**

| Benchmark                              | Mode |   Cnt |      Score |       Error |   Units |
| :---                                   | ---: |  ---: |       ---: |        ---: |    ---: |
| no_precompilation_params               | avgt |     3 |   2452.398 |  ±  781.016 |   us/op |
| no_precompilation_noparams             | avgt |     3 |   2448.145 |  ± 1668.292 |   us/op |
| no_precompilation_ref                  | avgt |     3 |   2274.765 |  ±  536.594 |   us/op |
| precompilation_no_macroexpand_params   | avgt |     3 |     41.957 |  ±   11.971 |   us/op |
| precompilation_no_macroexpand_noparams | avgt |     3 |     40.418 |  ±    1.310 |   us/op |
| precompilation_macroexpand_params      | avgt |     3 |      8.586 |  ±    1.368 |   us/op |
| precompilation_macroexpand_noparams    | avgt |     3 |      7.330 |  ±    3.818 |   us/op |
| precompilation_ref                     | avgt |     3 |      5.803 |  ±    1.008 |   us/op |

The benchmark can be run on the project's Gradle build: `./gradlew jmh -Pinclude=".*PrecompileBenchmark"`


### Benchmark

```java
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import com.github.jlangch.venice.*;


@Warmup(iterations=3, time=3, timeUnit=TimeUnit.SECONDS)
@Measurement(iterations=3, time=10, timeUnit=TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.MICROSECONDS)
@State (Scope.Benchmark)
@Threads (1)
public class PrecompileBenchmark {
    @Benchmark
    public Object no_precompilation_ref(State_ state) {
        return state.venice.eval("test", state.exprRef);
    }

    @Benchmark
    public Object precompilation_ref(State_ state) {
        return state.venice.eval(state.precompiled_ref);
    }

    @Benchmark
    public Object no_precompilation_params(State_ state) {
        return state.venice.eval("test", state.expr1, state.parameters);
    }

    @Benchmark
    public Object precompilation_no_macroexpand_params(State_ state) {
        return state.venice.eval(state.precompiledNoMacroExpand_params, state.parameters);
    }

    @Benchmark
    public Object precompilation_macroexpand_params(State_ state) {
        return state.venice.eval(state.precompiledMacroExpand_params, state.parameters);
    }

    @Benchmark
    public Object no_precompilation_noparams(State_ state) {
        return state.venice.eval("test", state.expr2);
    }

    @Benchmark
    public Object precompilation_no_macroexpand_noparams(State_ state) {
        return state.venice.eval(state.precompiledNoMacroExpand_noparams);
    }

    @Benchmark
    public Object precompilation_macroexpand_noparams(State_ state) {
        return state.venice.eval(state.precompiledMacroExpand_noparams);
    }

    @State(Scope.Benchmark)
    public static class State_ {
        public String expr1 = "(+ (cond (< x 0) -1 (> x 0) 1 :else 0) " +
                              "   (cond (< y 0) -1 (> y 0) 1 :else 0) " +
                              "   (cond (< z 0) -1 (> z 0) 1 :else 0))";

        public String expr2 = "(+ (cond (< -10 0) -1 (> -10 0) 1 :else 0) " +
                              "   (cond (< 0 0)   -1 (> 0 0)   1 :else 0) " +
                              "   (cond (< 10 0)  -1 (> 10 0)  1 :else 0))";

        public String exprRef = "nil";  // most simple expression, just return nil

        public Venice venice = new Venice();
        public Map<String,Object> parameters = Parameters.of("x", -10, "y", 0, "z", 10);

        public IPreCompiled precompiledNoMacroExpand_params = venice.precompile("example", expr1, false);
        public IPreCompiled precompiledMacroExpand_params = venice.precompile("example", expr1, true);
        public IPreCompiled precompiledNoMacroExpand_noparams = venice.precompile("example", expr2, false);
        public IPreCompiled precompiledMacroExpand_noparams = venice.precompile("example", expr2, true);
        public IPreCompiled precompiled_ref = venice.precompile("example", exprRef, true);
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
                                 // blacklist all Venice I/O, system, and
                                 // concurrency functions
                                 .rejectAllIoFunctions()
                                 .rejectAllConcurrencyFunctions()
                                 .rejectAllSystemFunctions()
                                 .rejectAllSenstiveSpecialForms()
                                 // whitelist a few Java classes
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

