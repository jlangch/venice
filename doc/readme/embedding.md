# Embedding Venice in Java

* [Overview](#overview)
* [Passing Parameters](#passing-parameters)
* [STDOUT Redirection](#stdout-redirection)
* [Precompiling](#precompiling)
* [Serialize/Deserialize Precompiled Scripts](#serialize-deserialize-precompiled-scripts)
* [Precompilation Benchmark](#precompilation-benchmark)
* [Sandbox](#sandbox)


## Overview

The main purpose for embedding Venice in a Java applications is to use Venice as an expression
or rules engine. 

Precompiling these Venice expressions or rules results in performance improvement that can be
pretty impressive. See the Precompilation Performance Benchmark further down.

For security reasons it might be necessary to establish a sandbox for the Venice expressions
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


## STDOUT Redirection

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.util.CapturingPrintStream;

public class Embed_03_StdOutRedirection {
    public static void main(final String[] args) {
        final Venice venice = new Venice();

        // case 1: redirect stdout to the <null> device
        venice.eval(
           "(println [1 2])", 
           Parameters.of("*out*", null));

        // case 2: capture stdout within the script and return it as the result
        System.out.println(
           venice.eval("(with-out-str (println [1 2]))"));

        // case 3: capturing stdout preserving the script result
        try(CapturingPrintStream ps = CapturingPrintStream.create()) {
           final Object result = venice.eval(
                                   "(do (println [1 2]) 100)", 
                                   Parameters.of("*out*", ps));
           System.out.println("result: " + result);
           System.out.println("stdout: " + ps.getOutput());
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

        final PreCompiled precompiled = venice.precompile("example", "(+ 1 x)");

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


### Serialize/Deserialize Precompiled Scripts

Precompiled scripts can be serialized and deserialized to store them on
a database for example.

```java
import com.github.jlangch.venice.*;

public class Embed_11_PrecompileSerialize {
    public static void main(final String[] args) {
        final Venice venice = new Venice();

        PreCompiled precompiled = venice.precompile("example", "(+ 1 x)");
        
        final byte[] data = precompiled.serialize();
        
        // ...
        
        precompiled = PreCompiled.deserialize(data);

        System.out.println(venice.eval(precompiled, Parameters.of("x", 2)));
    }
}
```


## Precompilation Benchmark

The benchmark did run on a 2017 MacBook Pro with a Java 11 server VM.

**Results:**

| Embed Type                       |   Calls | Elapsed |   Per Call |
| :---                             |    ---: |    ---: |       ---: |
| No precompilation                |   8'000 | 32.57 s | 3430.00 us |
| Precompilation                   |  80'000 |   1.47s |   18.43 us |
| Precompilation / macro expansion |  80'000 |   0.68s |    7.12 us |

_The benchmark source code can be found in the checked in Java package_ 
_'com.github.jlangch.venice.examples'. The slowest 20% of the runs are_ 
_considered as outliers and removed._


### Without Precompilation

```java
import com.github.jlangch.venice.*;

public class Embed_05_PrecompiledShootout_1 {

    // SIMPLIFIED: see source code for details!

    public static void main(final String[] args) {
        final String expr = "(cond (< x 0) -1 (> x 0) 1 :else 0)";

        final Venice venice = new Venice();
        
        for(int ii=0; ii<100; ii++) {
            venice.eval("test", expr, Parameters.of("x", (ii%3) - 1));
        }
    }
}
```


### With Precompilation

```java
import com.github.jlangch.venice.*;

public class Embed_06_PrecompiledShootout_2 {

    // SIMPLIFIED: see source code for details!

    public static void main(final String[] args) {
        final String expr = "(cond (< x 0) -1 (> x 0) 1 :else 0)";

        final Venice venice = new Venice();
        final PreCompiled precompiled = venice.precompile("example", expr, false);

        for(int ii=0; ii<100; ii++) {
           venice.eval(precompiled, Parameters.of("x", (ii%3) - 1));
        }
    }
}
```


### With Precompilation and Upfront Macro Expansion

```java
import com.github.jlangch.venice.*;

public class Embed_07_PrecompiledShootout_3 {

    // SIMPLIFIED: see source code for details!

    public static void main(final String[] args) {
        final String expr = "(cond (< x 0) -1 (> x 0) 1 :else 0)";

        final Venice venice = new Venice();
        
        final PreCompiled precompiled = venice.precompile("example", expr, true);

        for(int ii=0; ii<100; ii++) {
           venice.eval(precompiled, Parameters.of("x", (ii%3) - 1));
        }
    }
}
```



## Sandbox

If the expressions or rules come from an untrusted source a sandbox should be set in
place to prevent malicous actions.


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

