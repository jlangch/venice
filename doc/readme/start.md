# Getting Started

Welcome to Venice!

Venice can run on any operating system for which a Java VM is available, 
like Linux, MacOSX, or Windows.


## Venice Local Setup

### 1. Install Java

Venice requires Java 8 or higher.

Java can be downloaded from 
- [AdoptOpenJDK](https://adoptopenjdk.net/)
- [Zulu](https://www.azul.com/downloads/zulu-community/)


### 2. Get the Venice JAR file

Download Venice from Maven: [Venice](https://search.maven.org/artifact/com.github.jlangch/venice/1.7.23/jar)


### 3. Start a Venice REPL (Read-Eval-Print Loop)

Open a terminal, move to the directory the Venice JAR is located, and start 
a REPL with `java -jar venice-1.7.23.jar -colors`

```text
foo> java -jar venice-1.7.23.jar -colors
```

The REPL prompts for input:

```text
foo> java -jar venice-1.7.23.jar -colors
Venice REPL: V1.7.23
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


## Venice GitPod workspace 

### Start a REPL from a Venice GitPod workspace

Alternatively a REPL can be run without local installation in a [GitPod](https://gitpod.io/) workspace. If you don't have a GitHub login yet, please sign up for [GitHub](https://github.com/).

Just fire up a Venice GitPod workspace in your browser by clicking this button: 
[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/jlangch/venice). GitPod will ask you to login to GitHub and will start a new Venice workspace.

Wait until the workspace has been built ...

The workspace will start a fresh REPL in a terminal

If you see the Venice REP prompt `venice> ` type an expression like `(+ 100 1)` followed by a `<CR>`

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/gitpod/gitpod-repl.png">


### Manually create a new REPL terminal in the workspace

Start a new terminal (menu Terminal -> New Terminal) ...

launch the REPL within the terminal:

```text
$ java -jar build/libs/venice-*.jar -colors-darkmode
```



## Where to head next

- browse through the [readme](https://github.com/jlangch/venice/blob/master/README.md)
- see the cheatsheet and use it as a quick reference


