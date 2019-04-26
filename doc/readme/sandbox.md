# Sandbox

The Venice sandbox allows a program to execute _Venice_ in a restricted sandbox 
environment regarding Java interop. It is useful for applications that want 
to provide some degree of scriptability to users, without allowing them to 
read/write files, execute `System.exit(0)`, or any other undesirable operations.


### Multi-Threading

The sandbox is local to a thread. This allows multi-threaded applications to 
isolate execution properly, but it also means you cannot let Venice to create 
threads, or else it will escape the sandbox.

To ensure this you should prohibit the use of threads. The only safe way to 
work with threads and respecting the sandbox is by using Venice' built-in futures
and agents.


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
              .rejectAllVeniceIoFunctions()
              .rejectVeniceFunctions(
              	"time/date",
              	"time/zone-ids")
              .withStandardSystemProperties()
              .withSystemProperties("db.name", "db.port")
              .withClasspathResources("resources/images/*.png")
              .withClasses(
                "java.lang.Math:PI"
                "java.lang.Math:min", 
                "java.time.ZonedDateTime:*", 
                "java.awt.**:*", 
                "java.util.ArrayList:new",
                "java.util.ArrayList:add")
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
 

## Sandbox with the REPL

The initial REPL sandbox accepts all Java calls and Venice functions without any restrictions

```
venice> !sandbox status
No sandbox active (AcceptAllInterceptor)
venice> 
```

Change to a restricted _reject-all_ sandbox

```
venice> !sandbox reject-all
venice> !sandbox status
Sandbox active (RejectAllInterceptor). Rejects all Java calls and default blacklisted Venice functions
venice> !sandbox config
[reject-all]
Blacklisted Venice functions:
   agent
   agent-error
   agent-error-mode
     :
venice> 
```

Change to a _customized_ sandbox

```
venice> !sandbox customized
venice> !sandbox status
Customized sandbox active (SandboxInterceptor)
venice> !sandbox config
[customized]
Sandbox rules (whitelist):
   class:java.io.IOException:*
   class:java.io.InputStream
   class:java.io.OutputStream
   class:java.io.PrintStream:append
   class:java.lang.Boolean
   class:java.lang.Byte
   class:java.lang.Character
   class:java.lang.Double
   class:java.lang.Exception:*
   class:java.lang.Float
     :
venice> 
```

### Testing the _reject-all_ sandbox in the REPL

Enable the _reject-all_ sandbox

```
venice> !sandbox reject-all
```

Test

```clojure
; all Venice I/O functions are rejected
(io/exists-dir? (io/file "/tmp"))
```

```clojure
; all Java calls are rejected
(. :java.lang.Math :min 2 3)
```

### Testing the _customized_ sandbox in the REPL

Enable the _customized_ sandbox.

```
venice> !sandbox customized
```

Test

```clojure
; Venice I/O functions are accepted
(io/exists-dir? (io/file "/tmp"))
```

```clojure
; Java calls matching the default rules are accepted
(. :java.util.Date :new)
```

```clojure
; Java calls not matching the default rules are rejected
(. :java.lang.Math :min 2 3)
```

Customize (enable calls to _java.lang.Math_)

```
venice> !sandbox customized
venice> !sandbox add-rule class:java.lang.Math:*
venice> !sandbox add-rule blacklist:venice:*io*
venice> !sandbox add-rule blacklist:venice:count
```

```clojure
; Java calls to java.lang.Math are accepted
(. :java.lang.Math :min 2 3)
```

```clojure
; all Venice I/O functions are rejected
(io/exists-dir? (io/file "/tmp"))
```

```clojure
; the Venice function 'count' is rejected
(count [1 2 3])
```
