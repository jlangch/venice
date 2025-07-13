
<img src="https://github.com/jlangch/venice/blob/master/doc/logo/logo-readme.png" width="1024">

[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/jlangch/venice)
[![](https://github.com/jlangch/venice/blob/master/doc/maven-central.svg)](https://central.sonatype.com/artifact/com.github.jlangch/venice)
[![](https://github.com/jlangch/venice/blob/master/doc/license.svg)](./LICENSE)
![Java Version](https://img.shields.io/badge/java-%3E%3D%201.8-success)
[![CI](https://github.com/jlangch/venice/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/jlangch/venice/actions/workflows/ci.yml)

[![Release (latest by date)](https://img.shields.io/github/v/release/jlangch/venice)](https://github.com/jlangch/venice/releases/latest)
[![Release Date](https://img.shields.io/github/release-date/jlangch/venice?color=blue)](https://github.com/jlangch/venice/releases/latest)
[![GitHub commits since latest release (by date)](https://img.shields.io/github/commits-since/jlangch/venice/latest)](https://github.com/jlangch/venice/commits/)


# Venice

Venice is a Clojure inspired sandboxed Lisp dialect with excellent Java 
interoperability.


## Overview

Venice is a Lisp dialect born from the need for a safe, sandboxed, 
general-purpose language. It shares with Lisp the code-as-data philosophy 
and a powerful macro system. 

Venice is mainly a functional programming language focusing on immutable, 
persistent data structures.

Venice supports macros, tail-recursion, dynamic code loading, multimethods, 
protocols and many more. It comes with excellent Java interoperability, and
a configurable sandbox that can prevent all sorts of unwanted JVM and Venice 
interactions. Venice has been designed from the ground-up with a sandbox, 
making it a first class citizen.

Venice includes a comprehensive library of over 900 core functions. Its 
immutable persistent data structures, along with Clojure-style atoms, futures, 
promises, and agents, greatly simplify the process of writing concurrent code.

Venice seamlessly and transparently integrates with Java and any third-party 
libraries, giving you access to a vast array of libraries, frameworks, and 
tools.

Venice's strength is making complex scripting and automation tasks easy. Have you ever 
felt limited by Bash or PowerShell scripts? With Venice, you can write 
concise and elegant platform independent scripts effortlessly.

Venice does not depend on any runtime libraries (other than the JVM). You 
can easily add it as a standalone JAR to your classpath.

Venice requires Java 8 or newer.

Would you like to try Venice in an interactive REPL environment? 
[Test it on Gitpod](https://github.com/jlangch/venice/blob/master/doc/readme/start.md#venice-gitpod-workspace)



## Cheat Sheet

Cheat Sheet: [HTML](https://htmlpreview.github.io/?https://cdn.rawgit.com/jlangch/venice/688e6a7/cheatsheet.html) [PDF](https://cdn.rawgit.com/jlangch/venice/688e6a7/cheatsheet.pdf)



## Change Log

[Change Log](ChangeLog.md)



## Documentation

* [Getting started](doc/readme/start.md)
* [REPL](doc/readme/repl.md)
* [On Functional Programming](doc/readme/functional-about.md)
* [First Steps in Venice](doc/readme/functional-first-steps.md)
* [Example: Sudoku Solver](doc/readme/sudoku-solver.md)
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
* [Cryptography](doc/readme/cryptography.md)
* [JSON](doc/readme/json.md)
* [JSON Lines](doc/readme/json-lines.md)
* [CSV](doc/readme/csv.md)
* [PDF](doc/readme/pdf.md)
* [EXCEL](doc/readme/excel.md)
* [Charts](doc/readme/charts.md)
* [Kira Templates](doc/readme/ext-kira.md)
* [ASCII Tables](doc/readme/ascii-tables.md)
* [HTTP Client (Java 8+)](doc/readme/http-client-j8.md)
* [Venice meets LLMs](doc/readme/venice-meets-llms.md)
* [Database (JDBC)](doc/readme/database.md)
* [Shell Scripts](doc/readme/shell-scripts.md)
* [Extension modules](doc/readme/extension-modules.md)
* [Source Code as PDF](doc/readme/source2pdf.md)
* [Tree walker](doc/readme/walk.md)
* [Benchmarks](doc/readme/benchmarks.md)
* [Multi-File Apps](doc/readme/multi-file-app.md)
* [Development Tools](doc/readme/dev-tools.md)
* [Build dependencies](doc/readme/build-dependencies.md)
* [Performance comparison Venice - Clojure - Java](doc/readme/performance.md)



## Getting the latest release

You can can pull it from the central Maven repositories:

```
<dependency>
  <groupId>com.github.jlangch</groupId>
  <artifactId>venice</artifactId>
  <version>1.12.49</version>
</dependency>
```


## Building

From a command shell, run `./gradlew shadowJar` in the project home dir, to invoke the Gradle task to build the Venice JAR.

**On MacOS / Linux**

Give `gradlew` execute permission after cloning the Venice git repository ...

```shell
venice% chmod +x ./gradlew
```

... and build the project

```shell
venice% ./gradlew clean shadowJar
```

**On Windows**

```
C:\Users\foo\venice> gradlew.bat clean shadowJar
```


## Contributing

I accept Pull Requests via GitHub. There are some guidelines which will make applying PRs easier for me:

- No tabs! Please use spaces for indentation.
- Respect the existing code style for each file.
- Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source code should be reformatted create a separate PR for this change.
- Provide JUnit tests for your changes and make sure your changes don't break any existing tests by running gradle.


## License

This code is licensed under the [Apache License v2](LICENSE).


## 3rd Party Open Source

* [Copyright Notice](doc/readme/3rdparty-lic.md)


## Stargazers over time

[![Stargazers over time](https://starchart.cc/jlangch/venice.svg?background=%23FFFFFF&axis=%23989797&line=%230b41d6)](https://starchart.cc/jlangch/venice)

