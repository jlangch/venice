# Sandbox

The Venice sandbox allows a program to execute _Venice_ in a restricted sandbox 
environment regarding Java interop and Venice functions. It is useful for 
applications that want to provide some degree of scriptability to users, 
without allowing them to read/write files, execute `System.exit(0)`, or any other 
undesirable operations.


### Multi-Threading

The sandbox is local to a thread. This allows multi-threaded applications to 
isolate execution properly. 

When using Venice' built-in [Concurrency](concurrency.md) features like futures, 
agents, delays, schedulers, ..., Venice ensures that the underlying threads 
inherit the configured sandbox and are operating properly within the sandbox. 

However if you create your own, unmanaged threads, given the sandbox allows it, 
these threads have always a restricted sandbox attached rejecting all Java calls 
and Venice I/O functions and prohibiting access to stdin, stdout and stderr.

The "Dining Philosophers" example in the [Concurrency](concurrency.md) section 
demonstrates how to use Venice futures instead of bare Java threads.


### No blacklisting for Java interop

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
 - whitelist Java environment variables access down to individual vars
 - whitelist individual Venice extension modules like :shell, :maven, ...
 - blacklist all Venice I/O functions like `io/spit`, `io/slurp`, ...
 - blacklist all unsafe system functions like running GC, reading system properties and
   environment variables, ...
 - blacklist all unsafe special forms for dynamically loading code, managing vars, 
   namespaces, ...
 - blacklist individual Venice functions like `time/date`, ...
 - limiting the execution time of a script
 

### Example


#### Customized sandbox

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.*;

// for details see javadoc of class "com.github.jlangch.venice.javainterop.SandboxRules"
final IInterceptor interceptor =
        new SandboxRules()
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

              // Venice functions: blacklist group rules
              .rejectAllIoFunctions()
              .rejectAllConcurrencyFunctions()
              .rejectAllSystemFunctions()
              .rejectAllSenstiveSpecialForms()

              // Venice functions: blacklist rule for individual functions
              .rejectVeniceFunctions(
                  "time/date",
                  "time/zone-ids")
                
              // Venice functions: whitelist rules for print functions to offset
              // blacklist rules by individual functions
              .whitelistVeniceFunctions("*print*")

              // Generic rules	
              .withMaxFutureThreadPoolSize(20)
              .withMaxExecTimeSeconds(5)

              .sandbox();

final Venice venice = new Venice(interceptor);

// rule: "java.lang.Math:PI"
// => OK (whitelisted static field)
venice.eval("(. :java.lang.Math :PI)"); 

// rule: "java.lang.Math:min"
// => OK (whitelisted static method)
venice.eval("(. :java.lang.Math :min 20 30)"); 

// rule: "java.time.ZonedDateTime:*
// => OK (whitelisted constructor & instance method)
venice.eval("(. (. :java.time.ZonedDateTime :now) :plusDays 5))"); 

// rule: "java.util.ArrayList:new" and "java.util.ArrayList:add"
// => OK (whitelisted constructor & instance method)
venice.eval(
    "(doto (. :java.util.ArrayList :new)  " +
    "      (. :add 1)                     " +
    "      (. :add 2))                    ");
	
// rule: "java.awt.**:*"
// => OK (whitelisted)
venice.eval(
    "(-<> (. :java.awt.color.ColorSpace :CS_LINEAR_RGB)      " +
    "     (. :java.awt.color.ICC_ColorSpace :getInstance <>) " +
    "     (. <> :getMaxValue 0))                             ");

// => FAIL (invoking non whitelisted static method)
venice.eval("(. :java.lang.System :exit 0)"); 

// => FAIL (invoking blacklisted Venice I/O function)
venice.eval("(io/slurp \"/tmp/file\")"); 

// => OK (invoking whitelisted Venice I/O function 'println')
venice.eval("(println 100)"); 

// => FAIL exceeded max exec time of 5s
venice.eval("(sleep 30000)"); 

// => FAIL (accessing non whitelisted system property)
venice.eval("(system-prop \"db.password\")"); 

// => FAIL (accessing non whitelisted system environment variable)
venice.eval("(system-env \"USER\")"); 

// => FAIL (accessing non whitelisted classpath resources)
venice.eval("(io/load-classpath-resource "resources/images/img.tiff")"); 
```


#### Sandbox rejecting all 'unsafe' functions

Prohibit all 'unsafe' functions:

 - Java interop
 - all Venice I/O functions
 - all Venice concurrency functions
 - most Venice extension modules
 - all unsafe system functions like running GC, reading system properties and
   environment variables, ...
 - all unsafe special forms for dynamically loading code, managing vars, 
   namespaces, ...
     

```java
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;

final Venice venice = new Venice(new RejectAllInterceptor());

...
```

To verify which functions / modules are prohibited check the
reject-all sandbox in the REPL:

```
venice> !sandbox reject-all
venice> !sandbox config
[reject-all] SAFE restricted sandbox
Java calls:
   All rejected!
Whitelisted Venice modules:
   crypt
   kira
   math
     :
Blacklisted Venice functions:
   agent
   agent-error
   agent-error-mode
     :
venice> 
```


#### Sandbox turned off

Run Venice scripts with no restrictions:

```java
import com.github.jlangch.venice.Venice;

final Venice venice = new Venice();

...
```

#### Creating your own unmanaged threads

As mentioned above you can create your own threads if the configured 
sandbox allows it. 

However what you can do within these threads is very limited because a 
restricted sandbox (`:RejectAllInterceptor`)is attached to this unmanaged 
threads.

This means:

- No access to Java Calls
- No access to Venice I/O functions
- No access to 'load-file' and 'load-resource'
- No access to stdin, stdout, and stderr
- Access only to a few extension modules ("crypt", "kira", "xml")

```clojure
(do
  (defn async [f] (-> (. :java.lang.Thread :new f) 
                      (. :start)))

  (def fruits (atom ()))

  (async #(swap! fruits conj :apple))
  (async #(swap! fruits conj :mango))
  (async #(swap! fruits conj :orange))

  (sleep 2 :seconds)
 
  @fruits)
```
 

## Sandbox with the REPL

The sandbox can be managed and tested from within the REPL: [managing the sandbox](repl-sandbox.md)
