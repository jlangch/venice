# Advanced REPL 


## Macro Expansion

Expanding macros ahead of evaluation can speed up the execution of a script by 
a factor of 3 to 10.

Upfront macro expansion can be activated in the REPL by the `!macroexpand` command:

```text
venice> !macroexpand
```

The upfront macro expansion is applied to typed scripts and files loaded from 
filesystem or classpath.


An example:

```text
venice> (time (reduce + (map (fn [x] (cond (< x 0) -1 (> x 0) 1 :else 0)) 
                             (range -10000 10001))))
Elapsed time: 271,66 ms
=> 0
```
     
```text
venice> !macroexpand
venice> (time (reduce + (map (fn [x] (cond (< x 0) -1 (> x 0) 1 :else 0)) 
                             (range -10000 10001))))
Elapsed time: 26,35 ms
=> 0
```


## Drag and Drop Venice files into the REPL for execution

Drag and drop a Venice file into the REPL and press [RETURN] to execute it:

```text
venice> /Users/foo/test.venice
```

This is identical to run `(load-file "/Users/foo/test.venice")`.



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

Print the documentation for a Venice function

```text
venice> (doc filter)

venice> (doc +)
```


E.g.: Find the Venice cryptography PBKDF2 hash function and print the doc for it:

```text
venice> (load-module :crypt)
venice> !env global crypt*

venice> (doc crypt/pbkdf2-hash)
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

Start a Java VM with 2GB of memory, disable using preallocated exceptions (stack traces are always generated)

```text
> java \
    -server \
    -XX:-OmitStackTraceInFastThrow \
    -Xmx2G \
    -cp "libs/*" \
    com.github.jlangch.venice.Launcher \
    -colors
```

## Reload Venice context

Reload the Venice context without restarting the REPL

```text
venice> !reload
```


## Using a load path for files

Venice code often uses the function `load-file` to load a file with 
Venice functions. `load-file` loads the files by default from the current working 
directory. The REPL accepts the command line option "-loadpath" that defines a 
set of semi-colon separated paths files are searched for.

The `load-file` file completion honors the load path.

REPL Launcher with "-loadpath" option:

```text
> java \
    -server \
    -XX:-OmitStackTraceInFastThrow \
    -Xmx2G \
    -cp "libs:libs/*" \
    com.github.jlangch.venice.Launcher \
    -loadpath "/users/foo/venice/scripts1;/users/foo/venice/scripts2" \
    -colors
```


## Sandbox with the REPL

The Venice sandbox can be managed from within the REPL: [managing the sandbox](repl-sandbox.md)

