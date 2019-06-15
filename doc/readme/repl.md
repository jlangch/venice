# REPL

Start the REPL with `java -jar venice-1.5.0.jar -colors`

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


## Display global symbols

```text
venice> !env global
```

Use wildcards to filter global symbols:

```text
venice> !env global io/*
venice> !env global *file*
```


## Function documentation

E.g.: Find the Venice cryptography PBKDF2 hash function and print the doc for it:

```text
venice> (load-module :crypt)
venice> !env global crypt*

venice> (doc crypt/pbkdf2-hash)
```


Print the documentation for a Venice function

```text
venice> (doc zipmap)

venice> (doc +)
```


## Code Completion

The REPL supports code completion. Completion is triggered by the `TAB` key.


### Code completion for functions

```text
venice> (regex<TAB>
regex/find              regex/matcher           regex/find-group
regex/find?             regex/matches           regex/groupcount
regex/group             regex/pattern           regex/find-all-groups
regex/reset             regex/matches?
```

Cycle through the candidates with the `TAB` key or narrow the candidates by 
typing more characters.


### Code completion for loading a module

```text
venice> (load-module <TAB>
:kira     :math     :ring     :maven    :tomcat   :webdav   :xchart
```


### Code completion for loading a Venice file

```text
venice> (load-file <TAB>
chart.venice             exception.venice         perf-test-1.venice
indent.venice            parsatron.venice         perf-test-2.venice
script.venice            chart-swing.venice       login-webapp.venice
webdav.venice            demo-webapp.venice       vaadin-download.venice
```


### Code completion for doc function

```text
venice> (doc li<TAB>
list        list*       list?       list-comp
```


## REPL with 3rdParty jars

To start the REPL with additional 3rdParty .jar files

```text
> java -server -cp "*" com.github.jlangch.venice.Launcher -colors
```

or

```text
> java -server -cp "./libs/*" com.github.jlangch.venice.Launcher -colors
```

## Reload Venice context

Reload the Venice context without restarting the REPL

```text
venice> !reload
```


## Sandbox with the REPL

The Venice sandbox can be managed from within the REPL: [managing the sandbox](repl-sandbox.md)

