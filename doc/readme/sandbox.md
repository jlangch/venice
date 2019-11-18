# Sandbox

The Venice sandbox allows a program to execute _Venice_ in a restricted sandbox 
environment regarding Java interop. It is useful for applications that want 
to provide some degree of scriptability to users, without allowing them to 
read/write files, execute `System.exit(0)`, or any other undesirable operations.


### Multi-Threading

The sandbox is local to a thread. This allows multi-threaded applications to 
isolate execution properly, but it also means you cannot let Venice to create 
threads through Java interop, or else it will escape the sandbox.

To ensure this you should prohibit the use of threads. The only safe way to 
work with threads and respecting the sandbox is by using Venice' built-in 
[Concurrency](concurrency.md) features like futures, agents, delays, schedulers, ...

The "Dining Philosophers" example in the [Concurrency](concurrency.md) section 
demonstrates how to use Venice futures instead of bare Java threads.


### No blacklisting

Unlike a sandbox provided by _Java SecurityManager_, this sandboxing is only a 
skin deep. In other words, even if you prohibit Venice from executing a Java 
operation X, if an attacker finds another Java method Y that calls into X, he 
can execute X.

This in practice means you have to whitelist what's OK, as opposed to blacklist 
things that are problematic, because you'll never know all the static methods 
that are available to the script in the JVM!


### Features

 - whitelist Java classes down to individual methods and fields
 - whitelist Java system property access down to individual properties
 - blacklist all or individual Venice I/O functions like spit, slurp, ...
 - prohibit calls to all Venice I/O functions and Java fully
 - limiting the execution time of a script
 

### Example

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.*;

// for details see javadoc of class "com.github.jlangch.venice.javainterop.SandboxRules"
final IInterceptor interceptor =
    new SandboxInterceptor(
        new SandboxRules()
              // Java interop: whitelist rules
              .withStandardSystemProperties()
              .withSystemProperties("db.name", "db.port")
              .withSystemEnvs("SHELL", "HOME")
              .withClasspathResources("resources/images/*.png")
              .withClasses(
                "java.lang.Math:PI"
                "java.lang.Math:min", 
                "java.time.ZonedDateTime:*", 
                "java.awt.**:*", 
                "java.util.ArrayList:new",
                "java.util.ArrayList:add")
              
              // Venice functions: blacklist rules
              .rejectAllVeniceIoFunctions()
              .rejectVeniceFunctions(
              	"time/date",
              	"time/zone-ids")
              
              // Venice extension modules: blacklist rules
              .rejectVeniceModules(
              	"shell", 
              	"tomcat", 
              	"tomcat-util")
              
              // Generic rules	
              .withMaxFutureThreadPoolSize(20)              
              .withMaxExecTimeSeconds(5));

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

// => FAIL (accessing non whitelisted system environment variable)
venice.eval("(system-env \"USER\")"); 

// => FAIL (accessing non whitelisted classpath resources)
venice.eval("(io/load-classpath-resource "resources/images/img.tiff")"); 
```


Prohibit Venice I/O functions and Java Interop for completely safe 
scripting:

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.*;

final Venice venice = new Venice(new RejectAllInterceptor());

...
```


Run Venice scripts with no restrictions:

```java
import com.github.jlangch.venice.Venice;

final Venice venice = new Venice();

...
```
 

## Sandbox with the REPL

The sandbox can be managed and tested from within the REPL: [managing the sandbox](repl-sandbox.md)
