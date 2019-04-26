[![](https://github.com/jlangch/venice/blob/master/doc/license.svg)](./LICENSE)
[![](https://github.com/jlangch/venice/blob/master/doc/maven-central.svg)](http://mvnrepository.com/artifact/com.github.jlangch/venice)


# Venice

Venice, a Clojure inspired sandboxed Lisp dialect with Java interoperability serving as 
a safe scripting language.


## Overview

Venice's goal is not to build just another Lisp, it's born from the need of 
having a safe, powerful scripting and expression language that can be used 
to implement scriptable extension points and rules for applications.

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

[Cheat Sheet HTML](https://cdn.rawgit.com/jlangch/venice/a92c38e/cheatsheet.html)

[Cheat Sheet PDF](https://cdn.rawgit.com/jlangch/venice/a92c38e/cheatsheet.pdf)


## REPL

Start the REPL with `java -jar venice-1.4.5.jar -colors`

```text
venice> (+ 1 1)
=> 2
venice>
```

Type `!` from the REPL to get the help. Browse through the history expressions 
with the up/down arrows.

A history of the last three result values is kept by the REPL, accessible through 
the symbols `*1`, `*2`, `*3`, `**`.

The REPL supports multi-line editing:

```text
venice> (defn sum [x y]
      |    (+ x y))
venice> (sum 1 4)
=> 5
venice>
```

If the REPL colors don't harmonize well with your terminal color schema 
omit the '-colors' option or place a 'repl.json' config file with customized 
ANSI escape code colors on the working dir. The REPL command `!config` shows
a sample 'repl.json' that can be modified.



## Documentation

* [Running Venice scripts](doc/readme/execute-scripts.md)
* [Embedding Venice in Java](doc/readme/embedding.md)
* [Datatypes](doc/readme/datatypes.md)
* [Functions](doc/readme/functions.md)
* [Destructuring](doc/readme/destructuring.md)
* [Advanced String Features](doc/readme/advanced-strings.md)
* [Concurrency](doc/readme/concurrency.md)
* [Java Interop](doc/readme/java-interop.md)
* [Exception Handling](doc/readme/exceptions.md)
* [Sandbox](doc/readme/sandbox.md)
* [Extension modules](doc/readme/extension-modules.md)
* [A larger example](doc/readme/large-example.md)




## Build Dependencies


#### Gradle

```groovy
dependencies {
    compile 'com.github.jlangch:venice:1.4.5'
}
```


#### Maven

```xml
<dependencies>
    <dependency>
        <groupId>com.github.jlangch</groupId>
        <artifactId>venice</artifactId>
        <version>1.4.5</version>
    </dependency>
</dependencies>
```


