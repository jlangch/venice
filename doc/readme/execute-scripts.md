# Executing scripts

### Run a script:

```text
foo> java -jar venice-1.6.1.jar -script "(+ 1 1)"
=> 2
```

### Load the script from a file and run it:

```text
foo> echo "(+ 1 1)" > script.venice
foo> java -jar venice-1.6.1.jar -file script.venice
=> 2
```

### Venice passes the command line args as \*ARGV\* vector:

```text
foo> echo "(+ 1 (long (nth *ARGV* 2)))" > script.venice
foo> java -jar venice-1.6.1.jar -file script.venice 3
=> 4
```

*Note:* the command line args are only available when executing a script 
in the ways shown above. The command line args are not available in the REPL
and for security reasons they are not available with embedding Venice in Java! 

