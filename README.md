[![](https://github.com/jlangch/venice/blob/master/doc/license.svg)](./LICENSE)
[![](https://github.com/jlangch/venice/blob/master/doc/maven-central.svg)](https://search.maven.org/search?q=a:venice)
![Java CI](https://github.com/jlangch/venice/workflows/Java%20CI/badge.svg?branch=master)
[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/jlangch/venice) 

# Venice

Venice is a Clojure inspired sandboxed Lisp dialect with excellent Java 
interoperability.


## Overview

Venice is a Lisp dialect born from the need for a safe, sandboxed language that 
is suitable to drive standalone applications as well as serving as a scripting 
and expression language that can be used to implement scriptable extension 
points and rules for applications. 

Venice supports macros, tail-recursion, dynamic code loading, dynamic 
(thread-local) binding. It comes with excellent Java interoperability, and a 
configurable sandbox that can prevent all sorts of dangerous JVM interactions 
like reading/writing files, invoking  _System.exit(0)_  or any other malicious 
action. Venice has been designed from the ground-up with a sandbox making it 
a first class citizen.

Venice's immutable persistent data structures together with Clojure style atoms, 
futures, promises, and agents greatly simplify writing concurrent code.

Because Venice does not depend on any runtime libraries (other than the JVM) you 
can easily add it as standalone .jar to your classpath.

Venice requires Java 8 or newer.

Want to try Venice in a REPL? [Test it on GitPod](https://github.com/jlangch/venice/blob/master/doc/readme/start.md#venice-gitpod-workspace)



## Cheat Sheet

Cheat Sheet: [HTML](https://cdn.rawgit.com/jlangch/venice/ae5a1ec/cheatsheet.html) [PDF](https://cdn.rawgit.com/jlangch/venice/ae5a1ec/cheatsheet.pdf)



## Change Log

[Change Log](ChangeLog.md)



## Documentation

* [Getting started](doc/readme/start.md)
* [REPL](doc/readme/repl.md)
* [Introduction to Functional Programming](doc/readme/intro-functional.md)
* [Execute Venice scripts](doc/readme/execute-scripts.md)
* [Embedding Venice in Java](doc/readme/embedding.md)
* [Datatypes](doc/readme/datatypes.md)
* [Custom Datatypes](doc/readme/datatypes-custom.md)
* [Lazy Sequences](doc/readme/lazy-seq.md)
* [Functions](doc/readme/functions.md)
* [Control Flow](doc/readme/control-flow.md)
* [Filter-Map-Reduce](doc/readme/filter-map-reduce.md)
* [Transducers](doc/readme/transducers.md)
* [Recursion](doc/readme/recursion.md)
* [Mutable Refs](doc/readme/refs.md)
* [Destructuring](doc/readme/destructuring.md)
* [Advanced string features](doc/readme/advanced-strings.md)
* [Concurrency](doc/readme/concurrency.md)
* [Java interoperability](doc/readme/java-interop.md)
* [Namespaces](doc/readme/namespace.md)
* [Exception handling](doc/readme/exceptions.md)
* [Macros](doc/readme/macros.md)
* [Sandbox](doc/readme/sandbox.md)
* [JSON](doc/readme/json.md)
* [CSV](doc/readme/csv.md)
* [PDF](doc/readme/pdf.md)
* [Shell Scripts](doc/readme/shell-scripts.md)
* [Extension modules](doc/readme/extension-modules.md)
* [Benchmarks & Profiling](doc/readme/profiling.md)
* [Tree walker](doc/readme/walk.md)
* [Multi-File Apps](doc/readme/multi-file-app.md)
* [Development Tools](doc/readme/dev-tools.md)
* [Build dependencies](doc/readme/build-dependencies.md)
* [Performance comparison Venice - Clojure - Java](doc/readme/performance.md)
* [3rd Party Libs](doc/readme/3rdparty-lic.md)

