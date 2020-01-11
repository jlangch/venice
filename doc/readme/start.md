# Getting Started

Welcome to Venice!

Venice can run on any operating system for which a Java VM is available, 
like Linux, MacOSX, or Windows.


### 1. Install Java

Venice requires Java 8 or higher.

Java can be downloaded from 
- [AdoptOpenJDK](https://adoptopenjdk.net/)
- [Zulu](https://www.azul.com/downloads/zulu-community/)


### 2. Get the Venice JAR file

Download Venice from Maven: [Venice](https://search.maven.org/artifact/com.github.jlangch/venice/1.7.14/jar)


### 3. Start a Venice REPL (Read-Eval-Print Loop)

Open a terminal, move to the directory the Venice JAR is located, and start 
a REPL with `java -jar venice-1.7.14.jar -colors`

```text
jupiter> java -jar venice-1.7.14.jar -colors
```

The REPL prompts for input:

```text
jupiter> java -jar venice-1.7.14.jar -colors
Venice REPL: V1.7.14
Loading REPL config from repl.json...
Type '!' for help.
venice>
```

Type an expression like `(+ 1 1)` followed by a `<CR>`

```text
venice> (+ 1 1)
=> 2
venice>
```


### 4. Where to head next

- browse through the [readme](https://github.com/jlangch/venice/blob/master/README.md)
- see the cheatsheet and use it as a quick reference


