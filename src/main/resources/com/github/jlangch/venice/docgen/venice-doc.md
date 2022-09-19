# VeniceDoc

¶

**VeniceDoc** is a documentation generator for the *Venice* language for 
generating API documentation in HTML format from *Venice* source code.

It is used internally for generating the PDF and HTML cheatsheets. The 
function `doc` makes use of it to display the documentation for functions.
 
¶
¶

## Example

Define a function `add` with documentation:

```venice
(defn
  ^{ :arglists '(
        "(add)", "(add x)", "(add x y)", "(add x y & more)")
     :doc 
        """
        Returns the sum of the numbers.
        `(add)` returns 0.
        """
     :examples '(
        "(add)",
        "(add 1)",
        "(add 1 2)",
        "(add 1 2 3 4)")
     :see-also '(
        "+", "-", "*", "/") }

  add

  ([] 0)
  ([x] x)
  ([x y] (+ x y))
  ([x y & xs] (+ x y xs)))
```

¶
 
Show its documentation from the REPL:
 
```text
venice> (doc add)
```

REPL Output:

```text
(add), (add x), (add x y), (add x y & more)

Returns the sum of the numbers. (add) returns 0.

EXAMPLES:
   (add)

   (add 1)

   (add 1 2)

   (add 1 2 3 4)

SEE ALSO:
   +, -, *, /
```

¶
¶

## VeniceDoc Format

The documentation is defined as a Venice metadata `map`:

```venice
  { :arglists '("(add)", "(add x)")
    :doc "Returns the sum of the numbers."
    :examples '("(add 1)", "(add 1 2)")
    :see-also '("+", "-", "*", "/") }
```

¶

| key       | description                                              |
| [![text-align: left; width: 15%]] | [![text-align: left]]            |
| :arglist  | the optional arglist, a list of variadic arg specs       |
| :doc      | the documentation in [Venice markdown](#markdown) format |
| :examples | optional examples, a list of Venice scripts. ¶ Use triple quotes for multi-line scripts |
| :see-also | an optional list of cross referenced functions           |


