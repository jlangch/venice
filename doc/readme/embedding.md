# Embedding Venice in Java

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


## Passing parameters to Venice

Venice expects Java objects as parameters and returns Java objects as the expression result. It coerces 
Java data types to/from Venice data types implicitly. Basic types as Boolean, Long, Double, String, 
BigDecimal, List, Map, ... are coerced to Venice types like long, double, decimal, string, list, ... 
All other types can be accessed through Java interop. Java bean parameters expose its getters as Map 
keys in Venice, so the getters can be accessed simply through `(:getterName bean)`

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.Parameters;

public class Embed_02_PassingParameters {
    public static void main(final String[] args) {
        final Venice venice = new Venice();

        System.out.println(
                venice.eval(
                        "(+ x y 1)", 
                        Parameters.of("x", 6, "y", 3L)));

        System.out.println(
                venice.eval(
                        "(str \"(x: \" (:x point) \", y: \" (:y point) \")\")", 
                        Parameters.of("point", new Point(100, 200))));
    }
}
```


## Venice stdout redirection

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


## Precompiling Venice

Precompiling Venice speeds up evaluation significantly when calling an expression 
multiple times with different parameters. Running precompiled scripts is threadsafe. 
Every evaluation gets its own private Venice context.

If required precompiled scripts can be serialized/deserialized.

```java
import java.util.stream.IntStream;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.PreCompiled;
import com.github.jlangch.venice.Parameters;

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



## Precompilation Performance Benchmark

| Embed Type                       | Elapsed |   Per Call |
| :---                             |    ---: |       ---: |
| No precompilation                | 32.57 s | 3430.00 us |
| Precompilation                   |   1.47s |   18.43 us |
| Precompilation / macro expansion |   0.68s |    7.12 us |


### Without precompilation

```java
import com.github.jlangch.venice.*;

public class Embed_05_PrecompiledShootout_1 {

    public static void main(final String[] args) {
        final int iterations = 10000;
        final String expr = "(cond (< x 0) -1 (> x 0) 1 :else 0)";

        final Venice venice = new Venice();
        
        for(int ii=0; ii<iterations; ii++) {
            venice.eval("test", expr, Parameters.of("x", (ii%3) - 1));
        }

        final long start = System.currentTimeMillis();
        for(int ii=0; ii<iterations; ii++) {
            venice.eval("test", expr, Parameters.of("x", (ii%3) - 1));
        }
        final long elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed : " + elapsed + "ms");
        System.out.println("Per call: " + ((elapsed * 1000) / iterations) + "us");
    }
}
```


### With precompilation

```java
import com.github.jlangch.venice.*;

public class Embed_06_PrecompiledShootout_2 {

    public static void main(final String[] args) {
        final int iterations = 10000;
        final String expr = "(cond (< x 0) -1 (> x 0) 1 :else 0)";

        final Venice venice = new Venice();
        final PreCompiled precompiled = venice.precompile("example", expr, false);

        for(int ii=0; ii<iterations; ii++) {
            venice.eval(precompiled, Parameters.of("x", (ii%3) - 1));
        }

        final long start = System.currentTimeMillis();
        for(int ii=0; ii<iterations; ii++) {
            venice.eval(precompiled, Parameters.of("x", (ii%3) - 1));
        }
        final long elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed : " + elapsed + "ms");
        System.out.println("Per call: " + ((elapsed * 1000) / iterations) + "us");
    }
}
```


### With precompilation and upfront macro expansion

```java
import com.github.jlangch.venice.*;

public class Embed_07_PrecompiledShootout_3 {

    public static void main(final String[] args) {
        final int iterations = 10000;
        final String expr = "(cond (< x 0) -1 (> x 0) 1 :else 0)";

        final Venice venice = new Venice();
        final PreCompiled precompiled = venice.precompile("example", expr, true);

        for(int ii=0; ii<iterations; ii++) {
            venice.eval(precompiled, Parameters.of("x", (ii%3) - 1));
        }

        final long start = System.currentTimeMillis();
        for(int ii=0; ii<iterations; ii++) {
            venice.eval(precompiled, Parameters.of("x", (ii%3) - 1));
        }
        final long elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed : " + elapsed + "ms");
        System.out.println("Per call: " + ((elapsed * 1000) / iterations) + "us");
    }
}
```


