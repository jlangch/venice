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

Manually download the JAR from Maven: [Venice JAR](https://search.maven.org/artifact/com.github.jlangch/venice/1.12.19/jar)

or download it with `curl`:

```
curl "https://repo1.maven.org/maven2/com/github/jlangch/venice/1.12.19/venice-1.12.19.jar" --output ./venice-1.12.19.jar
```


### 3. Setup the Venice REPL

This automated setup will create a REPL launcher script with the Java *classpath*
correctly setup, will download the JAnsi library from Maven, and add some example 
Venice scripts. The setups works on MacOS, Linux, and Windows.

Open a terminal, move to the directory the Venice JAR is located, and start 
the REPL in setup mode:

```text
foo> java -jar venice-1.12.19.jar -setup -colors
```

For a REPL run in a darkmode terminal use the option `-colors-darkmode` instead 
of `-colors`. This can also be changed later on by modifying the generated launcher 
shell script.

The `-setup` option will cause the Venice setup to:
  - create a `libs`, `tmp`, `tools`, and `scripts` directory
  - download the Jansi library from the Maven repository
  - install Maven locally to the REPL in the `tools` dir
  - extract some example Venice scripts
  - create a launcher shell script `repl.bat` for Windows and `repl.sh` for all other OS
 

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/repl/repl-setup.png" width="500">

 
Now the Venice REPL can be started simply by executing the launcher.

on MacOSX and Linux:

```text
foo> ./repl.sh
```

on Windows:

```text
foo> repl.bat
```


The REPL is started and prompts for input:

```text
Venice REPL: V1.12.19
Java: 1.8.0_392
Loading configuration from file 'repl.json'
Using Ansi terminal (light color mode turned on)
Use the commands !lightmode or !darkmode to adapt to the terminal's colors
Type '!' for help.
venice>
```

### 4. Run expressions in the REPL

Type an expression like `(+ 1 1)` followed by a `<CR>`

```text
venice> (+ 1 1)
=> 2
venice>
```



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


