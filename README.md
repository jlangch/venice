[![](https://github.com/jlangch/venice/blob/master/doc/license.svg)](./LICENSE)
[![](https://github.com/jlangch/venice/blob/master/doc/maven-central.svg)](http://mvnrepository.com/artifact/com.github.jlangch/venice)


# Venice

Venice, a Clojure inspired sandboxed Lisp dialect with Java interoperability serving as 
a safe scripting language.


## Overview

Venice is a Lisp dialect born from the need for a safe, powerful scripting 
and expression language that can be used to implement scriptable extension 
points and rules for applications.

Venice supports macros, tail-recursion, dynamic code loading, dynamic 
(thread-local) binding. It comes with excellent Java interoperability, and a 
configurable sandbox that can prevent all sorts of dangerous JVM interactions 
like reading/writing files, invoking _System.exit(0)_ or any other malicious 
action.

Venice's immutable persistent data structures together with Clojure style atoms, 
futures, promises, and agents greatly simplify writing concurrent code.

Because Venice does not depend on any runtime libraries (other than the JVM) you 
can easily add it as standalone .jar to your classpath.

Venice requires Java 8 or newer.

 
 
## Cheat Sheet

Cheat Sheet: [HTML](https://cdn.rawgit.com/jlangch/venice/964de70/cheatsheet.html) [PDF](https://cdn.rawgit.com/jlangch/venice/964de70/cheatsheet.pdf)



## Documentation

* [REPL](doc/readme/repl.md)
* [Execute Venice scripts](doc/readme/execute-scripts.md)
* [Embedding Venice in Java](doc/readme/embedding.md)
* [Datatypes](doc/readme/datatypes.md)
* [Functions](doc/readme/functions.md)
* [Transducers](doc/readme/transducers.md)
* [Mutable Refs](doc/readme/refs.md)
* [Destructuring](doc/readme/destructuring.md)
* [Advanced string features](doc/readme/advanced-strings.md)
* [Concurrency](doc/readme/concurrency.md)
* [Java interoperability](doc/readme/java-interop.md)
* [Namespaces](doc/readme/namespace.md)
* [Exception handling](doc/readme/exceptions.md)
* [Sandbox](doc/readme/sandbox.md)
* [JSON](doc/readme/json.md)
* [PDF](doc/readme/pdf.md)
* [Extension modules](doc/readme/extension-modules.md)
* See a larger [example](doc/readme/large-example.md)
* [Benchmarks & Profiling](doc/readme/profiling.md)
* [Build dependencies](doc/readme/build-dependencies.md)
* [3rd Party Libs](doc/readme/3rdparty-lic.md)
* [Change Log](ChangeLog.md)
