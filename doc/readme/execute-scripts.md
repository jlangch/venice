# Executing scripts

Run a script:

```text
foo> java -jar venice-1.4.5.jar -script "(+ 1 1)"
=> 2
```

Load the script from a file and run it:

```text
foo> echo "(+ 1 1)" > script.venice
foo> java -jar venice-1.4.5.jar -file script.venice
=> 2
```

Venice passes the command line args as `*ARGS*` vector:

```text
foo> echo "(+ 1 (long (nth *ARGV* 2)))" > script.venice
foo> java -jar venice-1.4.5.jar -file script.venice 3
=> 4
```

