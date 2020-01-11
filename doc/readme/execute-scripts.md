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

File "script.venice":
```text
(+ 1 1)
```

run:
```text
foo> java -jar venice-1.7.13.jar -file script.venice -loadpath "/users/foo/venice/scripts"
=> 2
```

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

