# REPL

A Venice REPL (Read-Eval-Print Loop) is a programming environment which enables 
you to run and test code interactively.


## Start a REPL

Install Venice and the REPL following the [Getting started](start.md)
guide.

From the directory Venice has been installed to, run a REPL by executing the REPL
start script:

on MacOSX and Linux:

```text
foo> ./repl.sh
```

on Windows:

```text
foo> repl.bat
```

### Run expressions in the REPL

Type an expression like `(+ 1 1)` followed by a `<CR>`

```text
venice> (+ 1 1)
=> 2
venice>
```

The REPL supports multi-line editing:

```text
venice> (defn sum [x y]
           (+ x y))
venice> (sum 1 4)
=> 5
venice>
```

### REPL commands

Type `!` from the REPL to get the help. Browse through the history expressions 
with the up/down arrows.

A history of the last three result values is kept by the REPL, accessible through 
the symbols `*1`, `*2`, `*3`, `**`.

If the REPL colors don't harmonize well with your terminal color schema 
omit the '-colors' option or place a 'repl.json' config file with customized 
ANSI escape code colors on the working dir. The REPL command `!config` shows
a sample 'repl.json' that can be modified.



## Built-in documentation

The documentation for a function can be printed from the REPL:

```text
venice> (doc map)

venice> (doc +)
```


To learn more about the REPL see [Advanced REPL](repl-advanced.md)
