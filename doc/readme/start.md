# Getting Started

Welcome to Venice!

Venice can run on any operating system for which a Java VM is available, 
like Linux, MacOSX, or Windows.

Setup Venice on a [local machine](#venice-local-setup) or use a [Venice Gitpod workspace](#venice-gitpod-workspace) without requiring any local installation.
 


## Venice Local Setup

### 1. Install Java

Venice requires Java 8 or newer.

Java can be downloaded from: 
- [Eclipse Temurin](https://adoptium.net/de/temurin/releases/)
- [Zulu](https://www.azul.com/downloads/zulu-community/)

The Venice JAR published to Maven is compiled with Java 8. Nevertheless Venice is regulary compiled and unit tested with Java 8, 11, and 17.


### 2. Get the Venice JAR file

Download Venice from Maven: [Venice](https://search.maven.org/artifact/com.github.jlangch/venice/1.12.13/jar)


### 3. Start a Venice REPL (Read-Eval-Print Loop)

#### 3.1 Linux and MacOSX

Open a terminal, move to the directory the Venice JAR is located, and start 
a REPL:

```text
foo> java -jar venice-1.12.13.jar -colors
```

The REPL prompts for input:

```text
Venice REPL: V1.12.13
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


#### 3.2 Windows

On Windows the 'jansi' 2.4.1 library is required to run the REPL. Download the library from
Maven: [jansi](https://search.maven.org/artifact/org.fusesource.jansi/jansi/2.4.1/jar)

Open a terminal, move to the directory the Venice and the 'jansi' JAR is located, 
and start a REPL:

```text
foo> java -cp venice-1.12.13.jar;jansi-2.4.1.jar com.github.jlangch.venice.Launcher -colors-darkmode
```

The REPL prompts for input:

```text
Venice REPL: V1.12.13
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

#### 3.3 Automated setup for all operating systems

This is by far the easiest way to setup a local Venice REPL environment 
regardless of the operating system (Windows, MacOSX, or Linux).

You just need a Java JRE/JDK and the Venice JAR as prerequisite.

The automated setup will create a REPL launcher script with the Java classpath 
correctly setup, will download the Jansi library from Maven, and add some example 
Venice scripts.

Open a terminal, move to the directory the Venice JAR is located, and start 
the REPL in setup mode:

```text
foo> java -jar venice-1.12.13.jar -setup -colors
```

For a REPL run in a darkmode terminal use the option `-colors-darkmode` instead 
of `-colors`. This can also be changed later on by modifying the generated launcher 
shell script.

The `-setup` option will cause the Venice setup to:
  - create a `libs`, `tmp`, and `scripts` directory
  - download the Jansi library from the Maven repository
  - extract some example Venice scripts
  - create a launcher shell script `repl.bat` for Windows and `repl.sh` for all other OS
  
Now the Venice REPL can be started simply by executing the launcher.

on MacOSX and Linux:

```text
foo> ./repl.sh
```

on Windows:

```text
foo> repl.bat
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/repl/repl-setup.png" width="500">


## Venice Gitpod workspace 

### Start a REPL from a Venice Gitpod workspace

A Venice REPL can be run without local installation in a [Gitpod](https://gitpod.io/) workspace. If you don't have a GitHub login yet, please sign up for [GitHub](https://github.com/).

Just fire up a Venice Gitpod workspace in your browser by opening the URL https://gitpod.io/#https://github.com/jlangch/venice. Gitpod will ask you to login to GitHub and will launch a Venice workspace container for you in the cloud, containing a full Linux environment. It will also clone the Venice repository and build it.

Wait until the workspace has been built ...

The workspace will start a fresh REPL in a terminal.

If you see the Venice REPL prompt `venice> ` type an expression like `(+ 1 100)` followed by a `<CR>`

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/gitpod/gitpod-repl.png">


### Manually start a new REPL terminal in the workspace

Start a new terminal (menu Terminal -> New Terminal) ...

launch the REPL within the terminal:

```text
$ cd /workspace/repl
$ ./repl.sh
```


### Useful VSCode extensions

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/gitpod/gitpod-VsCodeExtensions.png" width="300">


### VSCode settings.json

```json
{
    "files.associations": {
        "*.venice": "clojure"
    },
    
    "workbench.colorTheme": "Tomorrow Night Blue",
    "redhat.telemetry.enabled": false    
}
```


## Where to head next

- browse through the [readme](https://github.com/jlangch/venice/blob/master/README.md)
- see the cheatsheet and use it as a quick reference


