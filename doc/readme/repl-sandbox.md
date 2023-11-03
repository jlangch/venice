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

; Exception in thread "main" SecurityException: Venice Sandbox (RejectAllInterceptor): 
; Access denied to Venice function 'io/file'!
;
; [Callstack]
;     at: io/file (user: line 1, col 18)
```

```clojure
; all Java calls are rejected
(. :java.lang.Math :min 2 3)

; Exception in thread "main" SecurityException: Venice Sandbox (RejectAllInterceptor): 
; Access denied to Venice function '.'!
;
; [Callstack]
;    at: . (user: line 1, col 2)
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

; => true
```

```clojure
; Java calls matching the default rules are accepted
(. :java.util.Date :new)

; => Fri Nov 03 19:05:34 CET 2023
```

```clojure
; Java calls not matching the default rules are rejected
(. :java.lang.Math :min 2 3)

; Exception in thread "main" SecurityException: Venice Sandbox: Access denied to 
; accessor java.lang.Math::min. File <user> (1,1)
; 
; [Callstack]
;    at: . (user: line 1, col 2)
```

#### Blacklisting/Whitelisting groups of functions
To simplify sandbox configuration Venice supports predefined function groups:
* all Venice I/O functions (using the group ref `*io*`)
* all Venice I/O printing functions (using the group ref `*print*`)
* all Venice unsafe special forms (using the group ref `*special-forms*`)
* all Venice concurrency functions (using the group ref `*concurrency*`)
* all Venice system functions (using the group ref `*system*`)
* all Venice Java Interop functions (using the group ref `*java-interop*`)
* all Venice unsafe functions (using the group ref `*unsafe*`)

Use the REPL sandbox command 'fn-group' to list the functions in a specific group

```
venice> !sandbox fn-group
venice> !sandbox fn-group *print*
```

#### Customize the sandbox...
* enable calls to _java.lang.Math_
* enable access to system property _venice.repl.home_
* enable access to system environment variable _REPL_HOME_
* blacklist all Venice I/O functions (using the group ref `*io*`)
* whitelist rule `*print*` to offset the blacklisted `*io*` rule to allow printing values
* blacklist the Venice 'count' function

```
venice> !sandbox customized
venice> !sandbox add-rule class:java.lang.Math:*
venice> !sandbox add-rule system.property:venice.repl.home
venice> !sandbox add-rule system.env:REPL_HOME
venice> !sandbox add-rule blacklist:venice:func:*io*
venice> !sandbox add-rule whitelist:venice:func:*print*
venice> !sandbox add-rule blacklist:venice:func:count
```

#### ...and test it

```clojure
; Java calls to java.lang.Math are accepted
(. :java.lang.Math :min 2 3)
```

```clojure
; Accessing system property 'venice.repl.home'
(system-prop :venice.repl.home)
```

```clojure
; Accessing environment variable 'REPL_HOME'
(system-env :REPL_HOME)
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
