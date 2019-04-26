# REPL

Start the REPL with `java -jar venice-1.4.5.jar -colors`

```text
venice> (+ 1 1)
=> 2
venice>
```

Type `!` from the REPL to get the help. Browse through the history expressions 
with the up/down arrows.

A history of the last three result values is kept by the REPL, accessible through 
the symbols `*1`, `*2`, `*3`, `**`.

The REPL supports multi-line editing:

```text
venice> (defn sum [x y]
      |    (+ x y))
venice> (sum 1 4)
=> 5
venice>
```

If the REPL colors don't harmonize well with your terminal color schema 
omit the '-colors' option or place a 'repl.json' config file with customized 
ANSI escape code colors on the working dir. The REPL command `!config` shows
a sample 'repl.json' that can be modified.


## Reload Venice context

Reload the Venice context without restarting the REPL

```text
venice> !reload
```


Sometimes it is necessary to reload an extension module

```text
venice> (load-module :chart)
venice> ... do something ...
venice> (load-module :chart)
venice> ... do something ...
```
This can result in an error message

`The existing global var 'xchart/doto-cond' must not be overwritten!`

Reloading the Venice context solves the problem without restarting the REPL:

```text
venice> (load-module :chart)
venice> !reload
venice> (load-module :chart)
```


## Display global symbols

```text
venice> !env global
```


## Sandbox with the REPL

The Venice sandbox can be managed from within the REPL: [managing the sandbox](repl-sandbox.md)
