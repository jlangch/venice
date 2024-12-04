# Executing scripts

* [Run a script](#run-a-script)
* [Load the script from a file and run it](#load-the-script-from-a-file-and-run-it)
* [Using command line arguments](#using-command-line-arguments)
* [Passing a load-path](#passing-a-load-path)




## Run a script

```text
foo> java -jar venice-1.12.35.jar -script "(+ 1 1)"
=> 2
```


## Load the script from a file and run it

File "script.venice":

```clojure
(do
  (defn circle-area [radius]
    (* math/PI radius radius))
    
  (println (circle-area 2.5)))
```

run:

```text
foo> java -jar venice-1.12.35.jar -file script.venice
=> 19.634954084936208
```



## Using command line arguments

Venice passes the command line args as **ARGV** vector.

File "script.venice":

```clojure
(+ 1 (long (first *ARGV*)))
```

run:

```text
foo> java -jar venice-1.12.35.jar -file script.venice 3
=> 4
```

*Note:* the command line args are only available when executing a script 
in the ways shown above. The command line args are not available in the REPL
and for security reasons they are not available with embedding Venice in Java! 



## Passing a load-path

Venice scripts often uses the function `load-file` to load a file with 
Venice functions. `load-file` loads the files by default from the current working 
directory. The command line option "-loadpath" defines a set of semi-colon 
separated paths files are searched for.

File "/users/foo/venice/test.venice":

```clojure
(do
  (defn circle-area [radius]
    (* math/PI radius radius)))
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
├── libs
│   └── venice-1.12.35.jar
└── scripts
    └── test.venice
```

Run:

```text
foo> cd /users/foo/venice
foo> java -jar libs/venice-1.12.35.jar -file script.venice -loadpath "/users/foo/venice/scripts"
```

The script loads "test.venice" from "/users/foo/venice/scripts/test.venice".


