[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/jlangch/venice)
[![](https://github.com/jlangch/venice/blob/master/doc/maven-central.svg)](https://search.maven.org/search?q=a:venice)
[![](https://github.com/jlangch/venice/blob/master/doc/license.svg)](./LICENSE)
![Java Version](https://img.shields.io/badge/java-%3E%3D%201.8-success)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/jlangch/venice.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/jlangch/venice/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/jlangch/venice.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/jlangch/venice/alerts/)
![Java CI](https://github.com/jlangch/venice/workflows/Java%20CI/badge.svg?branch=master)


# Venice

Venice is a Clojure inspired sandboxed Lisp dialect with excellent Java 
interoperability.


## Overview

Venice is a Lisp dialect born from the need for a safe, sandboxed language that 
is suitable to serve as a scripting and expression language, to implement scriptable 
extension points and rules for applications, and to drive standalone applications. 

Venice supports macros, tail-recursion, dynamic code loading, multimethods, 
protocols and many more. It comes with excellent Java interoperability, and a 
configurable sandbox that can prevent all sorts of dangerous JVM interactions 
like reading/writing files, invoking  _System.exit(0)_  or any other malicious 
action. Venice has been designed from the ground-up with a sandbox making it 
a first class citizen.

Venice comes with library of 800+ core functions. It's immutable persistent data 
structures together with Clojure style atoms, futures, promises, and agents greatly 
simplify writing concurrent code. 

Because Venice does not depend on any runtime libraries (other than the JVM) you 
can easily add it as standalone .jar to your classpath.

Venice requires Java 8 or newer.

Want to try Venice in a REPL? [Test it on Gitpod](https://github.com/jlangch/venice/blob/master/doc/readme/start.md#venice-gitpod-workspace)



## Cheat Sheet

Cheat Sheet: [HTML](https://htmlpreview.github.io/?https://cdn.rawgit.com/jlangch/venice/11d5354/cheatsheet.html) [PDF](https://cdn.rawgit.com/jlangch/venice/11d5354/cheatsheet.pdf)



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
* [Multimethods and Protocols](doc/readme/multimethods-and-protocols.md)
* [Macros](doc/readme/macros.md)
* [Sandbox](doc/readme/sandbox.md)
* [JSON](doc/readme/json.md)
* [CSV](doc/readme/csv.md)
* [PDF](doc/readme/pdf.md)
* [Shell Scripts](doc/readme/shell-scripts.md)
* [Extension modules](doc/readme/extension-modules.md)
* [Tree walker](doc/readme/walk.md)
* [Benchmarks](doc/readme/benchmarks.md)
* [Multi-File Apps](doc/readme/multi-file-app.md)
* [Development Tools](doc/readme/dev-tools.md)
* [Build dependencies](doc/readme/build-dependencies.md)
* [Performance comparison Venice - Clojure - Java](doc/readme/performance.md)


## 3rd Party Open Source

* [Copyright Notice](doc/readme/3rdparty-lic.md)


