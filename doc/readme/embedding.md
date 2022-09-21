# Embedding Venice in Java

* [Overview](#overview)
* [Passing Parameters](#passing-parameters)
* [stdout-stderr Redirection](#stdout-stderr-redirection)
* [Handle exceptions](#handle-exceptions)
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
            run();
            System.exit(0);
        }
        catch(VncException ex) {
            ex.printVeniceStackTrace();
            System.exit(1);
        }
        catch(RuntimeException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void run() {
        final Venice venice = new Venice();

        final Long result = (Long)venice.eval("(+ 1 2)");

        System.out.println(result);
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

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;

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
import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.util.CapturingPrintStream;

public class Embed_03_StdOutRedirection {
        final Venice venice = new Venice();

        Object result;

        // #1: redirect stdout/stderr to the <null> device
        result = venice.eval(
                   "(do               \n" +
                   "  (println [1 2]) \n" +
                   "  10)             ",
                   Parameters.of("*out*", null,
                                 "*err*", null));
        System.out.println("result: " + result);
        System.out.println();
        // result: 10

        // #2: capture stdout within the script and return it as the result
        result = venice.eval(
                    "(with-out-str     \n" +
                    "  (println [1 2]) \n" +
                    "  10)             ");
        System.out.println("result: " + result);
        // result: [1 2]

        // #3: capturing stdout/stderr preserving the script result
        try(CapturingPrintStream ps_out = new CapturingPrintStream();
            CapturingPrintStream ps_err = new CapturingPrintStream()
        ) {
           result = venice.eval(
                         "(do                        \n" +
                         "  (println [3 4])          \n" +
                         "  (println *err* :failure) \n" +
                         "  100)                     ",
                         Parameters.of("*out*", ps_out,
                                       "*err*", ps_err));
           System.out.println("result: " + result);
           System.out.print("stdout: " + ps_out.getOutput());
           System.out.print("stderr: " + ps_err.getOutput());
           // result: 100
           // stdout: [3 4]
           // stderr: :failure
        }
    }
}
```


## Handle exceptions

Venice prints informative stack traces with `VncException::printVeniceStackTrace()` 


```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;

public class Embed_05_Exceptions {
    public static void main(final String[] args) {
        try {
            final String script =
                    "(do                               \n" +
                    "  (defn speed [distance time]     \n" +
                    "     (/ distance time))           \n" +
                    "                                  \n" +
                    "   (str (speed 20 0) \"km/h\"))   ";

            new Venice().eval("test", script);
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

This prints a nice stack trace with the name of the function 
and the source location for every call stack level:

```clojure
(do                                 ;; line 1
  (defn speed [distance time]       ;; line 2
     (/ distance time))             ;; line 3
                                    ;; line 4
   (str (speed 20 0) "km/h"))       ;; line 5
```

```text
Exception in thread "main" VncException: / by zero

[Callstack]
    at: / (test: line 3, col 7)
    at: user/speed (test: line 5, col 10)
```

On the other hand `VncException::printStackTrace()` prints a technical Java 
stack trace that is mostly not of much value:

```text
com.github.jlangch.venice.VncException: / by zero
    at com.github.jlangch.venice.impl.types.VncLong.div(VncLong.java:205)
    at com.github.jlangch.venice.impl.functions.MathFunctions$4.apply(MathFunctions.java:247)
    at com.github.jlangch.venice.impl.VeniceInterpreter.evaluate(VeniceInterpreter.java:737)
    at com.github.jlangch.venice.impl.FunctionBuilder.evaluateBody(FunctionBuilder.java:270)
    at com.github.jlangch.venice.impl.FunctionBuilder.access$3(FunctionBuilder.java:264)
    at com.github.jlangch.venice.impl.FunctionBuilder$1.apply(FunctionBuilder.java:157)
    at com.github.jlangch.venice.impl.VeniceInterpreter.evaluate(VeniceInterpreter.java:745)
    at com.github.jlangch.venice.impl.VeniceInterpreter.evaluate_sequence_values(VeniceInterpreter.java:866)
    at com.github.jlangch.venice.impl.VeniceInterpreter.evaluate(VeniceInterpreter.java:680)
    at com.github.jlangch.venice.impl.VeniceInterpreter.EVAL(VeniceInterpreter.java:225)
    at com.github.jlangch.venice.impl.VeniceInterpreter.RE(VeniceInterpreter.java:247)
    at com.github.jlangch.venice.Venice.lambda$1(Venice.java:328)
    at com.github.jlangch.venice.Venice.runWithSandbox(Venice.java:485)
    at com.github.jlangch.venice.Venice.eval(Venice.java:322)
    at com.github.jlangch.venice.Venice.eval(Venice.java:261)
    at com.github.jlangch.venice.examples.Embed_05_Exceptions.run(Embed_05_Exceptions.java:55)
    at com.github.jlangch.venice.examples.Embed_05_Exceptions.main(Embed_05_Exceptions.java:32)
```



## Precompiling

Precompiling Venice speeds up evaluation significantly when calling an expression 
multiple times with different parameters. Running precompiled scripts is threadsafe. 
Every evaluation gets its own private Venice context.

```java
import com.github.jlangch.venice.IPreCompiled;
import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;

public class Embed_04_Precompile {
    public static void main(final String[] args) {
        final Venice venice = new Venice();

        // turn up-front macro expansion on
        final IPreCompiled pc = venice.precompile("example", "(+ 1 x)", true);

        // single-threaded
        IntStream.range(0, 100).sequential().forEach(
          ii -> System.out.println(
                  venice.eval(pc, Parameters.of("x", ii))));

        // multi-threaded
        IntStream.range(0, 100).parallel().forEach(
          ii -> System.out.println(
                  venice.eval(pc, Parameters.of("x", ii))));
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
| precompilation_no_macroexpand_params   | avgt |     3 |     38.511 |  ±    4.331 |   us/op |
| precompilation_no_macroexpand_noparams | avgt |     3 |     37.198 |  ±    1.744 |   us/op |
| precompilation_macroexpand_params      | avgt |     3 |      6.672 |  ±    0.173 |   us/op |
| precompilation_macroexpand_noparams    | avgt |     3 |      5.682 |  ±    1.664 |   us/op |
| precompilation_ref                     | avgt |     3 |      4.157 |  ±    0.339 |   us/op |

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
import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;

public class Embed_09_StrictSandbox {
    public static void main(final String[] args) {
        // disable all Java calls and all Venice IO functions
        // like 'println', 'slurp', ...
        //
        final Venice venice = new Venice(new RejectAllInterceptor());

        // => FAIL (Venice IO function) with Sandbox SecurityException
        try {
           venice.eval("(println 100)");
        }
        catch(SecurityException ex) {
            System.out.println("REJECTED: (println 100)");
        }
    }
}
```


### Customized Sandbox

A customized sandbox allows the configuration of all aspects for Java and
Venice calls.


```java
import com.github.jlangch.venice.SecurityException;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;
import com.github.jlangch.venice.javainterop.SandboxRules;

public class Embed_10_CustomSandbox {

    public static void main(final String[] args) {
        try {
            run();
        }
        catch(VncException ex) {
            ex.printVeniceStackTrace();
        }
        catch(RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    public static void run() {
        final Venice venice = new Venice(createSandbox());

        // rule: "java.lang.Math:PI"
        // => OK (whitelisted static field)
        venice.eval("(. :java.lang.Math :PI)");
        System.out.println("OK      : (. :java.lang.Math :PI)");

        // rule: "java.lang.Math:min"
        // => OK (whitelisted static method)
        venice.eval("(. :java.lang.Math :min 20 30)");
        System.out.println("OK      : (. :java.lang.Math :min 20 30)");

        // rule: "java.time.ZonedDateTime:*
        // => OK (whitelisted constructor & instance method)
        venice.eval("(. (. :java.time.ZonedDateTime :now) :plusDays 5))");
        System.out.println("OK      : (. (. :java.time.ZonedDateTime :now) :plusDays 5))");

        // rule: "java.util.ArrayList:new" and "java.util.ArrayList:add"
        // => OK (whitelisted constructor & instance method)
        venice.eval(
            "(doto (. :java.util.ArrayList :new)  " +
            "      (. :add 1)                     " +
            "      (. :add 2))                    ");
        System.out.println("OK      : java.util.ArrayList::new()");

        // rule: "java.awt.**:*"
        // => OK (whitelisted)
        venice.eval(
            "(-<> (. :java.awt.color.ColorSpace :CS_LINEAR_RGB)      " +
            "     (. :java.awt.color.ICC_ColorSpace :getInstance <>) " +
            "     (. <> :getMaxValue 0))                             ");
        System.out.println("OK      : use of java.awt.** classes");

        // => FAIL (invoking non whitelisted static method)
        try {
            venice.eval("(. :java.lang.System :exit 0)");
        }
        catch(SecurityException ex) {
            System.out.println("REJECTED: (. :java.lang.System :exit 0)");
        }

        // => FAIL (invoking blacklisted Venice I/O function)
        try {
            venice.eval("(io/slurp \"/tmp/file\")");
        }
        catch(SecurityException ex) {
            System.out.println("REJECTED: (io/slurp ...)");
        }

        // => OK (invoking whitelisted Venice I/O function 'println')
        venice.eval("(println 100)");
        System.out.println("OK:  (println 100)");

        // => FAIL exceeded max exec time of 3s
        try {
            venice.eval("(sleep 10_000)");
        }
        catch(SecurityException ex) {
            System.out.println("EXCEEDED: max exec time on (sleep ...)");
        }

        // => FAIL (accessing non whitelisted system property)
        try {
            venice.eval("(system-prop \"db.password\")");
        }
        catch(SecurityException ex) {
            System.out.println("REJECTED: (system-prop ...)");
        }

        // => FAIL (accessing non whitelisted system environment variable)
        try {
            venice.eval("(system-env \"USER\")");
        }
        catch(SecurityException ex) {
            System.out.println("REJECTED: (system-env ...)");
        }

        // => FAIL (accessing non whitelisted classpath resources)
        try {
             venice.eval("(io/load-classpath-resource \"resources/images/img.tiff\")");
        }
        catch(SecurityException ex) {
            System.out.println("REJECTED: (io/load-classpath-resource ...)");
        }
    }


    private static SandboxInterceptor createSandbox() {
        return new SandboxInterceptor(
                    new SandboxRules()
                        // Venice functions: blacklist all unsafe functions
                        .rejectAllUnsafeFunctions()

                        // Venice functions:  blacklist additional functions
                        .rejectVeniceFunctions(
                            "time/date",
                            "time/zone-ids")

                        // Venice functions: whitelist rules for print functions to offset
                        // blacklist rules by individual functions
                        .whitelistVeniceFunctions("*print*")

                        // Venice functions: whitelist Java calls offsets the black list
                        // rule for java interop from SandboxRules::rejectAllUnsafeFunctions()
                        .whitelistVeniceFunctions(".")

                        // Java interop: whitelist rules
                        .withStandardSystemProperties()
                        .withSystemProperties("db.name", "db.port")
                        .withSystemEnvs("SHELL", "HOME")
                        .withClasspathResources("resources/images/*.png")
                        .withClasses(
                            "java.lang.Math:PI",
                            "java.lang.Math:min",
                            "java.time.ZonedDateTime:*",
                            "java.awt.**:*",
                            "java.util.ArrayList:new",
                            "java.util.ArrayList:add")

                        // Venice extension modules: whitelist rules
                        .withVeniceModules(
                            "crypt",
                            "kira",
                            "math")

                        // Generic rules
                        .withMaxFutureThreadPoolSize(20)
                        .withMaxExecTimeSeconds(3));
    }
}
```

