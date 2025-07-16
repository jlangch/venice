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


An example (on an MacBook Air M2):

```text
venice> (time (reduce + (map (fn [x] (cond (< x 0) -1 (> x 0) 1 :else 0)) 
                             (range -10000 10001))))
Elapsed time: 110.38ms
=> 0
```
     
```text
venice> !macroexpand
venice> (time (reduce + (map (fn [x] (cond (< x 0) -1 (> x 0) 1 :else 0)) 
                             (range -10000 10001))))
Elapsed time: 12.14ms
=> 0
```

Check if macro expansion is enabled:

```text
venice> (macroexpand-on-load?)
=> true
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

The namespace alias for a module can be completed with a single char default:

```text
venice> (load-module :grep <TAB>
```

auto completes to

```text
venice> (load-module :grep ['grep :as 'g])
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


## Adding 3rdParty JARs to the REPL

3rdParty JARs can be manually copied to the REPL's library path `libs`. 

```text
REPL_HOME
├── libs
│   ├── venice-1.12.50.jar
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

Just restart the REPL after adding the libraries by running the REPL `!restart` 
command:

```text
venice> !restart
```

To check the new REPL classpath run the REPL `!classpath` command:

```text
venice> !classpath
REPL classpath:
  libs
  libs/jansi-2.4.1.jar
  libs/venice-1.12.50.jar
```


## Reload Venice context

Reload the Venice context without restarting the REPL

```text
venice> !reload
```


## Sandbox with the REPL

The Venice sandbox can be managed from within the REPL: [managing the sandbox](repl-sandbox.md)

