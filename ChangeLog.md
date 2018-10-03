# Change Log


All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).



## [0.9.7] - 2018-10-xx

### added

- added function 'str/char' to convert a number into single char string



## [0.9.6] - 2018-10-01

### fixed

- fixed fixed _def_ global variables to be redefined locally



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
