# Sandbox with the REPL

The initial REPL sandbox accepts all Java calls and Venice functions without any restrictions

```
venice> !sandbox status
No sandbox active (AcceptAllInterceptor)
venice> 
```

#### Change to a restricted _reject-all_ sandbox

```
venice> !sandbox reject-all
venice> !sandbox status
Sandbox active (RejectAllInterceptor). Rejects all Java calls and default blacklisted Venice functions
venice> !sandbox config
[reject-all] SAFE restricted sandbox
Java calls:
   All rejected!
Whitelisted Venice modules:
   crypt
   kira
   math
   walk
   xml
Blacklisted Venice functions:
   agent
   agent-error
   agent-error-mode
     :
venice> 
```

#### Change to a _customized_ sandbox

```
venice> !sandbox customized
venice> !sandbox status
Customized sandbox active (SandboxInterceptor)
venice> !sandbox config
[customized] Customized sandbox
Sandbox rules:
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

## Testing the _reject-all_ sandbox

#### Enable the _reject-all_ sandbox

```
venice> !sandbox reject-all
```

#### Test the sandbox

```clojure
; all Venice I/O functions are rejected
(io/exists-dir? (io/file "/tmp"))
```

```clojure
; all Java calls are rejected
(. :java.lang.Math :min 2 3)
```

## Testing the _customized_ sandbox

#### Enable the _customized_ sandbox.

```
venice> !sandbox customized
```

#### Test the sandbox

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

#### Customize the sandbox...
* enable calls to _java.lang.Math_
* enable access to system property _java.class.path_
* enable access to system environment variable _JAVA_HOME_
* blacklist all Venice I/O functions (using the group ref `*io*`)
* whitelist rules to offset the blacklisted `*io*` rule: `print`, `printf`, `println`, `newline`, `io/print` to allow printing values
* blacklist the Venice 'count' function

```
venice> !sandbox customized
venice> !sandbox add-rule class:java.lang.Math:*
venice> !sandbox add-rule system.property:java.class.path
venice> !sandbox add-rule system.env:JAVA_HOME
venice> !sandbox add-rule blacklist:venice:func:*io*
venice> !sandbox add-rule whitelist:venice:func:print
venice> !sandbox add-rule whitelist:venice:func:printf
venice> !sandbox add-rule whitelist:venice:func:println
venice> !sandbox add-rule whitelist:venice:func:newline
venice> !sandbox add-rule whitelist:venice:func:io/print
venice> !sandbox add-rule blacklist:venice:func:count
```

#### ...and test it

```clojure
; Java calls to java.lang.Math are accepted
(. :java.lang.Math :min 2 3)
```

```clojure
; Accessing system property 'java.class.path'
(system-prop :java.class.path)
```

```clojure
; Accessing environment variable 'JAVA_HOME'
(system-env :JAVA_HOME)
```

```clojure
; all Venice I/O functions are rejected (except the whitelisted printing functions)
(io/exists-dir? (io/file "/tmp"))
```

```clojure
; the Venice I/O function 'println' is allowed
(println 1000)
```

```clojure
; the Venice function 'count' is rejected
(count [1 2 3])
```
