# Executing scripts


### Run a script

```text
foo> java -jar venice-1.10.0.jar -script "(+ 1 1)"
=> 2
```


### Load the script from a file and run it

File "script.venice":

```clojure
(do
  (def PI (. :java.lang.Math :PI))
  
  (defn circle-area [radius]
    (* PI radius radius))
    
  (println (circle-area 2.5)))
```

run:

```text
foo> java -jar venice-1.10.0.jar -file script.venice
=> 19.634954084936208
```



### Using command line arguments

Venice passes the command line args as **ARGV** vector.

File "script.venice":

```clojure
(+ 1 (long (nth *ARGV* 2)))
```

run:

```text
foo> java -jar venice-1.10.0.jar -file script.venice 3
=> 4
```

*Note:* the command line args are only available when executing a script 
in the ways shown above. The command line args are not available in the REPL
and for security reasons they are not available with embedding Venice in Java! 



### Passing a load-path

Venice scripts often uses the function `load-file` to load a file with 
Venice functions. `load-file` loads the files by default from the current working 
directory. The command line option "-loadpath" defines a set of semi-colon 
separated paths files are searched for.

File "/users/foo/venice/test.venice":

```clojure
(do
  (def PI (. :java.lang.Math :PI))
  
  (defn circle-area [radius]
    (* PI radius radius)))
```

File "/users/foo/venice/script.venice":

```text
(do
  (load-file "test.venice")

  (println (circle-area 2.5))
```

Files:

```text
/users/foo/venice
├── script.venice
├── bin
│   └── venice-1.10.0.jar
└── scripts
    └── test.venice
```

Run:

```text
foo> cd /users/foo/venice
foo> java -jar bin/venice-1.10.0.jar -file script.venice -loadpath "/users/foo/venice/scripts"
```

The script loads "test.venice" from "/users/foo/venice/scripts/test.venice".


## Macro Expansion

Expanding macros ahead of evaluation can speed up the execution by 
a factor of 3 to 10.

Upfront macro expansion can be activated with the command line option `-macroexpand` 
and works for executing scripts or files.


**Example**

Run a script without upfront macro expansion:

```text
foo> java -jar venice-1.10.0.jar -script "(time (reduce + (map (fn [x] (cond (< x 0) -1 (> x 0) 1 :else 0)) (range -10000 10001))))"
Elapsed time: 271,66 ms
=> 0
```
 
Run a scrip with upfront macro expansion:
    
```text
foo> java -jar venice-1.10.0.jar -macroexpand -script "(time (reduce + (map (fn [x] (cond (< x 0) -1 (> x 0) 1 :else 0)) (range -10000 10001))))"
Elapsed time: 26,35 ms
=> 0
```
