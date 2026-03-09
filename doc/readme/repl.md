# REPL


A Venice REPL (Read-Eval-Print Loop), is an interactive programming environment 
that takes user inputs (lines of code), executes them, and returns the result 
to the user. This process allows for real-time feedback and is great for learning, 
testing code snippets, and debugging.

By default, the Venic REPL starts in the *user* namespace and this namespace is 
typically used for exploratory work.

 
 

## Installing Venice and the REPL

Install Venice and the REPL following the [Getting started](start.md) guide.

 
 

## Start a REPL

Open a terminal and run the REPL start script.

| MacOSX ¹⁾             | Linux                 |  Windows ²⁾              |
| :-                    | :-                    | :-                       |
| `/path-to-repl/repl.sh` | `/path-to-repl/repl.sh` | `C:\path-to-repl\repl.bat` |

¹⁾ Alternatively double-click on `/path-to-repl/repl.command` in the *MacOSX Finder*

²⁾ Alternatively double-click on `/path-to-repl/repl.bat` in the *File Explorer* 


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

Check the current namespace

```text
venice> *ns*
=> user
venice>
```

Change to the *demo* namespace

```text
venice> (ns demo)
=> demo
venice>
```


### REPL commands

Type `!` from the REPL to get the help. Browse through the history expressions 
with the up/down arrows.

A history of the last three result values is kept by the REPL, accessible through 
the Venice symbols `*1`, `*2`, `*3`, `**`.

```text
venice> (+ 10 1)
=> 11
venice> (* 5 4)
=> 20
venice> (+ *1 *2)
=> 31
venice> 
```

If the REPL colors don't harmonize well with your terminal color schema 
omit the '-colors' option or place a 'repl.json' config file with customized 
ANSI escape code colors in the repl home dir. The REPL command `!config` shows
a sample 'repl.json' that can be modified.

 
 

## Built-in documentation

The documentation for a function can be printed from the REPL:

**Example 1**

```text
venice> (doc map)
```

doc output:

```text
(map f coll colls*)

Applys f to the set of first items of each coll, followed by applying f to the
set of second items in each coll, until any one of the colls is exhausted. Any
remaining items in other colls are ignored.
Returns a transducer when no collection is provided.
Note: if Java collections are used the mapper converts all mapped items back to
Java data types to keep Java compatibilty as much as possible! To avoid this
just convert the Java collection to a Venice collection. E.g.: (into [] ...).

EXAMPLES:
   (map inc [1 2 3 4])

   (map + [1 2 3 4] [10 20 30 40])

   (map list '(1 2 3 4) '(10 20 30 40))

   (map vector (lazy-seq 1 inc) [10 20 30 40])

   (map (fn [[k v]] [k v]) {:a 1 :b 2})

   (map (fn [e] [(key e) (inc (val e))]) {:a 1 :b 2})

   (map inc #{1 2 3})

   ;; Venice enforces Java types when using java collections instead 
   ;; of Venice collections!
   ;; -> The returned element type is a 'java.util.ArrayList'
   ;;    and not a 'core/vector'
   (->> (doto (. :java.util.ArrayList :new) (. :add 1) (. :add 2)) 
        (map (fn [x] [(inc x)]))  ;; map to a 'core/vector'
        (first)
        (type))

   ;; Same example with a Venice collection!
   ;; -> The returned element type is a 'core/vector'
   (->> [1 2] 
        (map (fn [x] [(inc x)]))  ;; map to a 'core/vector'
        (first)
        (type)) 

SEE ALSO:
   filter, reduce, map-indexed
```

**Example 2**

```text
venice> (doc +)
```

doc output:

```text
(+), (+ x), (+ x y), (+ x y & more)

Returns the sum of the numbers. (+) returns 0.

EXAMPLES:
   (+)

   (+ 1)

   (+ 1 2)

   (+ 1 2 3 4)

   (+ 1I 2I)

   (+ 1 2.5)

   (+ 1 2.5M)

SEE ALSO:
   -, *, /, dec/add, dec/sub, dec/mul, dec/div, dec/scale
```


 
 

To learn more about the REPL see [Advanced REPL](repl-advanced.md)
