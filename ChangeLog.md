# Change Log


All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).



## [1.4.0] - 2019-03-18

### added

- added Clojure style multi methods for dynamic method dispatching



## [1.3.6] - 2019-03-02

### added

- added a result history to the REPL `*1`, `*2`, `*3` return the last, the second 
        last and the third last result
- added REPL help 
- reworked meta data handling. E.g.: `(def ^:test m [1 2 3])`, `(def ^{:test true} m [1 2 3])`



## [1.3.5] - 2019-02-24

### added

- added REPL multi-line support

### fixed

- fixed handling EOF/EOL in strings and providing better error messages



## [1.3.4] - 2019-02-22

### fixed

- fixed problem with Maven repo push



## [1.3.3] - 2019-02-21

### added

- migrated REPL to JLine3



## [1.3.2] - 2019-02-17

### added

- upgraded to VAVR 0.10.0 
- added function `replace`

### fixed

- fixed compareTo for collection types (list, vector, set, and map)
- fixed a problem with incorrectly evaluated quoted symbols used as map keys. 
        E.g.: `(replace {'a 5} [10 'a])`



## [1.3.1] - 2019-02-08

### added

- added map to work as function that delivers a value to a passed key 
        `({:a 1 :b 2} :b)`
- added functions (`prof`, `perf`, `dorun`) to simplify Venice performance 
        tests

### fixed

- fixed ValueException stack trace



## [1.3.0] - 2019-01-19

### added

- migrated all collections to immutable persistent data structures
           based on VAVR.



## [1.2.2] - 2019-01-10

### added

- added multi-arity functions and macros



## [1.2.1] - 2019-01-06

### added

- added line escapes to make `str/strip-indent` work (see README)
- added function `sqrt`
- added nested associative destructuring

### fixed

- fixed functions `print` and `println` to print `nil` values correctly
- fixed sequential destructuring when using remaining and :as element 
  together `(let [[x y & z :as all] [1 2 3 4 5 6]] ...)`



## [1.2.0] - 2018-12-28

### added

- added support for triple quoted, multi-line string literals: `"""{ "name": "john" }"""`
- added string interpolation: `(do (let [x 100] """~{x} ~(inc x)"""))`



## [1.1.3] - 2018-12-21

### added

- added a cached thread pool to run the futures for scripts with execution 
        time limit

### fixed

- fixed execution time limit with sandbox



## [1.1.2] - 2018-12-10

### added

- added a configurable execution time limit for Venice scripts running
        within a sandbox
- added multi expression body for functions
- added _defn_ macro support for pre conditions

### fixed

- fixed agent _shutdown-agents?_ arity error message



## [1.1.1] - 2018-11-30

### added

- added more implicit type conversions to convert Java lists to Venice lists.

### fixed

- fixed default sandbox rules to allow invoking `(delay 100)` under a sandbox



## [1.1.0] - 2018-11-25

### added

- added agents that complement Venice concurrency features. Agents 
        provide independent, asynchronous change of state.
- added special form _defonce_. e.g: `(defonce x 100)`
- added dynamic (thread-local) binding. e.g: `(binding [x 100] (print x))`
- added _with-out-str_ macro that returns the captured text from stdout.
- added _delay_ macro that defers function evaluation.
- added function _realized?_ for delays, futures, and promises
- added functions _io/file-parent_, _io/file-name_, _io/file-path_

### fixed

- fixed error message for map creation with an odd number of items 
        and added file location.



## [1.0.0] - 2018-11-09

### added

- added improvements to stack traces



## [0.9.12] - 2018-11-08

### added

- added user friendly stack traces as an alternative to pure Java stack traces
        that are pretty difficult to read

### fixed

- fixed internal datatypes on public APIs.



## [0.9.11] - 2018-11-07

### added

- added support for optional names for anonymous functions. e.g. `(fn double [x] (* 2 x))`
- added function _doc_
- added function _list*_
- added function _io/file-size_

### fixed

- fixed the _with-sh-dir_ macro to check that the directory exists. If not
        an exception is thrown.
- fixed sandbox for proxy methods. Venice proxy callbacks can potentially
        run in a thread other than the Venice parent function. The Venice parent
        function's sandbox is now applied to the proxy function.



## [0.9.10] - 2018-10-28

### added

- added function _with-sh-throw_ that causes subsequent _sh_ calls to throw an 
        exception if the exit code of the spawned shell process is not equal
        to 0.
- added function _io/delete-file_ supports multiple files
- added function _time/leap-year?_
- added function _time/length-of-year_
- added function _time/length-of-month_
- added _cons_, _conj_, _disj_ for sets
- added reader macro `#{}` to create sets. e.g. `#{1 2}`
- added reader macro `@` for dereference `(@a -> (deref a)`
- added reader macro `#()` to create anonymous functions. e.g. `(map #(* 2 %1) (range 1 5))`

### fixed

- fixed default sandbox class rules (they were missing)



## [0.9.9] - 2018-10-21

### added

- added function _compare_
- added function _printf_
- added support to execute scripts: `java -jar venice-0.9.9.jar -script "(+ 1 1)"`



## [0.9.8] - 2018-10-15

### fixed

- fixed _partial_ function



## [0.9.7] - 2018-10-15

### added

- added macro _case_
- added pre-conditions for functions
- added function _str/char_ to convert a number into single char string



## [0.9.6] - 2018-10-01

### fixed

- fixed _def_ global variables to be redefined locally



## [0.9.5] - 2018-09-30

### Added

- added ability to mix Venice functions with Java streams
- migrated to JUnit 5

### fixed

- fixed _def_ creates now global variables instead of using the local env context



## [0.9.4] - 2018-09-15

### Added

- added function 'io/load-classpath-resource' to load resources from 
        classpath. The function is sandboxed.
- added function 'lock' and 'unlock' to WebDAV extension module.

### fixed

- fixed documentation for function 'future'. The function is sandboxed!
- fixed function 'reduce' to work with maps too.



## [0.9.3] - 2018-09-06

### Added

- added function 'repeatedly'

### fixed

- fixed sandboxed access to system properties
- fixed a Java interop issue with with boxing args to type byte[] (the
  boxing works now for all ByteBuffer subclasses)



## [0.9.2] - 2018-09-03

### Added

- added support for Futures (the sandbox is active in the future's thread)
- added support for Promises (aka CompletedFuture)
- added support for sandboxed Java system properties
- added function 'system-prop' to access to Java system properties
- added function 'butlast'
- added threading macro 'as->'

### fixed

- fixed 'sh' function when providing stdin data to subprocess



## [0.9.1] - 2018-08-30

### Added

- added function 'time/with-time'
- added function 'time/first-day-of-month', 'time/last-day-of-month'
- added function 'time/earliest', 'time/latest', 'time/within?'
- added function 'name', 'split-with'
- added function 'sh', 'os?', 'sleep'

### fixed

- fixed XChart xy-chart
- fixed try-with-resources to close the resources in reversed order 
        of its definition



## [0.9.0] - 2018-08-27

### Added

- added time functions



## [0.8.5] - 2018-08-23

### Added

- added support for thread local
- added webdav extension module

### fixed

- fixed a hiding exception problem with JavaInterop on static method calls
- fixed printing full exception stack trace in REPL not just the message



## [0.8.4] - 2018-08-21

### Added

- added support for catching multiple exceptions within a try-catch-finally block
- added chart examples
- added updated cheat sheets
- added function 'load-classpath-file' to sequentially read and evaluate the set 
        of forms contained in the classpath file.
- added function 'io/move-file'

### fixed

- fixed documentation for functions 'proxify' and 'cond'
- fixed xchart extension module xy-chart axis styling



## [0.8.3] - 2018-08-18

### Added

- added function 'io/slurp-stream'
- added function 'io/spit-stream'
- added function 'io/delete-file-on-exit'
- added function 'flush' takes an optional argument output stream. E.g.: (flush os)
- added try-with-resources block 'try-with'

### fixed

- fixed Cheat Sheet for functions referenced multiple times
- fixed cheat sheet page breaks on PDF
- fixed try-catch-finally. The finally block is only evaluated for side effects



## [0.8.2] - 2018-08-16

### Added

- added dpi scaling to charts (high-res charts)
- added sandboxing for created temp files 
- added more documented functions in the cheatsheet

### fixed

- fixed xchart/to-bytes
- fixed str/join



## [0.8.1] - 2018-08-10

### Added

- added function 'type' to reveal the type of an item
- added set functions 'intersect' and 'union'

### fixed

- fixed function 'into'.
- fixed adding elements to data type 'set'.
- fixed README Java Interop example



## [0.8.0] - 2018-08-09

### Added

- added function keep, merge, assoc-in, get-in
- added function update, vec, difference
- added function every? and any? operating on sequential collections
- added associative destructuring 
    - `(let [{:keys [a b]} {:a 1 :b 2}] (+ a b))`
    - `(let [{:syms [a b]} {'a 1 'b 2}] (+ a b))`
    - `(let [{:strs [a b]} {"a" 1 "b" 2}] (+ a b))`
    - `(fn [x {:keys [a b]}] (+ x a b))`
- added destructuring `:as` and `:or`options
    - `(fn [x {:keys [a b] :or {b 2} :as params}] (+ x a b))`
    - `(let [[x y :as coords] [1 2 3 4]] (str "x:" x ", y:" y ", dim:" (count coords)))`
- improved Java interop with Java functions returning arrays
    - `byte[]` is converted to bytebuf
    - all other arrays are converted to vector

### fixed

- fixed Java interop proxifying interfaces with void functions (e.g. Runnable)
- fixed sort function to work on sets as well
- fixed apply function to handle coll with nil value correctly



## [0.7.2] - 2018-08-04

### Added

- added function empty
- added function mapv
- added function docoll
- added support to change 'stdout' stream by adding the variable `*out*`

### fixed

- fixed mixed precision math (+, -, *, /). Implicitly coerce values to higher 
  precision operand type. As a result (range 0 5 0.5) emits correct values and 
  does not loop anymore.



## [0.7.1] - 2018-08-02

### Added

- added function str/blank?
- added support for `**` pattern in sandbox rules: E.g.: `org.apache.commons.text.**:*`

### fixed

- fixed Java Interop callbacks to support import statements



## [0.7.0] - 2018-07-31

### Added

- added function str/quote
- added function bytebuf-from-string, bytebuf-to-string
- added support for Java callbacks



## [0.6.1] - 2018-07-27

### Fixed

- fixed REPL
- fixed CheatSheet: not is a function not a macro
- fixed JSON lib detection


## [0.6.0] - 2018-07-26

### Removed

- removed function 'class-for-name'. The JavaInterop function already provides that. E.g.: (. :java.lang.Math :class)

### Fixed

- fixed loading forms from strings, files, and modules 
- fixed JavaInterop on invoking methods with byte array parameters


## [0.5.0] - 2018-07-24

### Fixed

- OSS release



## [0.4.0] - 2018-07-24

### Added

- added function composition
- added partial functions
- added functions partition, distinct, and dedupe
- added an explicit Venice type for keywords
- added keywords to act like functions on maps: (:b {:a 1 :b 2}) => 2
- added JSON util functions (requires Jackson lib at runtime on classpath)
- added support for scoped enum values while interacting with Java objects 
- added support for Java arrays

### Fixed

- fixed SecurityException handling
- made 'not' a function (instead of a macro) so it can be used from higher order functions



## [0.3.0] - 2018-07-18

### Added

- added Java Interop function (. classname :class). Returns the class for the classname
- added improved error messages for Java Interop
- added smarter type coercion, replaces simple casts and giving better error messages
  if the coercion is not possible
- added zipmap, interleave, interpose, nfirst, and nlast functions
- added PDF cheatsheet



## [0.2.0] - 2018-07-15

### Added

- added line and column number to parser exception
- added file I/O functions
- refactored sandbox
- supporting escaped unicode '\u0041' characters in string literals

### Fixed

- fixed JavaInterop passing enums args



## [0.1.0] - 2018-07-10

### Added

- project opened
