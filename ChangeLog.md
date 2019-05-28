# Change Log


All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).



## [1.5.7] - 2019-05-xx

### Added

- function `io/zip-list` to list zip  file content

### Changed

- ... 

### Fixed

- function `io/zip` to support directories



## [1.5.6] - 2019-05-27

### Added

- XML doc to cheat sheet
- zip and gzip functions

### Changed

- try/catch/finally supports now multiple body expressions for each of the 
  try, catch, finally blocks
- try-with/catch/finally supports now multiple body expressions  for each
  of the try-with, catch, finally blocks 
- JLine3 lib to actual version 3.11.0
- to Gradle 5.4.1



## [1.5.5] - 2019-05-23

### Added

- special form `set!` to set a global or thread-local variable 
- the option 'decimal-as-double' to the JSON writer to control whether 
  the writer emits decimals as JSON strings or doubles.
- improvements for 'ring' and 'tomcat' module
- an XML parser built on the JDK's SAX parser: [XML](doc/readme/ext-xml.md) 



## [1.5.4] - 2019-05-14

### Added

- support for JSON: `json/write-str`, `json/read-str`, `json/spit`, `json/slurp`, `json/pretty-print`



## [1.5.3] - 2019-05-11

### Fixed

- map conversion to Java HashMap when a map entry value was _nil_



## [1.5.2] - 2019-05-10

### Added

- functions `regex/find-all`, `regex/find-group`, and `regex/find-all-groups`
- code completion for the REPL
- function `match?` that replaces `match`. `match` did not follow the naming conventions.
- function `match-not?` that replaces `match-not`. `match-not` did not follow the naming conventions.

### Fixed

- demo WEB application
- a `~{x}` string interpolation problem with trailing `"`: `(let [a 1 b 2] """{ "~{a}": "~{b}" }""")`
- a `~(x)` string interpolation problem with trailing `"`: `(let [a 1 b 2] """{ "~(str a)": "~(str b)" }""")`

### Deprecated

- function `match`
- function `match-not`



## [1.5.1] - 2019-05-07

### Added

- function `regex/matches`
- a simple templating module: [Kira](doc/readme/ext-kira.md)
- encoding/decoding functions for Base64 and URLs
- escape functions for HTML and XML

### Fixed

- `regex/group` to handle `nil` groups correctly



## [1.5.0] - 2019-05-03

### Added

- stack datatype
- defaults for keyword as function: `(:c {:a 1 :b 2} :none))`
- an embedded [Apache Tomcat WEB Server](doc/readme/ext-tomcat.md) launcher 

### Changed

- handle REPL parse errors gracefully, allow to get the incorrect expression from the history to fix it

### Fixed

- the order of the stacktrace frames (reversed it)
- dereferencing a `future` when the future has thrown an exception
- `io/copy-stream`

### Incompatible Changes

- removed 'io/spit-temp-file', replace with 'io/spit'
- removed 'io/slurp-temp-file', replace with 'io/slurp'



## [1.4.5] - 2019-04-26

### Added

- functions `str/letter?`, `str/digit?`, `str/whitespace?`, `str/linefeed?`
- regular expression functions: `regex/pattern`, `regex/matcher`, `regex/find?`, `regex/group`, ...
- enhanced REPL to change and manage the sandbox. Type `!sandbox` in the REPL.

### Fixed

- a problem with detecting unauthorized private function calls



## [1.4.4] - 2019-04-22

### Added

- function `defn-` to simply private function definition
- inheritance of thread-local vars for child threads used by futures and agents



## [1.4.3] - 2019-04-19

### Added

- support for raw Java array data types. E.g.: `(long-array '(1 2 3))`
- locale support for `str/format` function
- function `resolve` to resolve symbols
- private functions

### Fixed

- unit tests for Java 11



## [1.4.2] - 2019-04-04

### Added

- enhanced the function `into` to handle raw Java collections the most 
  efficient way
- int numeric type (int literals have suffix 'I'). E.g. `2I`, 
  `(+ 2I 3I)`, `(int 2)`
- a limit to the number of bytes that can be written to a 
  _CapturingPrintStream_ by a Venice script to prevent buggy or malicious 
  scripts to overrun the memory. Defaults to 10MB.

### Fixed

- a problem with the `*out*` dynamic var not being visible to 
  precompiled scripts



## [1.4.1] - 2019-03-31

### Added

- improved performance of precompiled scripts
- significantly reduced the size of precompiled scripts
- math module with bigint support
- support for `compare` function for all raw java types. Thereby sequences 
  with raw java types can be sorted.
- function `instance?`
- sandbox support for macros. E.g. Venice macros like `load-file`
  and `load-classpath-file` can now be rejected by the sandbox without needing
  to sandbox the underlying functions.



## [1.4.0] - 2019-03-18

### Added

- Clojure style multi methods for dynamic method dispatching



## [1.3.6] - 2019-03-02

### Added

- result history to the REPL. The symbols `*1`, `*2`, `*3` return the last, the second 
  last and the third last result
- REPL help 

### Changed

- meta data handling for documenting. E.g.: `(def ^:test m [1 2 3])`, `(def ^{:test true} m [1 2 3])`



## [1.3.5] - 2019-02-24

### Added

- REPL multi-line support

### Fixed

- handling EOF/EOL in strings and providing better error messages



## [1.3.4] - 2019-02-22

### Fixed

- problem with Maven repo push



## [1.3.3] - 2019-02-21

### Changed

- REPL to be built on JLine3



## [1.3.2] - 2019-02-17

### Added

- function `replace`

### Changed

- VAVR lib to actual version 0.10.0 

### Fixed

- compareTo for collection types (list, vector, set, and map)
- a problem with incorrectly evaluated quoted symbols used as map keys. 
  E.g.: `(replace {'a 5} [10 'a])`



## [1.3.1] - 2019-02-08

### Added

- map to work as function that delivers a value to a passed key 
  `({:a 1 :b 2} :b)`
- functions (`prof`, `perf`, `dorun`) to simplify Venice performance 
  tests

### Fixed

- ValueException stack trace



## [1.3.0] - 2019-01-19

### Added

- migrated all collections to immutable persistent data structures
  based on VAVR.



## [1.2.2] - 2019-01-10

### Added

- multi-arity functions and macros



## [1.2.1] - 2019-01-06

### Added

- line escapes to make `str/strip-indent` work (see README)
- function `sqrt`
- nested associative destructuring

### Fixed

- functions `print` and `println` to print `nil` values correctly
- sequential destructuring when using remaining and :as element 
  together `(let [[x y & z :as all] [1 2 3 4 5 6]] ...)`



## [1.2.0] - 2018-12-28

### Added

- support for triple quoted, multi-line string literals: `"""{ "name": "john" }"""`
- string interpolation: `(do (let [x 100] """~{x} ~(inc x)"""))`



## [1.1.3] - 2018-12-21

### Added

- a cached thread pool to run the futures for scripts with execution 
  time limit

### Fixed

- execution time limit with sandbox



## [1.1.2] - 2018-12-10

### Added

- a configurable execution time limit for Venice scripts running
  within a sandbox
- multi expression body for functions
- _defn_ macro support for pre conditions

### Fixed

- agent _shutdown-agents?_ arity error message



## [1.1.1] - 2018-11-30

### Added

- more implicit type conversions to convert Java lists to Venice lists.

### Fixed

- default sandbox rules to allow invoking `(delay 100)` under a sandbox



## [1.1.0] - 2018-11-25

### Added

- agents that complement Venice concurrency features. Agents 
  provide independent, asynchronous change of state.
- special form _defonce_. e.g: `(defonce x 100)`
- dynamic (thread-local) binding. e.g: `(binding [x 100] (print x))`
- _with-out-str_ macro that returns the captured text from stdout.
- _delay_ macro that defers function evaluation.
- function _realized?_ for delays, futures, and promises
- functions _io/file-parent_, _io/file-name_, _io/file-path_

### Fixed

- error message for map creation with an odd number of items 
  and added file location.



## [1.0.0] - 2018-11-09

### Added

- improvements to stack traces



## [0.9.12] - 2018-11-08

### Added

- user friendly stack traces as an alternative to pure Java stack traces
  that are pretty difficult to read

### Fixed

- internal datatypes on public APIs.



## [0.9.11] - 2018-11-07

### Added

- support for optional names for anonymous functions. e.g. `(fn double [x] (* 2 x))`
- function _doc_
- function _list*_
- function _io/file-size_

### Fixed

- the _with-sh-dir_ macro to check that the directory exists. If not
  an exception is thrown.
- sandbox for proxy methods. Venice proxy callbacks can potentially
  run in a thread other than the Venice parent function. The Venice parent
  function's sandbox is now applied to the proxy function.



## [0.9.10] - 2018-10-28

### Added

- function _with-sh-throw_ that causes subsequent _sh_ calls to throw an 
  exception if the exit code of the spawned shell process is not equal
  to 0.
- function _io/delete-file_ supports multiple files
- function _time/leap-year?_
- function _time/length-of-year_
- function _time/length-of-month_
- _cons_, _conj_, _disj_ for sets
- reader macro `#{}` to create sets. e.g. `#{1 2}`
- reader macro `@` for dereference `(@a -> (deref a)`
- reader macro `#()` to create anonymous functions. e.g. `(map #(* 2 %1) (range 1 5))`

### Fixed

- default sandbox class rules (they were missing)



## [0.9.9] - 2018-10-21

### Added

- function _compare_
- function _printf_
- support to execute scripts: `java -jar venice-0.9.9.jar -script "(+ 1 1)"`



## [0.9.8] - 2018-10-15

### Fixed

- _partial_ function



## [0.9.7] - 2018-10-15

### Added

- macro _case_
- pre-conditions for functions
- function _str/char_ to convert a number into single char string



## [0.9.6] - 2018-10-01

### Fixed

- _def_ global variables to be redefined locally



## [0.9.5] - 2018-09-30

### Added

- ability to mix Venice functions with Java streams
- migrated to JUnit 5

### Fixed

- _def_ creates now global variables instead of using the local env context



## [0.9.4] - 2018-09-15

### Added

- function 'io/load-classpath-resource' to load resources from 
  classpath. The function is sandboxed.
- function 'lock' and 'unlock' to WebDAV extension module.

### Fixed

- documentation for function 'future'. The function is sandboxed!
- function 'reduce' to work with maps too.



## [0.9.3] - 2018-09-06

### Added

- function 'repeatedly'

### Fixed

- sandboxed access to system properties
- a Java interop issue with with boxing args to type byte[] (the
  boxing works now for all ByteBuffer subclasses)



## [0.9.2] - 2018-09-03

### Added

- support for Futures (the sandbox is active in the future's thread)
- support for Promises (aka CompletedFuture)
- support for sandboxed Java system properties
- function 'system-prop' to access to Java system properties
- function 'butlast'
- threading macro 'as->'

### Fixed

- 'sh' function when providing stdin data to subprocess



## [0.9.1] - 2018-08-30

### Added

- function 'time/with-time'
- function 'time/first-day-of-month', 'time/last-day-of-month'
- function 'time/earliest', 'time/latest', 'time/within?'
- function 'name', 'split-with'
- function 'sh', 'os?', 'sleep'

### Fixed

- XChart xy-chart
- try-with-resources to close the resources in reversed order 
  of its definition



## [0.9.0] - 2018-08-27

### Added

- time functions



## [0.8.5] - 2018-08-23

### Added

- support for thread local
- webdav extension module

### Fixed

- a hiding exception problem with JavaInterop on static method calls
- printing full exception stack trace in REPL not just the message



## [0.8.4] - 2018-08-21

### Added

- support for catching multiple exceptions within a try-catch-finally block
- chart examples
- updated cheat sheets
- function 'load-classpath-file' to sequentially read and evaluate the set 
  of forms contained in the classpath file.
- function 'io/move-file'

### Fixed

- documentation for functions 'proxify' and 'cond'
- xchart extension module xy-chart axis styling



## [0.8.3] - 2018-08-18

### Added

- function 'io/slurp-stream'
- function 'io/spit-stream'
- function 'io/delete-file-on-exit'
- function 'flush' takes an optional argument output stream. E.g.: (flush os)
- try-with-resources block 'try-with'

### Fixed

- Cheat Sheet for functions referenced multiple times
- cheat sheet page breaks on PDF
- try-catch-finally. The finally block is only evaluated for side effects



## [0.8.2] - 2018-08-16

### Added

- dpi scaling to charts (high-res charts)
- sandboxing for created temp files 
- more documented functions in the cheatsheet

### Fixed

- xchart/to-bytes
- str/join



## [0.8.1] - 2018-08-10

### Added

- function 'type' to reveal the type of an item
- set functions 'intersect' and 'union'

### Fixed

- function 'into'.
- adding elements to data type 'set'.
- README Java Interop example



## [0.8.0] - 2018-08-09

### Added

- function keep, merge, assoc-in, get-in
- function update, vec, difference
- function every? and any? operating on sequential collections
- associative destructuring 
    - `(let [{:keys [a b]} {:a 1 :b 2}] (+ a b))`
    - `(let [{:syms [a b]} {'a 1 'b 2}] (+ a b))`
    - `(let [{:strs [a b]} {"a" 1 "b" 2}] (+ a b))`
    - `(fn [x {:keys [a b]}] (+ x a b))`
- destructuring `:as` and `:or`options
    - `(fn [x {:keys [a b] :or {b 2} :as params}] (+ x a b))`
    - `(let [[x y :as coords] [1 2 3 4]] (str "x:" x ", y:" y ", dim:" (count coords)))`
- improved Java interop with Java functions returning arrays
    - `byte[]` is converted to bytebuf
    - all other arrays are converted to vector

### Fixed

- Java interop proxifying interfaces with void functions (e.g. Runnable)
- sort function to work on sets as well
- apply function to handle coll with nil value correctly



## [0.7.2] - 2018-08-04

### Added

- function empty
- function mapv
- function docoll
- support to change 'stdout' stream by adding the variable `*out*`

### Fixed

- mixed precision math (+, -, *, /). Implicitly coerce values to higher 
  precision operand type. As a result (range 0 5 0.5) emits correct values and 
  does not loop anymore.



## [0.7.1] - 2018-08-02

### Added

- function str/blank?
- support for `**` pattern in sandbox rules: E.g.: `org.apache.commons.text.**:*`

### Fixed

- Java Interop callbacks to support import statements



## [0.7.0] - 2018-07-31

### Added

- function str/quote
- function bytebuf-from-string, bytebuf-to-string
- support for Java callbacks



## [0.6.1] - 2018-07-27

### Fixed

- REPL
- CheatSheet: not is a function not a macro
- JSON lib detection


## [0.6.0] - 2018-07-26

### Removed

- removed function 'class-for-name'. The JavaInterop function already provides that. E.g.: (. :java.lang.Math :class)

### Fixed

- loading forms from strings, files, and modules 
- JavaInterop on invoking methods with byte array parameters


## [0.5.0] - 2018-07-24

### Fixed

- OSS release



## [0.4.0] - 2018-07-24

### Added

- function composition
- partial functions
- functions partition, distinct, and dedupe
- an explicit Venice type for keywords
- keywords to act like functions on maps: (:b {:a 1 :b 2}) => 2
- JSON util functions (requires Jackson lib at runtime on classpath)
- support for scoped enum values while interacting with Java objects 
- support for Java arrays

### Fixed

- SecurityException handling
- made 'not' a function (instead of a macro) so it can be used from higher order functions



## [0.3.0] - 2018-07-18

### Added

- Java Interop function (. classname :class). Returns the class for the classname
- improved error messages for Java Interop
- smarter type coercion, replaces simple casts and giving better error messages
  if the coercion is not possible
- zipmap, interleave, interpose, nfirst, and nlast functions
- PDF cheatsheet



## [0.2.0] - 2018-07-15

### Added

- line and column number to parser exception
- file I/O functions
- refactored sandbox
- supporting escaped unicode '\u0041' characters in string literals

### Fixed

- JavaInterop passing enums args



## [0.1.0] - 2018-07-10

### Added

- project opened
