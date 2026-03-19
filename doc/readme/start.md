# Getting Started

Welcome to Venice!

Venice can run on any operating system for which a Java VM is available, 
like Linux, MacOS, or Windows.

 
 

## Venice Setup

 

### 1. Install Java

Venice requires Java 8 or newer.

Java can be downloaded from: 
- [Eclipse Temurin](https://adoptium.net/de/temurin/releases/)
- [Zulu](https://www.azul.com/downloads/zulu-community/)

The Venice JAR published to Maven is compiled with Java 8. Nevertheless Venice is regulary compiled and unit tested with Java 8, 17, and 21.

 

### 2. Get the Venice JAR file

Manually download the JAR from Maven: [Venice JAR](https://search.maven.org/artifact/com.github.jlangch/venice/1.12.85/jar)

or download it with `curl`:

```
curl -O https://repo1.maven.org/maven2/com/github/jlangch/venice/1.12.85/venice-1.12.85.jar
```

 

### 3. Setup the Venice REPL

This automated setup will create a REPL launcher script with the Java *classpath*
correctly setup, will download the *Jansi* library from Maven, and add some example 
Venice scripts. The setups works on MacOS, Linux, and Windows.

Open a terminal, move to the directory the Venice JAR is located, and start 
the REPL in setup:

``` text
java -jar venice-1.12.85.jar -setup -colors -dir ./repl
```

For a REPL run in a darkmode terminal use the option `-colors-dark` instead of
`-colors`. This can also be changed later on by modifying the generated REPL 
launcher shell script `repl.bat` or `repl.sh` respectively.

The `-setup` option will cause the Venice setup to:
  - create the setup dir given by the option `-dir ./repl` if it does not exist
  - create a `libs`, `tmp`, `tools`, and `scripts` directory in the setup dir
  - install Maven locally to the REPL in the `tools` dir. Maven is used to download (if required so), 
    additional 3rdParty packages with its dependencies. It's only used when you need
    additional packages like the Apache-POI libraries for dealing with Excel files.
  - download the Jansi library from the Maven repository to the `libs` dir
  - extract some example Venice scripts to the `scripts` dir
  - create a launcher shell script `repl.bat` for Windows and `repl.sh` for all other OS
 

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/repl/repl-setup.png" width="500">

 

#### REPL setup directory structure (on MacOS / Linux)

``` text
REPL_HOME
├── libs
│   ├── venice-1.12.85.jar
│   ├── jansi-2.4.1.jar
│   └── repl.json
├── tools
│   └── apache-maven-3.9.6
│       └── ...
├── tmp
│   └── ...
├── scripts
│   └── ... (example scripts)
├── repl.env
├── repl.sh
└── run-script.sh
```

#### REPL setup directory structure (on Windows)

``` text
REPL_HOME
├── libs
│   ├── venice-1.12.85.jar
│   ├── jansi-2.4.1.jar
│   └── repl.json
├── tools
│   └── apache-maven-3.9.6
│       └── ...
├── tmp
│   └── ...
├── scripts
│   └── ... (example scripts)
├── repl.env.bat
└── repl.bat
```


### 3b. Setup a minimal REPL

To setup a minimal REPL add the option `-minimal`:

``` text
java -jar venice-1.12.85.jar -setup -minimal -colors -dir ./repl
```

This will setup a fully functional REPL but omits the demo scripts and the local Maven.


``` text
REPL_HOME
├── libs
│   ├── venice-1.12.85.jar
│   ├── jansi-2.4.1.jar
│   └── repl.json
├── tmp
├── repl.env
└── repl.sh
```

 

### 4. Start the REPL

The Venice REPL can be started simply by executing the REPL launcher.

Open a terminal and run the REPL start script.

| Linux, MacOS ¹⁾       |  Windows ²⁾              |
| :-                    | :-                       |
| `/path-to-repl/repl.sh` | `C:\path-to-repl\repl.bat` |

¹⁾ Alternatively double-click on `/path-to-repl/repl.command` in the *MacOS Finder*

²⁾ Alternatively double-click on `/path-to-repl/repl.bat` in the *File Explorer* 

 

The REPL is started and prompts for input:

``` text
Venice REPL: 1.12.85
Home: /Users/juerg/Desktop/venice
Java: 1.8.0_462
Jansi: 2.4.1
Macro expansion: disabled
Configuration: file 'repl.json'
Using Ansi terminal (light color mode turned on)
Use the commands !lightmode or !darkmode to adapt to the terminal's colors
Type '!' for help.
venice>
```

 

### 5. Run expressions in the REPL

Type an expression like `(+ 1 1)` followed by a `<CR>`

``` text
venice> (+ 1 1)
=> 2
venice>
```

 
 

### VSCodium 

[VSCodium](https://vscodium.com/) is a community-driven, freely-licensed binary distribution of Microsoft’s editor VS Code.

**Install VSCodium on MacOS**

If you are on a Mac and have [Homebrew](https://brew.sh/) installed:

```
brew install --cask vscodium
```

or install the VSCodium *.dmg* package from [VSCodium Install](https://vscodium.com/#install)

**Syntax Highlighting**

Edit the VSCodium **settings.json** file to establish syntax highlighting for Venice source files

``` json
{
    "files.associations": {
        "*.venice": "clojure"
    },
    
    "workbench.colorTheme": "Tomorrow Night Blue",
    "redhat.telemetry.enabled": false
}
```

 
 

### VSCode 

If you use VSCode to edit Venice files, this VSCode **settings.json** file will establish syntax highlighting for Venice source files

``` json
{
    "files.associations": {
        "*.venice": "clojure"
    },
    
    "workbench.colorTheme": "Tomorrow Night Blue",
    "redhat.telemetry.enabled": false
}
```

 
 

## Where to head next

- working with a [REPL](https://github.com/jlangch/venice/blob/master/doc/readme/repl.md)
- [Venice first steps](https://github.com/jlangch/venice/blob/master/doc/readme/functional-first-steps.md)
- browse through the [readme](https://github.com/jlangch/venice/blob/master/README.md#documentation)
- see the [cheatsheet](https://cdn.rawgit.com/jlangch/venice/fb56310/cheatsheet.pdf) and use it as a quick reference


