A Guide to Parsifal
====================


The original Clojure Parsatron guide can be found at [A Guide to the Parsatron](https://github.com/sjl/parsatron/blob/docs/docs/guide.markdown)

This guide is adapted to the  _Parsifal_  Venice port of  _Parsatron_ .


_Parsifal_  is a library for building parsers for languages. For an overview of
how it works internally you can watch [this talk][talk] from Nate Young on his 
Clojure Parsatron implementation.

This document will show you the basics of how to use _Parsifal_ as an end user.

[talk]: http://www.infoq.com/presentations/Parser-Combinators

Parsifal added the following changes to the original Parsatron parser combinator:
- Added protocol 'SourcePosition' and changed function `inc-sourcepos` to support error message for source line/column nr if item types other than char are used.
- Deduping the error message list, to avoid repeated error messages
- Enhanced the `never` parser to accept an optional customized error message
- Changed the behaviour of the built-in `string`, `between`, and `times` parser to not consume any input upon failure to follow the principle of least surprise.
- Added an `any` parser to consume a token with any value
- Added a `hexdigit` parser to consume hexdigit chars


Importing
---------

You can grab all the things you'll need by loading the `parsifal` module:

```clojure
(load-module :parsifal)
(ns-alias 'p 'parsifal)
```

Running
-------

Let's see how to run a basic parser.  It won't do much, but it will get
something on the screen so we can try things as we go.  Assuming you've got
everything imported:

    (p/run (p/char #\H) "Hello, world!")
    ; \H

The `run` function takes a parser and some input, runs the parser on that
input, and returns the result.

The parser we passed here was `(p/char #\H)`.  We'll talk more about parsers in a
second, but for now just know that it's a parser that will parse a single "H"
character.

Notice that it only parsed the first character, and even though there was more
left it still successfully returned.  We'll talk about how to make sure that
there's no remaining input later.

Input
-----

We passed a string as the input to `run` in our first example, but the input
doesn't necessarily have to be a string.  It can be any sequence.  For example,
this works:

    (p/run (p/token #{1 2}) [1 "cats" :dogs])
    ; 1

The `(p/token #{1 2})` is a parser that matches the *integer* 1 or the *integer*
2, and we've passed it a vector of things.

Errors
------

If the parser you give to `run` can't parse the input successfully, a
ParseError will be thrown:

    (p/run (p/char #\q) "Hello, world!")
    ; ParseError Unexpected token 'H' at line: 1 column: 1 ...

The exception will tell you the line and column of the error, which is usually
quite helpful.

Parsers
-------

Now that we've got the basics, it's time to talk about how to create new
parsers.

A "parser" is, technically, a function that takes 5 arguments and returns
a special value, but you don't need to worry about that yet.  What you *do* need
to worry about is how to create them and combine them.

When we ran `(char #\H)` in the first example, it returned a parser.  `char`
itself is a *function* that, when given a character, creates a parser that
parses that character.

Read that again and make sure you understand it before moving on.  `char` is
not a parser.  It's a function that creates parsers.  Character goes in, parser
comes out:

    (def h-parser (p/char #\h))
    (p/run h-parser "hi")
    ; #\h

Basic Built-In Parsers
----------------------

There are a few other basic parser-creating functions that you'll probably find
useful, which we'll talk about now.

### token

`token` creates parsers that match single items from the input stream (which
are characters if the input stream happens to be a string).  You give it a
predicate, and it returns a parser that parses and returns items that match the
predicate.  For example:

    (defn less-than-five [i]
      (< i 5))

    (p/run (p/token less-than-five)
         [3])
    ; 3

The predicate can be any function, so things like anonymous functions and sets
work well.

### char

We've already seen `char`, which creates parsers that parse and return a
single, specific character.

    (p/run (p/char #\H) "Hello, world!")
    ; #\H

### any-char

`any-char` creates parsers that will parse and return any character.  Remember
that we can use _Parsifal_ to parse more than just strings:

    (p/run (p/any-char) "Cats")
    ; #\C

    (p/run (p/any-char) [#\C #\a #\t #\s])
    ; #\C

    (p/run (p/any-char) [1 2 3])
    ; ParseError...

### letter and digit

`letter` and `digits` create parsers that parse and return letter characters
(a-z and A-Z) and digit characters (0-9) respectively.

    (p/run (p/letter) "Dogs")
    ; #\D

    (p/run (p/digit) "100")
    ; #\1

Note that digit works with *character* objects.  It won't work with actual
integers:

    (p/run (p/digit) [10 20 30])
    ; ParseError...

If you want a parser that matches numbers in a non-string input sequence, use
`token` and the Venice builtin function `number?` to make it:

    (p/run (p/token number?) [10 20 30])
    ; 10

### string

`string` creates parsers that parse and return a sequence of characters given
as a string:

    (p/run (p/string "Hello") "Hello, world!")
    ; "Hello"

Note that this is the first time we've seen a parser that consumes more than
one item in the input sequence.

### eof

`p/eof` creates parsers that ensure the input stream doesn't contain anything else:

    (p/run (p/eof) "")
    ; nil

    (p/run (p/eof) "a")
    ; ParseError...

On its own it's not very useful, but we'll need it once we learn how to combine
parsers.

Combining Parsers
-----------------

_Parsifal_ wouldn't be very useful if we could only ever parse one thing at
a time.  There are a number of ways you can combine parsers to build up complex
ones from basic parts.

### >>

The `>>` macro is the simplest way to combine parsers.  It takes any number of
parsers and creates a new parser.  This new parser runs them in order and
returns the value of the last one.

Again, `>>` takes *parsers* and returns a new *parser*.  We'll see this many
times in this section.

Here's an example:

    (def my-parser (p/>> (p/char #\a)
                         (p/digit)))

    (p/run my-parser "a5")
    ; #\5

    (p/run my-parser "5a")
    ; ParseError...

    (p/run my-parser "b5")
    ; ParseError...

    (p/run my-parser "aq")
    ; ParseError...

We create a parser from two other parsers with `>>` and run it on some input.
`>>` runs its constituent parsers in order, and they all have to match for it
to parse successfully.

Now that we can combine parsers, we can also ensure that there's no garbage
after the stuff we parse by using `eof`:

    (p/run (p/>> (p/digit) (p/eof)) "1")
    ; nil

    (p/run (p/>> (p/digit) (p/eof)) "1 cat")
    ; ParseError...

### times

The next way to combine parsers (or, really, a parser with itself) is the
`times` function.

`times` is a function that takes a count and a parser, and returns a parser that
repeats the one you gave it the specified number of times and returns the
results concatenated into a sequence.

For example:

    (p/run (p/times 5 (p/letter)) "Hello, world!")
    ; (#\H #\e #\l #\l #\o)

This is different than `(p/>> (p/letter) (p/letter) (p/letter) (p/letter) (p/letter))`
because it returns *all* of the parsers' results, not just the last one.

### many

`many` is the first creator of "open-ended" parsers we've seen.  It's a function
that takes a parser and returns a new parser that will parse zero or more of the
one you gave it, and return the results concatenated into a sequence.

For example:

    (p/run (p/many (p/digit)) "100 cats")
    ; (#\1 #\0 #\0)

Now we can start to build much more powerful parsers:

    (def number-parser (p/many (p/digit)))
    (def whitespace-parser (p/many (p/token #{#\space #\newline #\tab})))

    (p/run (p/>> number-parser whitespace-parser number-parser) "100    400")
    ; (#\4 #\0 #\0)

We still need to talk about how to get more than just the last return value, but
that will come later.

### many1

`many1` is just like `many`, except that the parsers it creates require at least
one item.  It's like `+` in a regular expression instead of `*`.

    (def number-parser (p/many (p/digit)))
    (def number-parser1 (p/many1 (p/digit)))

    (p/run number-parser "")
    ; []

    (p/run number-parser "100")
    ; (#\1 #\0 #\0)

    (p/run number-parser1 "")
    ; ParseError...

    (p/run number-parser1 "100")
    ; (#\1 #\0 #\0)

### choice

`choice` takes one or more parsers and creates a parser that will try each of
them in order until one parses successfully, and return its result.  For example:

    (def number (p/many1 (p/digit)))
    (def word (p/many1 (p/letter)))

    (def number-or-word (p/choice number word))

    (p/run number-or-word "dog")
    ; (#\d #\o #\g)

    (p/run number-or-word "42")
    ; (#\4 #\2)

Notice that we used `many1` when defining the parsers `number` and `word`.  If
we had used `many` then this would always parse as a number because if there
were no digits it would successfully return an empty sequence.

### between

`between` is a function that takes three parsers, call them left, right, and
center.  It creates a parser that parses them in left - center - right order and
returns the result of center.

This is a convenient way to handle things like parentheses:

    (def whitespace-char (p/token #{#\space #\newline #\tab}))
    (def optional-whitespace (p/many whitespace-char))

    (def open-paren (p/char #\lparen))
    (def close-paren (p/char #\rparen))

    (def number (p/many1 (p/digit)))

    (p/run (p/between (p/>> open-paren optional-whitespace)
                      (p/>> optional-whitespace close-paren)
                      number)
        "(123    )")
    ; (#\1 #\2 #\3)

This example is a bit more complicated than we've seen so far, so slow down and
make sure you know what's going on.

The three parsers we're giving to `between` are:

1. `(p/>> open-paren optional-whitespace)`
2. `(p/>> optional-whitespace close-paren)`
3. `number`

Once you're comfortable with this example, it's time to move on to the next
stage of parsing: building and returning values.

Returning Values
----------------

So far we've looked at many ways to parse input.  If you just need to validate
that input is in the correct format, but not *do* anything with it, you're all
set.  But usually the goal of parsing something is to do things with it, so
let's look at how that works now.

We've been using the word "returns" in a fast-and-loose fashion so far, but now
it's time to look a bit more closely at what it means in _Parsifal_.

### defparser and always

When we looked at parsers created with `char` (like `(p/char #\H)`) we said that
these parsers *returned* that character they parsed.  That's not quite true.
They actually return a specially-wrapped value.

If you want to know exactly what that special wrapping is, watch the [talk][].
But you don't really need to understand the guts to use _Parsifal_.  You just
need to know how to create them.

This is the first time we're going to be creating parsers that are more than
just simple combinations of existing ones.  To do that we need to use a special
macro that handles setting them up properly: `defparser`.  Look at the following
example (don't worry about what `always` is yet):

    (p/defparser sample []
      (p/string "Hello")
      (p/always 42))

First of all, `defparser` doesn't define parsers.  It defines functions that
*create* parsers, just like all of the ones we've seen so far.  Yes, I know how
ridiculous that sounds.  In practice it's only *slightly* confusing.

So now we've got a function `sample` that we can use to create a parser by
calling it:

    (def my-sample-parser (sample))

Okay, now lets run it on some input:

    (p/run my-sample-parser "Hello, world!")
    ; 42

There's a bunch of interesting things going on here, so let's slow down and take
a look.

First, the parsers created by the functions `defparser` defines implicitely wrap
their bodies in `>>`, which as we've seen runs its argument parsers in order and
returns the last result.  So our `(sample)` parser will run the "Hello" string
parser, and then the always parser (which it uses as the result).

So what is this `always` thing?  Well, remember at the beginning of this section
we said that parsers return a specially-wrapped value?  `always` is a way to
simply stick a piece of data in this special wrapper so it can be the result of
a parser.

Here's a little drawing that might help:

    raw input --> (p/run ...) --> raw output
                  |      ^
                  |      |
                  |  wrapped output
                  v      |
               (some parser)

`run` takes the wrapped output from the parser and unwraps it for us before
returning it, which is why our `run` calls always gave us vanilla Venice data
structures before.

We're almost to the point where we can create full-featured parsers.  The final
piece of the puzzle is a way to intercept results and make decisions inside of
our parsers.

### let->>

The `let->>` macro is the magic glue that's going to make creating your parsers
fun.  In a nutshell, it lets you bind (unwrapped) parser results to names, which
you can then use normally.  Let's just take a look at how it works:

    (p/defparser word []
      (p/many1 (p/letter)))

    (p/defparser greeting []
      (p/let->> [prefix (p/string "Hello, ")
                 name (word)
                 punctuation (p/choice (p/char #\.)
                                       (p/char #\!))]
        (if (= punctuation #\!)
          (p/always [(apply str name) :excited])
          (p/always [(apply str name) :not-excited]))))

    (p/run (greeting) "Hello, Cat!")
    ; ["Cat" :excited]

    (p/run (greeting) "Hello, Dog.")
    ; ["Dog" :not-excited]

There's a lot happening here so let's look at it piece-by-piece.

First we use `defparser` to make a `word` function for creating word parsers.
We could have done this with `(def word (p/many1 (p/letter)))` and then used it as
`word` later, but I find it's easier to just use `defparser` for everything.
That way we always get parsers the same way: by calling a function.

Next we have our `greeting` parser (technically a function that makes a parser,
but you get the idea by now).  Inside we have a `let->>` that runs three parsers
and binds their (unwrapped) results to names:

1. `(p/string "Hello, ")` parses a literal string.  `prefix` gets bound to the
   string `"Hello, "`.
2. `(p/word)` parses one or more letters.  `name` gets bound to the result, which
   is a sequence of chars like `(#\C #\a #\t)`.
3. `(p/choice (p/char #\.) (p/char #\!))` parses a period or exclamation point.
   `punctuation` gets bound to the character that was parsed, like `#\.` or `#\!`.

That's it for the binding section.  Next we have the body of the `let->>`.  This
needs to return a *wrapped* value, but we can do anything we like with our bound
variables to determine what to return.  In this case we return different things
depending on whether the greeting ended with an exclamation point or not.

Notice how the return values are wrapped in `(p/always ...)`.  Also notice how all
the bound values have been unwrapped for us by `let->>`.  `name` really is just
a sequence of characters which can be used with `(apply str ...)` as usual.

You might wonder whether you can move the `(apply str ...)` into the `let->>`
binding form, so we don't have to do it twice.  Unfortunately you can't.
**Every right hand side in a `let->>` binding form has to evaluate to a parser**.

If you tried to do something like `(p/let->> [name (apply str (word))] ...)` it
wouldn't work for two reasons.  First, `let->>` evaluates the right hand side
and expects the result to be a parser, which it then runs.  So it would call
`(apply str some-word-parser)` and get a string back, which isn't a parser.

Second, `let->>` unwraps the return value of `(word)` right before it binds it,
so even if the first problem weren't true, `(apply str ...)` would get a wrapped
value as its argument, which is not going to work.

Of course, you can do anything you want in the *body* of a `let->>`, so this is
fine:

    (p/let->> [name (word)]
      (let [name (apply str name)]
        (p/always name)))

`let` in this example is a vanilla Venice `let`.

Binding forms in a `let->>` are executed in order, and importantly, later forms
can refer to earlier ones.  Look at this example:

    (p/defparser sample []
      (p/let->> [sign (p/choice (p/char #\+)
                                (p/char #\-))
               word (if (= sign #\+)
                      (p/string "plus")
                      (p/string "minus"))]
        (p/always [sign word])))

    (p/run (sample) "+plus")
    ; [#\+ "plus"]

    (p/run (sample) "-minus")
    ; [#\- "minus"]

    (p/run (sample) "+minus")
    ; ParseError...

In this example, `sign` gets bound to the unwrapped result of the `choice`
parser, which is a character.  Then we use that character to determine which
parser to use in the next binding.  If the sign was a `#\+`, we parse the string
`"plus"`.  Likewise for minus.

Notice how mixing the two in the last example produced an error.  We saw the
`#\+` and decided that we'd used the `(p/string "plus")` parser for the next input,
but it turned out to be `"minus"`.

Tips and Tricks
---------------

That's about it for the basics!  You now know enough to parse a wide variety of
things by building up complex parsers from very simple ones.

Before you go, here's a few tips and tricks that you might find helpful.

### You can parse more than just strings

Remember that _Parsifal_ operates on sequences of input.  These don't
necessarily have to be strings.


### You can throw away `let->>` bindings

Sometimes you're writing a `let->>` form and encounter a value that you don't
really need to bind to a name.  Instead of stopping the `let->>` and nesting
a `>>` inside it, just bind the value to a disposable name, like `_`:

    (p/defparser float []
      (p/let->> [integral (p/many1 (p/digit))
                 _ (p/char #\.)
               fractional (p/many1 (p/digit))]
        (let [integral (apply str integral)
              fractional (apply str fractional)]
          (p/always (double (str integral "." fractional))))))

    (p/run (float) "1.4")
    ; 1.4

    (p/run (float) "1.04")
    ; 1.04

    (p/run (float) "1.0400000")
    ; 1.04
