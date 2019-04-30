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


## REPL with 3rdParty jars

To start the REPL with additional 3rdParty .jar files

```text
> java -server -cp "*" com.github.jlangch.venice.Launcher -colors
```

or

```text
> java -server -cp "./libs/*" com.github.jlangch.venice.Launcher -colors
```

## Reload Venice context

Reload the Venice context without restarting the REPL

```text
venice> !reload
```


## Display global symbols

```text
venice> !env global
```


## Sandbox with the REPL

The Venice sandbox can be managed from within the REPL: [managing the sandbox](repl-sandbox.md)

