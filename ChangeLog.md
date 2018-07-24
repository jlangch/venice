# Change Log


All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).



## [Unreleased]

### Added

- added function composition
- added partial functions
- added functions partition, distinct, and dedupe
- added an explicit Venice type for keywords
- added keywords to act like functions on maps: (:b {:a 1 :b 2}) => 2
- added JSON util functions (requires Jackson lib at runtime on classpath)
- added support for scoped enum values while interacting with Java objects 
- added support for Java arrays

### Removed

- removed ...

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
