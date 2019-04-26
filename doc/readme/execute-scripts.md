# Executing scripts

```text
foo> java -jar venice-1.4.5.jar -script "(+ 1 1)"
=> 2
```

```text
foo> echo "(+ 1 1)" > script.venice
foo> java -jar venice-1.4.5.jar -file script.venice
=> 2
```
