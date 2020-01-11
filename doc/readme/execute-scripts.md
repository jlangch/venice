# Executing scripts

### Run a script:

```text
foo> java -jar venice-1.7.13.jar -script "(+ 1 1)"
=> 2
```

### Load the script from a file and run it:

File "script.venice":
```text
(+ 1 1)
```

run:
```text
foo> java -jar venice-1.7.13.jar -file script.venice
=> 2
```


### Passing a load-path:

Venice scripts often uses the function `load-file` to load a file with 
Venice functions. `load-file` loads the files by default from the current working 
directory. The command line option "-loadpath" defines a set of semi-colon 
separated paths files are searched for.

File "script.venice":
```text
(load-file "test.venice")
```

run:
```text
foo> java -jar venice-1.7.13.jar -file script.venice -loadpath "/users/foo/venice/scripts"
```

The script loads "test.venice" from "/users/foo/venice/scripts/test.venice".



### Venice passes the command line args as \*ARGV\* vector:

File "script.venice":
```text
(+ 1 (long (nth *ARGV* 2)))
```

run:
```text
foo> java -jar venice-1.7.13.jar -file script.venice 3
=> 4
```

*Note:* the command line args are only available when executing a script 
in the ways shown above. The command line args are not available in the REPL
and for security reasons they are not available with embedding Venice in Java! 

