# Embedding Venice in Java

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


## Passing parameters to Venice

Venice expects Java objects as parameters and returns Java objects as the expression result. It coerces 
Java data types to/from Venice data types implicitly. Basic types as Boolean, Long, Double, String, 
BigDecimal, List, Map, ... are coerced to Venice types like long, double, decimal, string, list, ... 
All other types can be accessed through Java interop. Java bean parameters expose its getters as Map 
keys in Venice, so the getters can be accessed simply through `(:getterName bean)`

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.Parameters;

final Venice venice = new Venice();

System.out.println(
   venice.eval(
      "(+ x y 3)", 
      Parameters.of("x", 6, "y", 3L)));
```


## Venice stdout redirection

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
      "(with-out-str (println [1 2]))"));

// capturing stdout preserving the script result
final CapturingPrintStream ps = CapturingPrintStream.create();
System.out.println(
   venice.eval(
      "(do (println [1 2]) 100)", 
      Parameters.of("*out*", ps)));
System.out.println(ps.getOutput());
```


## Precompiling Venice

Precompiling Venice speeds up evaluation significantly when calling an expression 
multiple times with different parameters. Running precompiled scripts is threadsafe. 
Every evaluation gets its own private Venice context.

If required precompiled scripts can be serialized/deserialized.

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.PreCompiled;
import com.github.jlangch.venice.Parameters;

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

```
