# Change Log


All notable changes to this project will be documented in this file.



## [1.12.81] - 2026-02-xx

### Enhancements

- Added the setup option `-minimal` to setup a minimal REPL

```
$ java -jar venice-1.12.81.jar -setup -minimal -colors -dir ./repl

     REPL_HOME
     ├── libs
     │   ├── venice-1.12.81.jar
     │   ├── jansi-2.4.1.jar
     │   └── repl.json
     ├── tmp
     ├── repl.env
     └── repl.sh
```


- The REPL installer creates now an additional *repl.command* file on Mac OSX.
  While the *repl.sh* shell script must be executed from within a terminal,
  the *repl.command* shell script can be double-clicked in the OSX Finder 
  to run. 
  Note: *repl.sh* can be manually associated with the Terminal App to allow 
        double click to run it


## [1.12.80] - 2026-02-14

### Bugs

- Allow multiline single and triple quoted string literals in the REPL

```
  venice> (def x """
                 123
                 456
                 """)

  venice> (def y "123
                  456")
```



## [1.12.79] - 2026-02-11

### Enhancements

- Bumped VAVR from 0.11.0 to 1.0.0
- IPC dead letter queue can now be disabled (dead letter queue is enabled by default)
- IPC response messages mirror now the request's destination name even in error case.



## [1.12.78] - 2026-02-08

### Enhancements

- Improved IPC ACL api



## [1.12.77] - 2026-02-05

### Enhancements

- Enhanced IPC ACL management functions to allow both strings and keywords as function arguments



## [1.12.76] - 2026-02-04

### Enhancements

- Added IPC ACLs
- Added IPC heartbeat checking
- Made IPC dead-letter queue publicly available



## [1.12.75] - 2026-02-01

### Enhancements

- Added IPC authorization for creating queues and topics
- Added IPC authorization for getting server statistics and info
- Added IPC destination functions (send/receive)

- Made IPC dead-letter queue size configurable 


## [1.12.74] - 2026-01-28

### Enhancements

- Added more IPC performance optimizations for small messages (<16KB). 



## [1.12.73] - 2026-01-22

### Enhancements

- Optimized IPC memory and throughput



## [1.12.72] - 2026-01-18

### Enhancements

- Improved IPC message metadata. Made it smaller and optimized the encoding/decoding



## [1.12.71] - 2026-01-16

### Enhancements

- Added heartbeat support to IPC client/server
- Added support for IPC Unix domain sockets
- Added the option to configure send and receive buffer sizes for IPC
  client and server sockets
- Activated TCP_NODELAY on IPC client/server sockets



## [1.12.70] - 2026-01-08

### Enhancements

- Added authentication support to IPC client/server

### Bugs

- Fixed a 'try-with' auto closing resources edge case when a binding expression fails



## [1.12.69] - 2025-12-24

### Enhancements

- Added an optional logger for the IPC server
- Made IPC publish/subscribe completely asynchronous in the client and the server
- IPC encryption can now be enforced by the server for all client connections

### Updated dependencies:

- Bumped VAVR from 0.10.7 to 0.11.0



## [1.12.68] - 2025-12-10

### Enhancements

- Complete rewrite of the durable queue. Optimized the lock critical section for read/write 
  WAL records together with offer/poll of messages to process these as an atomical operation.

### Bugs

- Fixed `ipc/create-queue` function argument parsing when called for a client node



## [1.12.67] - 2025-12-07

### Enhancements

- Added IPC Write-Ahead-Log (WAL) compaction (optional)
- Added IPC Write-Ahead-Log (WAL) record compression (optional)
- Added IPC Write-Ahead-Log (WAL) technical logging for WAL lifecycle events and errors



## [1.12.66] - 2025-12-03

### Enhancements

- Added IPC support for Write-Ahead-Logs (WAL) for queues as technical
  preview.



## [1.12.65] - 2025-11-26

### Enhancements

- Added IPC support for temporary queues



## [1.12.64] - 2025-11-23

### Enhancements

- Added a circular buffer data type
- Added IPC offer/poll optional *reply-to* queue
- Improved IPC timeout management
- Improved the script auto-run Venice JAR rewriter



## [1.12.63] - 2025-11-19

### Enhancements

- Corrected the version number. Enhanced the Gradle build to check for a valid
  version number before publishing to Sonatype.



## [1.12.62] - 2025-11-19

### Enhancements

- Added function `ipc/clone` to clone IPC clients
- Added IPC message expiration

### Bugs

- Fixed the IPC error circular buffer size. It was effectively one item larger 
  than the specified capacity.



## [1.12.61] - 2025-11-16

### Enhancements

- Simplified 'for' macro sequence expression partitioning into groups
- Enhanced IPC api with async functions returning futures
- Added IPC requestId to messages to allow for idempotency checks

### Bugs

- Fixed IPC oneway send function



## [1.12.60] - 2025-11-14

### Bugs

- Fixed a 'for' macro edge case when normalizing the sequence expressions into groups



## [1.12.59] - 2025-11-12

### Enhancements

- Added for list comprehension with `:when`, `:while`, and `:let` modifiers
- Added function `partition-at`



## [1.12.58] - 2025-10-20

### Enhancements

- Added optional AES-256 GCM encryption for IPC message transport. 
  The encryption secret is generated and exchanged using the Diffie–Hellman key 
  exchange algorithm.
- Enhanced `str/encode-base64` and `str/decode-base64` to support an optional
  encoding/decoding schema: `:Standard` (RFC4648) or `:UrlSafe` (RFC4648_URLSAFE)
- Overhauled `:crypt` module



## [1.12.57] - 2025-10-09

### Enhancements

- Added gzip compression for IPC message payloads
- Removed gitpod support



## [1.12.56] - 2025-10-01

### Enhancements

- Added deque datatype (bounded or unbounded)

### Bugs

- Fixed Maven download 403 HTTP error by adding an explicit user agent to the HTTP requests



## [1.12.55] - 2025-09-22

### Enhancements

- Added IPC (Inter-Process-Communication) module. Supports request/response messages
  between a client and a server as well as publish / subcribe with many clients
  and an orchestrating server.



## [1.12.54] - 2025-08-29

### Enhancements

- Added function `io/truncate-from-start-keep-lines` to truncate text files honoring
  complete lines
- Added function `cron/schedule-at-round-times-in-day`. A scheduler that is not prone
  to clock shifts/drifts
- Added function `cron/schedule-at-fixed-rate`. A scheduler that is not prone
  to clock shifts/drifts
- Added function `cron/schedule-at`. A scheduler that is not prone to clock shifts/drifts
- Added a logger with console and file handlers. The file handlers support a max
  size and daily/monthly file rotation. The scheduler for file rotation handling 
  is not prone to clock shifts/drifts.

### Updated dependencies:

- Bumped the Aviron (ClamAV client) library to V1.9.1



## [1.12.53] - 2025-08-13

### Enhancements

- Added the option to pass a 'fswatch' monitor to the file watcher on MacOS.

### Updated dependencies:

- Migrated the :aviron module to the Aviron library to V1.9.0
- Bumped the Aviron (ClamAV client) library to V1.9.0



## [1.12.52] - 2025-07-30

### Enhancements

- Added the new Aviron quarantine features to the :aviron module
- Added the new Aviron dynamic CPU limit features to the :aviron module
- Added the new Aviron Clamd CPU limiter features to the :aviron module

### Updated dependencies:

- Bumped the Aviron (ClamAV client) library to V 1.5.3
- Bumped VAVR from 0.10.6 to 0.10.7



## [1.12.51] - 2025-07-21

### Enhancements

- Added the newest Aviron features to the :aviron module

### Updated dependencies:

- Bumped the Aviron (ClamAV client) library to V 1.3.3



## [1.12.50] - 2025-07-16

### Bugs

- Fixed a function argument passing problem with the `scan-path` function 
  of the :aviron module

### Updated dependencies:

- Bumped the Aviron (ClamAV client) library to V 1.3.0



## [1.12.49] - 2025-07-12

### Enhancements

- Added function `http-client-j8/create-authorization-bearer` and 
  `http-client-j8/build-url`
- Added function `pdf/extract-urls` to extract all URLs from a PDF
- Added functions `sh/pgrep` and `sh/pargs` to simplify scripts dealing
  with process pids and process command lines

### Bugs

- Fixed an edge case in the `sh` function dealing with binary shell command 
  output streams.



## [1.12.48] - 2025-06-13

### Enhancements

- Added explicit URL query parameter and fragment support for the HTTP Client. 
  Parameters and fragments are encoded appropriately.

### Bugs

- Fixed Maven download URL to "https://archive.apache.org/dist/maven/"
  instead of "https://dlcdn.apache.org/maven/maven-3/". Maven changed 
  the latter URL to provide the current version only.
  This unfortunately broke the Venice setup in demo mode and the 
  installation of additional 3rd party libs.



## [1.12.47] - 2025-06-04

### Enhancements

- Changed the publishing to Sonatype's Central Maven repository. The publishing
  process uses now more functionality from the 'maven-publish' plugin to
  create the Maven bundle and uploads the bundle explicitly via Sontatype's
  publisher REST api.



## [1.12.46] - 2025-05-31

### Enhancements

- Changed the Maven publish process for optional auto deployment to Sonatype's
  new Central Maven repository.



## [1.12.45] - 2025-05-29

### Enhancements

- Added support for static and dynamic Java MBeans. See :mbean module
- Updated the Maven publishing process by migrating to Sonatype’s new Central Maven
  repository, as the old OSSRH server was retired on June 30, 2025. Only a
  few viable alternatives were available. The Maven Publish plugin’s bridge
  mode did not work, Vanniktech’s plugin is incompatible with Java 8, and
  JReleaser is complex — especially when working with shadow JARs. As a 
  result, the “bare metal” approach was chosen for its simplicity and the 
  added benefit of avoiding credential disclosure to third-party components.
  


## [1.12.44] - 2025-05-09

### Enhancements

- Added :aviron module, a ClamAV client

### Bugs

- Fixed a bug in the file watcher regarding the detection of new sub directories



## [1.12.43] - 2025-05-01

### Enhancements

- Added function `io/exists?` to check for files or directories
- Added functions `shell/get-process-pids` and `shell/exists-process-with-pid?`
- Added module `:ascii-charts` to render bar charts in the terminal



## [1.12.42] - 2025-04-24

### Enhancements 

- Improved the file runner (launcher) not to print 'nil' to stdout if the 
  result was just `nil` (cosmetic change).
- Added :stopwatch module for time measuring



## [1.12.41] - 2025-04-18

### Enhancements

- Added function `time/between`
- Enhanced file watcher functions



## [1.12.40] - 2025-04-09

### Enhancements

- Added function `shell/nice`
- Added function `index-of` and `last-index-of` for sequences



## [1.12.39] - 2025-03-23

### Enhancements

- Added clear screen command to the REPL
- Added :openai module support for 'me' endpoint

### Updated dependencies:

- Bumped jtokkit lib in the :jtokkit module to V1.1.0
- Bumped bumped xchart lib in the :chart module to V3.8.8



## [1.12.38] - 2025-02-20

### Enhancements

- Added support for structured addresses to :qrbill module. Please note that
  starting November 25, 2025, Swiss banks will no longer accept payments using 
  combined address elements
- Added support for `fit-to-page` in the excel print setup

### Updated dependencies:

- Bumped VAVR from 0.10.4 to 0.10.6

    

## [1.12.37] - 2025-02-12

### Enhancements

- Added :zipvault support for the functions `root-folder-name-in-zip` and 
  `file-name-in-zip` for more flexibility adding files to a zip
- Added module :qrcode for encoding and decoding QR code images



## [1.12.36] - 2025-02-07

### Enhancements

- Added :excel module layout functions (print-layout, page-margins, 
  header-margin, footer-margin, header, and footer)
- Added documentation for Venice Shebang scripts in the cheatsheet
- Changed :excel module to not support for Apache POI 4.x anymore 
  (please use POI 5.x)
- Re-enabled GitHub Code Scanning workflow. GitHub deactivated it after 60
  days of inactivity

### Updated dependencies:

- Bumped Apache POI from 5.3.0 to 5.4.0



## [1.12.35] - 2024-12-04

### Enhancements

- Added module :keystores to deal with certificates (PKCS12, ...)



## [1.12.34] - 2024-10-10

### Enhancements

- Added function `add-url-hyperlink` to :excel module
- Added function `add-email-hyperlink` to :excel module
- Added function `remove-hyperlink` to :excel module
- Added function `remove-formula` to :excel module
- Added function `cell-lock` to :excel module
- Changed the Tomcat download URL in the :tomcat-util module to use HTTPS 
  instead of HTTP. The new official archive URL is: 
  `https://archive.apache.org/dist/tomcat`
- Improved the module :qrref that manages QR references according to the 
  Swiss payment standards.



## [1.12.33] - 2024-09-05

### Enhancements

- Added function `add-conditional-bg-color` to :excel module
- Added function `add-conditional-font-color` to :excel module
- Added function `add-conditional-border` to :excel module
- Added function `add-text-data-validation` to :excel module

### Bugs

- Fixed a bug in the Venice PDF cheatsheet generation where the doc 
  section header was  not rendered in the right doc section column.

### Updated dependencies:

- Bumped Apache POI from 5.2.3 to 5.3.0



## [1.12.32] - 2024-08-29

### Bugs

- Fixed a bug in the :excel module when reading values 
 `(excel/read-val sheet row col)` from string cells with formulas and the 
 cell's value was `nil`.



## [1.12.31] - 2024-08-27

### Enhancements

- Added function `delete-row` to :excel module to delete a row in a sheet
- Added function `copy-row` to :excel module to copy a row to another row in a sheet
- Added function `copy-row-to-end` to :excel module to copy a row to the end of a sheet
- Added function `insert-empty-row` to :excel module to insert an empty row in a sheet
- Added function `clear-row` to :excel module to clear a row's cells in a sheet



## [1.12.30] - 2024-08-16

### Bugs

- Fixed a bug in the Venice REPL setup on Windows with canonical path conversion.



## [1.12.29] - 2024-08-16

### Enhancements

- Added support for an explicit install directory (other than the current working 
  directory) to the REPL installer 
- Added better error messages to the REPL installer 

### Updated dependencies:

- Bumped Gradle from 8.3 to 8.9



## [1.12.28] - 2024-08-15

### Enhancements

- Added an unattended setup option for installing the Venice REPL. 
  This allows for automated setups or unit testing the setup.
- Changed the Gradle shadow plugin.
  The Gradle plugin "io.github.goooler.shadow" has been retired now as well.
  Migrated to "com.gradleup.shadow" (see https://github.com/GradleUp/shadow).
  This seems to be the final new home for the retired 
  "com.github.johnrengelman.shadow" plugin.
- Changed the Gradle CI workflow to build and test on Ubuntu and Windows using 
  JDK 8, 17 and 21.



## [1.12.27] - 2024-08-05

### Bugs

- Fixed the MeterRegistry. On M2 Macs some events reported with 0ns elapsed time.
  These 0ns events have been skipped resulting in a wrong profiler event
  count under heavy load.



## [1.12.26] - 2024-07-26

### Enhancements

- Added 32-bit `float` data type that complements the 64-bit `double` data type
- Added functions `java-float-list`, `java-double-list`, and `java-long-list` to support LLM
  embedding vectors with vector databases like *Pinecone* or *Qdrant*
- Added support for shebang lines. Venice allows shebang lines simply by implementing 
  `#!` as a reader macro defining a comment like  `;`
- Added support for `#_` reader macro to skip forms



## [1.12.25] - 2024-06-12

### Enhancements

- Added :openai module support for chat completion streaming usage
- Added :openai module support for embedding api
- Changed the Gradle shadow plugin.
  The Gradle plugin "com.github.johnrengelman.shadow" has been
  retired. See [johnrengelman/shadow#908](https://github.com/johnrengelman/shadow/issues/908). 
  Switched to the fork "io.github.goooler.shadow"



## [1.12.24] - 2024-05-23

### Enhancements

- Changed the OpenAI client api slightly to allow the integration of the 
  assistants functionality.
  Use the actual OpenAI examples from the README with Venice 1.2.24+!



## [1.12.23] - 2024-05-22

### Enhancements

- Added :openai module support for audio speech api
- Added :openai module support for files api
- Added :openai module support for models api
- Added OpenAI vision examples



## [1.12.22] - 2024-05-20

### Enhancements

- Added the Venice logo to the cheatsheet
- Added :openai module support for image creation
- Added :openai module support for image variants
- Added :openai module support for image edits



## [1.12.21] - 2024-05-15

Google changed the download URL for fonts from *Google Fonts* again causing all
font examples to fail. Moved to [Font Squirrel](https://www.fontsquirrel.com/).


### Changed

- the *True Type* font source from *Google Fonts* to 
  [Font Squirrel](https://www.fontsquirrel.com/).

  The PDF font examples now use "Opens Sans", "Roboto", "Source Code Pro" 
  and "JetBrains Mono" fonts from the *Font Squirrel* repository.
  
  The :fonts module has been updated to download these fonts from *Font Squirrel*. 
  They are available under the Apache License v2 or the SIL Open Font License v1.10.



## [1.12.20] - 2024-05-14

### Fixed

- a bug in the Venice setup that crept into the last 1.12.19 release



## [1.12.19] - 2024-05-12

### Added

- many new imaging functions to the `:images` module
- function `fn-args` to return meta data on a functions' arguments.

### Fixed

- 'repl.sh' and 'repl.bat' to source the 'repl.env' within the restart loop



## [1.12.18] - 2024-05-06

### Added

- function `openai/assert-response-http-ok` to simplify error handling
  in the OpenAI examples



## [1.12.17] - 2024-05-06

### Added

- :openai module with chat completion support (incubation status).
  The openai module runs out-of-the-box with Venice, it does not
  require any 3rd party libraries.
  Support for *Audio*, *Images*, *Embeddings*, and *Assistants* will
  follow soon.
- :jtokkit module, bringing the Java JTokkit library functionality to 
  Venice

### Improved 

- JDBC query result table renderer uses now right alignment for numeric columns

### Fixed

- fixed a bug in the HTTP client when slurping a binary data response



## [1.12.16] - 2024-04-30

### Added

- JDBC support through the modules `:jdbc-core` and `:jdbc-postgresql`
- `:cargo-postgresql` module to install/start/stop PostgreSQL DB docker 
  containers 
- `:postgresql-jdbc-install` module to install the PostgreSQL JDBC driver

### Fixed

- a problem with starting an already running docker container.
  Venice is doing nothing if the container version is ok, otherwise
  it stops the container and runs a new one with the desired version.



## [1.12.15] - 2024-04-27

### Added

- support for sourcing environment vars in the Windows REPL start script

### Improved 

- the REPL setup. Maven will now be installed locally to the REPL to allow 
  on demand installation of modules like Tomcat, LangChain4J, ... with its
  dependencies.

### Fixed

- the REPL setup for *Gitpod.io* instances regarding the sourcing of environment 
  variables from *repl.env*.



## [1.12.14] - 2024-04-16

### Added

- functions `get-request-parameter`, and `get-request-long-parameter` to 
  :ring-util module
- more HTTP status codes with short names and descriptions
  
### Improved 

- SSE WebApp example to demonstrate explicit connection closing on the 
  server side
- REST WebApp example to demonstrate the use of query parameters

### Fixed 

- HTTP client SSL requests



## [1.12.13] - 2024-04-14

### Added

- custom type support for Venice persistent collections
- more options to slurp a HTTP client's JSON response

### Fixed 

- the HTTP client to gracefully react on interrupted exceptions and close 
  the streams
- the HTTP client debug mode to print authorization header values as "******"
- the `:ring` debug mode to print authorization header values as "******"
- an edge case with function `str/equals-ignore-case?`



## [1.12.12] - 2024-04-11

### Added

- options `:async-support` and `:load-on-startup` to `tomcat/tc-start` 
  servlet options
- `:ring` support for asynchronous handlers. See example "async-webapp.venice".
- `:ring` support for Server-Side-Events. See example "sse-webapp.venice".
- content encoding (gzip, deflate) support for http client response processing
- support for server-side-events processing in the http client



## [1.12.11] - 2024-04-08

### Added

- support for multiple url mappings for a servlet in the `:ring` module. 
  See example "rest-webapp.venice".
- a `:ring` example with a 2 servlet configuration. See example 
  "two-servlet-webapp.venice".
- the effective url (the url after a redirect) to the response map in the
  http client
- http status code helpers:
     *  `http-client-j8/status-ok-range?` 
     *  `http-client-j8/status-redirect-range?` 
     *  `http-client-j8/status-client-range?` 
     *  `http-client-j8/status-server-error-range?` 

### Changed

- the http client to provide the name of the HTTP status code in the response 
  map. E.g.: `HTTP_OK` for status code 200



## [1.12.10] - 2024-04-07

### Added

- support for module `:http-client-j8` to *ungzip* a response data stream
  with the content encoding 'gzip'  
- function `io/wrap-is-with-gzip-input-stream` to gzip data sent to the stream
- function `io/wrap-os-with-gzip-output-stream` to ungzip data read from the stream
- improvements to the `:multipart` parser. The parsers is now more robust in
  parsing the part headers and returns all part headers in its raw form in
  parsed data map.

### Fixed 

- the multipart parser's mimetype handling for content type headers



## [1.12.9] - 2024-04-06

### Added

- module `:http-client-j8`. A handy HTTP client based on the JDK HttpUrlConnection.
  The HttpClient can be used to send requests and retrieve their responses. It supports
  sending multi-part requests. The HTTP client does not depend on 3rd party libraries!
  There are REST and file upload examples in the Venice script examples directory.
- module `:multipart` that supports the subtype `multipart/form-data`. Renders and 
  parses multi-part binary buffers
- support for multi-part requests to `:ring` module and improved the request/response
  dump functions
- function `bytebuf-merge` to merge byte buffers
- function `bytebuf-index-of` for searching patterns in byte buffers using 
  the Knuth-Morris-Pratt (KMP) pattern matching algorithm

### Fixed 
- the return status handling in the `:ring` module



## [1.12.8] - 2024-04-02

### Added

- function `io/file-normalize-utf`. This is useful to make it easier to process
  MacOS filenames with umlauts.
- thread type support to the function `thread` to allow spawning *daemon* or *user* 
  threads
- support for tracing exceptions in `trace/trace-var`
- math function `rand-bigint`. Venice uses this function to create unique
  boundary strings for multipart HTTP requests.
- function `mimetypes/probe-content-type` to get mimetypes for file names.



## [1.12.7] - 2024-03-26

### Added

- function `str/normalize-utf`. This is useful to make it easier to process
  MacOS filenames with umlauts.
- a pure ascii version of the Venice cheatsheet to feed it as embeddings 
  to LLMs
- improvements on to markdown to ascii text renderer to allow the Venice
  Github flavoured markdown documentation to be converted to pure ascii
  to feed it as embeddings to LLMs
- `:cargo-qdrant` module to install/start/stop Qdrant Vector DB docker 
  containers 
  
### Changed

- the `:docker` module to support now multiple port publish definitions

### Fixed 
- a markdown parser edge case when a table block is immediately followed 
  by a list block with no empty line in between



## [1.12.6] - 2024-03-19

### Added

- function `zip-folder` to `zipvault`
- support italic font style in the :ansi module

### Changed

- the function `maven/download` to accept a single artifact as well as a
  list of multiple artifacts

### Fixed 

- :maven module (a bug has been added with V1.12.0 refactoring)



## [1.12.5] - 2024-03-13

### Added

- math function `clamp`
- basic authentication support for `io/download`
- follow redirects supports  for `io/download`, if the protocol does not change 
  for the requested redirect
  
### Changed

- the **geoip** module to use basic authentication to access the MaxMind databases.
  This is mandatory starting from May 1st, 2024 with MaxMind's effort to 
  improve security and reliability on its services.



## [1.12.4] - 2024-03-10

### Added

- Java interop function `enum?`

### Improved 

- support for Java enums
- Java interop documentation on enums



## [1.12.3] - 2024-03-05

### Added

- functions to hide columns in an excel sheet

### Refactored

- the global vars "\*REPL\*" and "\*repl-color-theme\*". Dedicated REPL functions like 
  `(repl/color-theme)` provide now access to the REPL configuration. 
  This affects only the internal REPL scripts.



## [1.12.2] - 2024-01-16

This release focuses on Windows.

### Improved 

- and simplified the setup of custom REPLs

### Fixed 

- all unit tests to run on Windows too
- a path composition problem in the :gradlew module when running on Windows
- :grep module to run on Windows
- an edge case in load-path management on Windows
- REPL terminal width/height functions on Windows



## [1.12.1] - 2024-01-12

### Added 

- function `io/file-set-readable`
- function `io/file-set-writable`
- function `io/file-set-executable`
- function `io/copy-files-glob`
- function `io/copy-file-tree`
- function `io/move-files-glob`
- function `io/symbolic-link?`
- function `io/create-symbolic-link`
- function `io/create-hard-link`



## [1.12.0] - 2024-01-07

### Added 

- an :installer module to simplify the installation of 3rdparty libraries
  from within the REPL and to simplify the REPL setup process
- improvements to REPL demo fonts. See the font demo in the 
  [PDF readme](doc/readme/pdf.md#fonts) chapter.
- an optional force flag to maven/download

### Fixed 

- documentation for :matrix module

### Updated dependencies:

- Bump flying-saucer from 9.3.2 to 9.4.0
- Bump xchart from 3.8.2 to 3.8.6
- Bump pdfbox from 2.0.27 to 3.0.1



## [1.11.3] - 2023-12-31

### Added 

- the function `maven/mvn` to run a maven command
- the function `maven/dependencies` to get the dependency tree of an
  artifact (e.g.: "org.knowm.xchart:xchart:3.8."). Venice creates a 
  temporary maven project to compute the dependency tree and removes 
  the project afterwards

### Updated dependencies:

- Bump flying-saucer from 9.3.1 to 9.3.2
- Bump openpdf from 1.3.32 to 1.3.35



## [1.11.2] - 2023-12-23

### Fixed 

- the availability detection of ChaCh20 and ChaCha20-BC file encryption 
  algorithms on all setup combinations Java 8 / Java 11+ with or without
  the BouncyCastle libraries



## [1.11.1] - 2023-12-22

### Added 

- documentation 



## [1.11.0] - 2023-12-21

### Added 

- module :ascii-table for creating and customizing simple ASCII tables

### Refactored

- :core module to use qualified core functions within macros to follow
  the "principal of least surprise". 



## [1.10.56] - 2023-12-12

### Added 

- AES-256 (CBC, PKCS5Padding) file encryptor/decryptor to :crypt
  module (for testing purposes only, in production use AES-256 GCM)
- ChaCha20 file encryptor/decryptor to :crypt module (Java 11+)
- ChaCha20 BouncyCastle file encryptor/decryptor to :crypt modul (Java 8+)
- functions for handling byte order (little/big endian) on byte buffers

### Disabled 

- external entities in XML parser utils



## [1.10.55] - 2023-12-10

### Added 

- function `str/align`
- bytebuf allocation with random values
- bytebuf allocation with a fill value
- an example script comparing AES-256 CBC, AES-256 GCM, and AES-256 ZIP
  encryption and decryption performance
- an example script comparing MD5, SHA-1, SHA-256 hashing performance



## [1.10.54] - 2023-12-07

### Added 

- AES-256 (GCM, NoPadding) file encryptor/decryptor to :crypt
  module



## [1.10.53] - 2023-11-13

### Added 

- improvements to :zipvault module. New functions `zipvault/entries`
  `zipvault/add-file`, `zipvault/add-folder`, `zipvault/add-stream`



## [1.10.52] - 2023-11-09

### Added 

- support for Docker ArangoDB dump & restore in the :cargo-arangodb module
- support for copying Docker ArangoDB dumps to/from the local file system
- support for 'docker/exec' detached/non detached (async/sync) mode
- module :zipvault to create AES-256 encrypted and password protected zip files 
  that can be opened by most unzip/uncompress tools on MacOS, Linux, or Windows

### Fixed 

- Fixed 'docker/exec' function. The docker tool expects the exec command and
  its args as individual arguments: `docker exec 0286eb877a91 ls /var/lib`.  
  Passing "ls /var/lib" is not accepted.
- option processing in 'io/file-out-stream' function
  


## [1.10.51] - 2023-11-05

### Added 

- support for docker volumes in `cargo/start` function 

### Fixed 

- an edge case with handling envs and args in `docker/run` function



## [1.10.50] - 2023-11-03

### Added 

- an additional check to the CSV reader to reject quote chars in non quoted 
  fields. This is not allowed by the CSV standard
- function `finder` to find symbols

### Fixed 

- an edge case with CSV reader on a sequence of empty fields 



## [1.10.49] - 2023-10-30

### Added 

- jsonl/splitln function
- io/print-line support for a single stream argument

### Improved 

- docker module to use :jsonl module to parse JSON command output



## [1.10.48] - 2023-10-22

### Added 

- improvements to simplify the time function's support for ISO formats
- support for full decimal number range with *JSON* read/write. Reading and writing
  of decimals like `99999999999999999999999999999999999999999999999999.3333333333333333M` 
  in its full precision is now possible. This feature must be activated explicitly
  because its not part of the JSON standard.
  (Note: the JSON standard itself is limited to the double floating-point number 
  range and precision)



## [1.10.47] - 2023-10-19

### Added 

- function `io/print-line`
- an optional filter function to `jsonl/slurp`
- support for lazy sequences and transducer to JSON Lines data slurper

### Improved 

- performance of JSON Lines `jsonl/spit` function
- JSON Lines documentation



## [1.10.46] - 2023-10-17

### Added 

- support for [JSON Lines](https://jsonlines.org/) (see module :jsonl)

### Fixed 

- function docker/container-exec-by-name 
- function read-line to detect end-of-stream properly



## [1.10.45] - 2023-10-04

### Added 

- functions to handle locks based on a semaphore

### Updated 

- :cargo-arangodb module to disable the ArangodDB telemetrics sent home

### Fixed 

- Venice to follow the Java rules when propagating exceptions from 
  try-with-resources blocks:
    1. exception from finally block
    2. exception from catch block
    3. exception from body block
    4. exception from resource auto-close

### Updated 

- :pdf module dependencies to flyingSaucer 9.3.1 and openpdf 1.3.30

### Updated dependencies:

- Bump gradle from 8.0.2 to 8.3



## [1.10.44] - 2023-09-27

### Added 

- function `running?` and `prune` to :cargo module

### Improved 

- error messages for the :cargo and :docker module



## [1.10.43] - 2023-09-23

### Added 

- module :cargo to run testcontainers with the support of Venice



## [1.10.42] - 2023-09-18

### Added 

- function argument type hints
- an optional timeout to `sh` function


### Enhanced 

- the functions `time/plus` and `time/minus` to accept `:java.time.Period` and `:java.time.Duration` too as the amount of time to add or subtract

### Fixed 

- function `docker/rm`



## [1.10.41] - 2023-09-12

### Added 

- a no arg function variant to `crypt/ciphers`
- a `docker` module to manage docker images and containers from Venice scripts. The functions support both TEXT and JSON output of the docker commands. The JSON output is parsed and converted to Venice data for further processing. 
A generic docker function can run any docker command.  
Requires a local docker installation.  
Venice supports the most used docker commands:
    - docker version, 
    - docker images, docker rmi, docker image prune/rm/pull
    - docker run/ps/start/stop/cp/exec/diff/logs/pause/unpause/wait/prune
    - docker volume list/create/rm/exists  
- functions `var-sym-meta` and `var-val-meta` to access the meta data of the symbol and of the value of a var.


### Fixed 

- unwrapping a Java object of type `Optional<T>` if `T` is a Java array type



## [1.10.40] - 2023-08-17

### Added 

- formal type support for unwrapping a Java object of type `Optional<T>` through the function `java-unwrap-optional`.
- detailed explanation of the role of Java formal types in Venice. (See the functions: `formal-type`, `cast`, and `remove-formal-type`)
- function `crypt/ciphers` to list the available ciphers

### Improved 

- documentation and examples for lazy sequences

### Fixed 

- 'str/split' doc examples



## [1.10.39] - 2023-07-10

### Added 

- UTF-8 module with common UTF-8 character constants
- function `remove-formal-type`

### Fixed 

- not working service registry lookup when any sandbox is active. The affected Java classes are white-listed as default now and the service registry sandboxing is simply controlled through black-listing the `service` function.



## [1.10.38] - 2023-07-09

### Added 

- function `str/split-columns`
- the ability to use dynamic service discovery with Venice service registry to simplify Venice embedding use cases



## [1.10.37] - 2023-04-30

### Added 

- a service registry to simplify Venice integration in application scripting scenarios

### Fixed 

- Excel module to work with Apache POI 4.1.x again. To use charts with Excel
  documents Apache POI 5.2.0 or newer is required!



## [1.10.36] - 2023-04-18

### Fixed 

- Gradle build shadow jar task. Gradle 8.0.x does not accept the `classifier` property
  anymore. Renamed to `archiveClassifier` and use `archiveClassifier = ''` to prevent
  the shadow jar task to add the '-all' classifier to the jar name.



## [1.10.35] - 2023-04-14

### Added 

- a PDF to text tool: `pdf/to-text`



## [1.10.34] - 2023-04-11

### Added 

- functions `io/filesystem-total-space`, `io/filesystem-usable-space`
- function `inet/reachable?` to check if an INET addr is reachable
- improvements to Excel renderer regarding column hiding
- support for PNG and JPEG images to `:excel` module
- support for line, bar, area, and pie charts to `:excel` module

### Fixed

- :excel module 3rd-party library dependencies

### Updated dependencies:

- Bump com.github.johnrengelman.shadow gradle plugin from 7.1.2 to 8.1.1
- Bump gradle from 7.5 to 8.0.2



## [1.10.33] - 2023-01-18

### Added 

- functions `str/nrest`, `str/butnlast`, and `str/split-at`
- a parallel reduce `preduce` based on a reduce / combine strategy
- module `:qrref`

### Fixed 

- parsifal parser combinator `let->>*` and `>>*` macros



## [1.10.32] - 2023-01-03

### Fixed 

- setup script



## [1.10.31] - 2023-01-03

### Added 

- module `:qrbill` to create Swiss QR bills
- function `io/file-basename`



## [1.10.30] - 2022-12-07

### Added 

- improvements to `:excel` module cell styling
- support for freeze panes to `:excel` module 
- function `excel/read-error-code` to read the error code from a formula cell
- function `excel/read-val` to have an option to read data from cells generically

### Improved

- and simplified the function `excel/write-data` for writing 2 dimension tabular 
  data to an excel sheet



## [1.10.29] - 2022-11-29

### Added 

- an optional start row/col to `excel/write-data`

### Fixed

- an edge case in `macroexpand-all` when the upfront macro expansion 
  meets java data structures. Runtime macro expansion works fine
  in these cases.
- function `excel/write-items` handling formulas



## [1.10.28] - 2022-11-21

### Added 

- Excel reader / writer converter

### Fixed

- `excel/sheet-index` (must be 1-based)
- Excel formula cell evaluation
- Excel vertical middle alignment



## [1.10.27] - 2022-11-14

### Added 

- functions to automate downloading 3rd party Java libraries and fonts 
  required for PDF, Excel, Chart, and Tomcat modules

### Updated dependencies:

- Bump Apache POI from 4.1.2 to 5.2.3 (compatibility with POI 4.1.2 is 
  still provided)



## [1.10.26] - 2022-11-11

### Added

- the function 'excel/sheet-index' to get an excel sheet's index
- a HTML and PDF renderer for syntax highlighted Venice source files

### Fixed

- 'excel/open' when passing a bytebuf



## [1.10.25] - 2022-11-06

### Added

- support for interfaces with default implementations that are not
  overridden for Java  interop dynamic proxies 
- macros `assert-ne` and `assert-does-not-throw`
- function `qualified-symbol?`, `regex/matches-not?`, `io/file-within-dir?`

### Improved

- functions `match?` and `not-match?` to support also regex patterns 
  (:java.util.regex.Pattern)

### Fixed

- REPL command completer to consider special forms as well 

### Migrated

- the *Tomcat* WebApp modules `:tomcat` and `:ring` to the Java EE 9 Jakarta
  versions of *Tomcat*. Tomcat 9 is not supported anymore. Use Tomcat 10.0.x
  with Java 8 and Tomcat 10.1.x with Java 11+.



## [1.10.24] - 2022-10-03

### Added

- support for destructuring in `loop / recur` loops
- an optional customized indentation string to the JSON pretty printer
- proxy builder for Java functional UnaryPredicate interface
- macros `assert-eq` and `assert-throws`
- file, line, and column information to the exceptions raised by the assert 
  macros `assert`, `assert-eq`, and `assert-throws`
- :com.github.jlangch.venice.AssertionException to the list of the 
  default class imports
- a test framework

### Improved

- `str/split` to support an optional limit

### Fixed

- proxy builder for Java functional BiPredicate and BiConsumer interfaces 



## [1.10.23] - 2022-09-14

### Added

- optimizations to pre-compiled execution

### Improved

- sandbox construction

### Fixed

- a cheatsheet example



## [1.10.22] - 2022-09-06

### Improved

- pre-compiled scripts to reach the performance level of _Venice_ V1.10.0 
  scripts again.

### Fixed

- pre-compiled scripts to support namespace aliases for loaded modules and 
  files.

- pre-compiled scripts to support macro expansion for loaded modules and 
  files

### Incompatible Changes

- renamed the public class 'com.github.jlangch.venice.PreCompiled' to 
  'com.github.jlangch.venice.IPreCompiled' to pave the way for Java modules.



## [1.10.21] - 2022-09-05

### Added

- load path support for `io/delete-file`

### Improved

- CSV and JSON to use the I/O module to handle its file I/O.   
  This way CSV and JSON can be controlled by the sandbox's I/O 
  configuration

- the support for data sources and sinks CSV and JSON functions can handle
  to slurp or spit data.



## [1.10.20] - 2022-09-02

### Added

- function `partition-all`
- load path support for `io/slurp`, `io/spit`, `io/file-in-stream`, and
  `io/file-out-stream`

### Improved

- the sandbox's control over `load-classpath-file`. Instead of just being able to
  allow or reject calls to `load-classpath-file` the sandbox offers now a finer 
  control over the function by applying the classpath access rules on the 
  resource being loaded. 
  The sandbox configuration provides now three options:
    * no restrictions at all
    * reject all calls to `load-classpath-file`
    * accept calls to `load-classpath-file` based on the classpath resource 
  
- the core module to not rely on Java interop anymore. The core module
  can now be used without restrictions when Java interop is disabled.  

### Fixed

- REPL to add expressions with leading spaces to the history too by overriding
  `jline3`'s default configuration

- Venice to not wrap a `SecurityException` with a `VncException` on sandbox 
  violations.



## [1.10.18] - 2022-08-02

### Added

- better documentation for `load-file` and `load-resource` regarding load
  paths

### Fixed

- load path access, ZIP files on the load path are now fully supported



## [1.10.17] - 2022-07-29

### Added

- functions `io/touch-file`, `io/delete-files-glob`
- support for queues with transducers and reducers
- performance improvements to the `grep` functions by using `pmap` instead of 
  `map` in its implementation.
- a `delay-queue` that is based on the Java class `java.util.concurrent.DelayQueue`
- reader macro for regex patterns: #"[0-9]+"

### Fixed

- the message of the exceptions raised by `str/format` on illegal formats. 
  Additionally provide a Venice stack trace with these exceptions.

### Security

- Fix Partial Path Traversal Vulnerability submitted by [Jonathan Leitschuh](https://github.com/JLLeitschuh)

### Updated dependencies:

- Bump gradle from 7.4.1 to 7.5



## [1.10.16] - 2022-07-01

### Added

- support for import aliases `(import :java.awt.Color :as :Col)`
  `(import :java.awt.Color)` is equivalent to `(import :java.awt.Color :as :Color)`
- support for regex rematching in function `regex/matches?`
- a *grep* module with grep like search for lines in text

### Changed

- the implementation of the special forms. It's now easier to add and document
  special forms
- all tabs to spaces in the sources

### Fixed

- `io/delete-file-on-exit` to work with directories too
- a bug in the markdown table renderer with parsing custom column styles



## [1.10.15] - 2022-06-02

### Fixed

- color definitions in :xchart module.



## [1.10.14] - 2022-06-01

### Fixed

- JavaInterop to run on Java 17. All unit tests run now on Java 8, 11,
  and 17.

### Changed

- the 'component' extension module to work with vanilla Venice values 
  that do not implement the Component protocol. This allows for a smoother 
  integration with configurations created by the 'config' module.
- refactored the 'component' extension module to store a component's 
  dependencies in the component's meta data.
- the namespaces of the extension modules 'tomcat', 'tomcat-util', and
  'benchmark' to follow the module name. 
  Using namespace aliases, introduced with Venice 1.10.11, helps a lot 
  in working with qualified symbols.



## [1.10.13] - 2022-05-19

### Fixed

-  fixed the '-setup-ext' option in the  _repl-setup.venice_  script 
   that is used to install Venice with additional fonts and 3rdparty 
   libraries   
   `shell> java -jar venice-*.jar -setup -colors`   
   `shell> java -jar venice-*.jar -setup-ext -colors`



## [1.10.12] - 2022-05-18

### Added

- function `str/hexdigit?`
- CSS column styles for markdown tables. Currently 'text-align' and
  'width' are supported
- new features to the _Parsifal_ parser combinator:
    1. Deduping the error message list, to avoid repeated error messages
    2. Allow customized error messages with the `never` parser
    3. Added protocol 'SourcePosition' and changed function `inc-sourcepos` 
       to support error messages with source line/column nr for item types
       other than `char`.
    4. A parser for any token: `any`
    5. A parser for hex digits: `hexdigit`

### Changed

- the `load-module`, `load-file`, and `load-classpath-file` macros to 
  accept an optional ns alias
- the name of the parser combinator to _Parsifal_ 

### Updated dependencies:

- Bump openpdf from 1.3.27 to 1.3.28



## [1.10.11] - 2022-05-05

### Added

- namespace aliases (`ns-alias`, `ns-aliases`, `ns-unalias`)
- `Object` protocol to support customized `compareTo` function for custom datatypes
- a _Parsifal_ expression parser example _'doc/example/scripts/expr-parser.venice'_
- functions `str/trim-left` and `str/trim-right`

### Changed

- the printing of 'Infinite' and 'NaN' double numbers in the reader format for
  the functions `pr-str`, `pr`, and `prn`. 

### Fixed
- function `name` to return the simple name only (without namespace)



## [1.10.10] - 2022-05-01

### Added

- support for char literals: `#\A`, `#\u03C0`, `#\space` (implemented as reader macro)
- support for multiple collections to function `pmap` 
- macro `letfn` and `def-`
- function `deliver-ex` to complete promises exceptionally by delivering an exception
- functions `prewalk-replace` and `postwalk-replace` (recursive replace)
- functions `pr` and `prn` that round out the printing functions's support 
  for the Venice's reader format

### Fixed
- a problem with the functions `io/string-in-stream` and `io/bytebuf-in-stream` not 
  being accessible through its global symbols
- an edge case with dynamic vars propagation across threads



## [1.10.9] - 2022-04-01

### Added

- functions `select-keys`
- functions `pmap` and `pcalls`

### Updated dependencies:

- Bump openpdf from 1.3.26 to 1.3.27
- Bump gradle from 7.2 to 7.4.1



## [1.10.8] - 2022-03-02

### Added

- math functions `exp`, `asin`, `acos`, `atan`
- functions `nan?` and `infinite?` for double values
- `Object` protocol to support customized 'toString' conversion for custom datatypes
- improvements to documentation

### Updated dependencies:

- Bump jline3 from 3.20.0 to 3.21.0
- Bump com.github.johnrengelman.shadow gradle plugin from 7.1.0 to 7.1.2
- Bump junit-jupiter-api from 5.7.0 to 5.8.2
- Bump junit-jupiter-engine from 5.7.0 to 5.8.2



## [1.10.7] - 2022-02-01

### Added

- functions `user-name`, `io/file-in-stream`, `io/string-in-stream`



## [1.10.6] - 2022-01-02

### Added

- timeout functions for promise chaining



## [1.10.5] - 2021-12-01

### Added

- function `future-task` that offers an alternative processing model for 
  asynchronous tasks
- functions `drop-last`, `take-last`
- chaining async tasks for promises
- support for `done?`, `cancel`, `cancelled?` to promises



## [1.10.4] - 2021-10-28

### Added

- functions `cartesian-product` and `compositions`
- functions `subset?` and `superset?` for sets
- support for running app archives from the REPL

### Fixed

- application archive runner

### Updated dependencies:

- Bump jline3 from 3.20.0 to 3.21.0



## [1.10.3] - 2021-10-10

### Added

- support for isolated root components that have no dependencies for the 
  `:component` module
 
- support for protocol default function implementations



## [1.10.2] - 2021-10-08

### Added

- support for `doc` function to print the functions of protocols and 
  to print the associated protocols for custom types.

### Fixed

- `assoc` on custom types to keep the meta data



## [1.10.1] - 2021-10-06

### Added

- module :component
- function `str/levenshtein`

### Changed

- `doc` function to use *Levenshtein distance* if the exact symbol is not found 
  to do a fuzzy search for candidates.  

### Fixed

- a namespace problem with protocols



## [1.10.0] - 2021-10-01

### Added

- a DAG (directed acyclic graph) data type with topological sorting using 
  [Kahn's algorithm](https://en.wikipedia.org/wiki/Topological_sorting)
- function `deftype-describe` to get details on a custom type definition
- [Protocols](doc/readme/multimethods-and-protocols.md#Protocols) (`defprotocol`, `extend`)

### Fixed

- an edge case with `core` namespace. E.g: `(do (ns foo) (defn foo/*  [x y] (core/* x y 2)))`



## [1.9.31] - 2021-09-21

### Added

- functions `merge-deep`, `io/->url`, `io/->uri`, 
- the module `:config` to simplify application configuration
- support for `java.net.URL` and `java.net.URI` with `io/slurp` and `io/slurp-lines`
- improvements to cheatsheet rendering for inline code sections
- multimethods to support `isa?` semantic when dispatching on data types



## [1.9.30] - 2021-09-11

### Added

- improvements to callstack when threads are used

### Fixed

- the HTML cheatsheet link in the README. Prepended the Markdown URL with
  `https://htmlpreview.github.io/?` to make it work again.
- generated HTML cheatsheet to pass the W3C *Markup Validation Service* without 
  warnings and errors
- the isolation of the try-catch-finally local vars against each other
- arity exceptions to carry a callstack



## [1.9.29] - 2021-09-02

### Added

- a custom mode to `ansi/progress`

### Fixed

- `ns-remove` to not allow the removal of the current namespace
- an edge case with special form `ns-list` 
- `io/watch-dir` when the optional error and termination listener are not passed



## [1.9.28] - 2021-08-14

### Fixed

- :ansi module progress bar percentage to be converted to an integer always thus
  preventing the progress bar from displaying floats and decimals



## [1.9.27] - 2021-08-01

### Fixed

- import in module :benchmark
- an edge case with destructuring in binding special form
  `(binding [bindings*] exprs*)`



## [1.9.26] - 2021-07-21

### Added

- a Venice `com.github.jlangch.venice.SecurityException` that is thrown by the 
  sandbox instead of a `java.lang.SecurityException` to indicate a sandbox 
  violation. This allows Venice to pass a more user friendly Venice stacktrace 
  with the exception
- support for nillable types with `deftype`
- tee functions to the tracing module

### Fixed

- `CapturingPrintStream` to use the system-dependent line separator string.

### Changed

- VAVR dependency to version 0.10.4



## [1.9.25] - 2021-07-12

### Added

- function `vector*`
- optimizations to global var lookup
- callstack info to error messages where it was missing



## [1.9.24] - 2021-07-02

### Added

- support for _Gitpod VS Code_

- color mode switching to the REPL. The REPL commands `!lightmode` and
  `!darkmode` switch Venice's color mode to adapt to the REPL's 
  terminal color mode.
  
- VS code settings for Venice source code syntax highlighting for editing
  on _Gitpod_

### Fixed

- destructuring with lazy sequences. All destructuring features are now
  supported with lazy sequences.



## [1.9.23] - 2021-06-23

### Added

- added an exception catch selector for exception cause types 
  in try-catch blocks

### Fixed

- a markdown rendering problem for text mode with list items



## [1.9.22] - 2021-06-08

### Added

- exception selectors for `catch` clauses in try-catch blocks
- utility functions to access message, cause, and stacktraces
  of an exception

### Fixed

- a problem with the `ValueException` class regarding introducing Java modules



## [1.9.21] - 2021-06-04

### Added

- improvements to the exception handling. The new function `ex` simplifies 
  the creation of exceptions and plays well with the full restricted 
  sandbox. E.g.: `(throw (ex :VncException "test"))`



## [1.9.20] - 2021-06-02

### Added

- improvements to the online and cheatsheet documentation.
- prevention of redirecting stdin, stdout, and stderr streams explicitly. Any
  redirection attempt by changing the thread local vars (:*in*, :*out*, :*err*) throws
  an exception.



## [1.9.19] - 2021-05-30

### Added

- access to REPL info when running in the REPL. 
  E.g.: `(repl/info)`, `(repl/term-rows)`, `(repl/term-cols)`


### Changed

- JLine dependency to version 3.19.0
- OpenPDf dependency to version 1.3.26
- JAnsi dependency to version 2.3.2
- migrated the documentation to markdown for more flexibility when rendering to
  the Venice HTML/PDF cheatsheet or for REPL documentation in a terminal through 
  the `doc` function (e.g. `(doc filter)`) 



## [1.9.18] - 2021-04-19

### Added

- REPL restart honors now the current macro expansion setting

### Changed

- JLine3 Jansi dependency to version 2.1.0: org.fusesource.jansi:jansi:2.1.0



## [1.9.17] - 2021-04-16

### Fixed

- 3rdParty library availability check in :xchart module



## [1.9.16] - 2021-04-16

### Fixed

- the `partition` function to behave like Clojure's `partition` function
- the function `hexdump/dump` to handle an edge case correctly



## [1.9.15] - 2021-04-14

### Added

- refactorings to the :crypt module. It's now a pure Venice module without
  needing Java helper functions.

### Fixed

- a potential missing of closing a nested FileOutputStream in a 
  try-with-resources block with function json/spit. Reported by LGTM CI
- dropping a file to the REPL that has special characters (space, asteriks, 
  ...) in the filename. The underlying OS shell is escaping these characters.
  E.g. on MacOSX:  "test\ 1.venice", "test\?1.venice"

### Changed

- build to Gradle 7.0 and moved to MavenCentral because JCenter's shutdown 
  has been announced by JFrog in February 2021



## [1.9.14] - 2021-04-04

### Added

- synchronous element processing to the Venice queue: `(queue)` 

### Fixed

- alerts reported by the LGTM code quality analysis



## [1.9.13] - 2021-03-28

### Added

- function `swap-vals!` for atoms 
- the option to pass arguments for opening MacOS apps. E.g.: 
  (shell/open-macos-app "TextEdit" "example.txt")
- an ESR module for dealing with Swiss ESR reference numbers
- a restartable option for the REPL if the REPL launcher script supports it.
  While REPL 'reload' creates a new Venice context, a 'restart' exits the
  current REPL process and starts a new REPL with a new Java VM running.

### Changed

- function >, >=, <, and <= support now more than 2 arguments and throw an 
  exception if the arguments are not numbers

### Fixed

- Google open source font download directories for extended REPL setup



## [1.9.12] - 2021-03-04

### Added

- support for 24-bit colors, cell border, and cell rotation to Excel builder



## [1.9.11] - 2021-01-24

### Added

- Excel builder

### Fixed

- `supertype` for number types. Returns now e.g. ":core/number" for `(supertype 1)` 
  instead of ":core/val".
- `instance-of` to support unqualified Java types that get resolved by import statements

### Changed

- function `instance?` to `instance-of?`
- build to Gradle 6.8
- JLine dependency to version 3.19.0



## [1.9.10] - 2021-01-10

### Changed

- XChart dependency to version 3.8.0. Due to an incompatible API change in the 
  XChart 3.8.0 Java library, Venice 1.9.10+ does not work with earlier XChart 
  versions!



## [1.9.9] - 2021-01-07

### Added

- improvements to documentation
- options for `io/zip-file` and `io/zip-list` to print progress to `*out*`
- an optional *mapper* to `io/zip-file` to simplify the creation of
  anonymized archive files
- function `ip-private?` to test for private IP addresses like 192.168.170.181

### Fixed

- fixed the function `total-memory` to return the correct value for the total
  memory available to the JVM

### Changed

- Refactored regex API regarding find functions returning positional group
  information and improved doc. `find-group` renamed to `find+`, 
  `find-all-groups` renamed to `find-all+`.



## [1.9.8] - 2021-01-01

### Added

- improvements to documentation
- function `trace/trace-str-limit`

### Fixed

- fixed function `var-name` for qualified vars
- fixed :xchart module bar charts with multiple categories 

### Changed

- XChart dependency to version 3.7.0. Due to an incompatible API change in the 
  XChart 3.7.0 Java library, Venice 1.9.8+ does not work with earlier XChart 
  versions!



## [1.9.7] - 2020-12-26

### Added

- improvements to documentation
- function `io/file-last-modified`
- support for JavaDoc. Opens a browser window displaying the JavaDoc 
  for a Java class. E.g: `(java/javadoc :java.lang.String)`

### Fixed

- documentation for Kira templating
- fixed functions `os-type` and `os-type?` for Linux operating systems

### Changed

- JLine dependency to version 3.18.0



## [1.9.6] - 2020-12-06

### Fixed

- an issue with the REPL where the expression history stopped working



## [1.9.5] - 2020-12-01

### Added

- function `cycle`
- threading macros `some->` and  `some->>`
- cross references to cheatsheet
- syntax highlighting for cheatsheet examples
- `shell/processes-info` to list the process info of all running processes
- arity check for passed arguments for all special forms
- callstack for map/set/vector used as function
- added optimizations to the interpreter that makes it up to 40% faster

### Fixed

- word wrap with function names in PDF cheatsheet
- `shell/process-info` to handle Java Optional in lieu of the caller
- callstack for macro arity exception
- `let`, `recur`, and `bindings` to throw an exception if the binding vector 
  does not have an even number of forms



## [1.9.4] - 2020-11-01

### Added

- function `partition-by`
- macro `doseq`
- arity check for passed arguments to functions. Throws an ArityException if the 
  args do not match the function's parameter count.
- automatic TCO, that was an experimental feature since version 1.9.3
- improvements to the 'kira' template extension module. With the availability of 
  `doseq` the module could be simplified. 'kira/emit' has been removed and 
  'kira/foreach' is replaced by 'doseq'. See the documentation in the readme.

### Fixed

- function `partition` for overlapping parts (when steps is smaller than n, the size of a part)
- method execution to throw an exception if a macro is passed where a function is expected



## [1.9.3] - 2020-10-17

### Added

- a tracing extension module to help with debugging
- a hexdump extension module
- a few helper functions to simplify using Java Dynamic Proxies with Java functional 
  interfaces.
  E.g.: `(proxify :java.util.function.Function { :apply #(+ % 1) })` can be simplified 
  to `(as-function #(+ % 1))`
- experimental automatic tail call optimization, not yet enabled for releases.
- experimental JPMS (Java Platform Module System) support, not yet enabled for releases. 
  Enhanced the Gradle build for building Venice as a Java 8 library supporting JPMS
  
### Fixed

- stack trace for some special form errors. The line/col information was missing.
- stack trace for symbol not found errors. The line/col information was missing.
- function `map` to support lazy seq when passing multiple collections. 
  E.g: `(map list [:a :b :c] (lazy-seq 1 inc))`



## [1.9.2] - 2020-10-02

### Fixed

- a security issue with Venice precompiled script execution not using the active interceptor
- load paths for running a Venice app (the application zip must be added to the load paths)



## [1.9.1] - 2020-10-01

### Added

- function `map-keys` and `map-vals`
- function `io/await-for` to wait for a file getting created, modified, or deleted
- function `io/watch-dir` and `io/close-watcher` to watch file modifications in a directory
- function `shell/open` to open a file or an url with the associated application
- process management function for the shell extension module
- 'gradle' extension module to run gradle build tasks
- support for SHA-1 hashes (`crypt/sha1-hash`)
- REPL support for drag/drop Venice files for execution
- load paths (used with `load-file` and `load-resource`) to the sandbox to make load paths more secure

### Changed

- the function `load-file` to accept absolute file paths, if the Venice sandbox permits it.



## [1.9.0] - 2020-09-01

### Added

- lazy sequences with values generated by functions `(lazy-seq 100 #(+ % 1))`
- big integer as core type
- hex long literals: `0x00A020FF`
- macros `cond->` and `cond->>`
- documentation for double/decimal literals in scientific notation
- the possibility to redefine a `defmethod` definition like `defn` does
- support for underscores in number literals. E.g.: `1_000_000`

### Fixed

- `map` in its transducing version to accept a map, a keyword, or a set as mapping 
  function. E.g.:  `(transduce (map :ip) conj [{:ip 6}])`
- `filter` in its transducing version to accept a map, a keyword, or a set as filter 
  predicate. E.g.:  `(transduce (filter #{1 3 5 7 9}) conj [1 2 3 4 5 6])`
- an edge case with complex macro bodies
- a problem with legend/series styling in the 'xchart' extension module
- REPL to add input that caused a ParseError to the command history to be available
  for repeated correction.

### Deprecated

- 'math' module. Big integer is now a core type



## [1.8.13] - 2020-08-01

### Added

- custom types support `assoc` to change one or multiple fields

### Performance

- The GEOIP module country lookup is now lightning-fast. A country lookup for 
  an IPv4 address on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz) takes ~1.1us
  based on actual MaxMind data.
  Venice uses now an ultra-fast trie concurrent data structure to store
  CIDR / country relations and do IP lookups. This impressively demonstrates the 
  power of trie data structures used for this kind of problems.
- Improved function invocation efficiency



## [1.8.12] - 2020-07-20

### Added

- pre-loaded modules to honor the 'macroexpandOnLoad' flag (Java integration API)
- profiling to show now details on each macro type expansion
- macro expansion of the core module at startup if up-front macro expansion
  is activated

### Fixed

- macros to use the current namespace when expanding as opposed to functions
  that use the namespace they have defined in when executing the body.
- the REPL to leave char escaping to the Venice Reader. Now (print "abc\ndef")  
  works fine in the REPL too. No double escape (print "abc\\\ndef") needed anymore.
- JLine3 from throwing an IllegalStateException in the REPL while an escaped 
  char is typed manually.



## [1.8.11] - 2020-07-09

### Added

- line joining for text blocks (backslash at the end of a line)
- support for private vars and functions. Private vars and functions may 
  only be accessed from callers within the same namespace as the private 
  var or function.
- a check to prevent the definition of private macros. Private macros open 
  all sort of complex access verification problems when macros get nested.



## [1.8.10] - 2020-06-18

### Added

- an enhancement to the syntax highlighter to mark unprocessed forms like 
  `(inc 1)` in `(do (+ 1 2)) (inc 1)` in the REPL with a special color. This 
  feature also makes unbalanced parenthesis visible.
- a history file for the REPL commands
- a clear history command to the REPL

### Fixed

- REPL to handle an edge case of unprocessed forms gracefully. E.g.: adding
  `(+ 1 2)\n(+ 2 3)` with copy/paste to the REPL.
- REPL to handle [ctrl-c] gracefully when the user interrupts the REPL while 
  reading user input

### Changed

- exit from REPL with command `!q` or `!quit` 



## [1.8.9] - 2020-06-13

### Added

- `deftype-of` validation with predicate functions as an alternative to use `assert`

### Fixed

- missing type check for 'deftype-of' builder
- 'deftype-or' with 'nil' choice (optional)



## [1.8.8] - 2020-06-11

### Changed

- REPL light color theme
 


## [1.8.7] - 2020-06-10

### Fixed

- Gradle Maven publication task to publish the shadow Jar instead of the plain
  Jar. The published releases 1.8.3 to 1.8.5 contained the plain Jar that missed the 
  repackaged JLine and Vavr libs.
  


## [1.8.6] - 2020-06-10

### Fixed

- publish to Maven by falling back to manual publication. The Gradle Maven 
  publication task published the plain Jar instead of the shadow Jar.



## [1.8.5] - 2020-06-09

### Changed

- REPL setup on Windows

### Performance

- Refactored the Venice tokenizer to improve performance by another 30%
  by simplifying character look ahead.
  The Venice reader (parser) processes now 1'000'000 source lines per second 
  measured for `core.venice` on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz),
  Java 8 server VM. See the JMH reader benchmark 'ReaderBenchmark.java'



## [1.8.4] - 2020-06-03

### Added

- improvements to syntax highlighting color themes
- function `highlight` to make syntax highlighting publicly available

### Changed

- ansi module progress bars to reduce flickering on slower systems

### Performance

- Refactored the Venice reader (parser) to not use regular expressions anymore
  for tokenizing and reading S-expressions, resulting in a 50% performance
  improvement.
  The Venice reader processes now 750'000 source lines per second measured
  for `core.venice` on a 2017 MacBook Pro (Mac OSX, Core i7 2.8 GHz), Java 8 
  server VM. See the JMH reader benchmark 'ReaderBenchmark.java'



## [1.8.3] - 2020-06-03

### Added

- syntax highlighting for the REPL. REPL syntax highlighting can be
  turned on/off.
- function `fnil`

### Changed

- function `str/lower-case` and `str/upper-case` to accept an optional locale
- from manual Maven publishing to the "Maven Publisher" Gradle plugin
- VAVR dependency to version 0.10.3
- JLine dependency to version 3.15.0
- optional OpenPDF dependency to version 1.3.17



## [1.8.2] - 2020-05-28

### Added

- Gradle build automated Maven publish



## [1.8.1] - 2020-05-27

### Added

- PDF watermark text outline customization
- implicitly generating build and check functions for custom types

### Fixed

- 'parsatron' parser combinator



## [1.8.0] - 2020-05-18

### Added

- functions `deftype`, `deftype-of`, and `deftype-or`

### Fixed

- PDF watermark color opacity: OpenPDF library changed behavior in 
  one of the recent releases!



## [1.7.29] - 2020-05-14

### Added

- access to stdin in the REPL and Venice scripts 
- function `read-line`
- optional revision number support to 'semver' module (ArangoDB uses 
  3.6.3.1 style versions thus not sticking to semantic versioning)
  
### Changed

- REPL setup: repl.bat gets an `@ECHO OFF` to suppress command echoing



## [1.7.28] - 2020-05-13

### Changed

- REPL setup: handles color mode better
- function `str/replace-first` to optionally support replacing ignoring case 
  and replacing the first n occurrences
- function `str/replace-last` to optionally support replacing ignoring case

### Fixed

- edge cases in functions `str/replace-first` and `str/replace-last`



## [1.7.27] - 2020-05-12

### Added

- functions `str/expand`

### Changed

- function `str/truncate` to support an optional truncation mode `:start`, `:middle`, or `:end`
- REPL setup: put the generated 'repl.json' on the classpath

### Fixed

- `drop` function (dropped 1 item too much, the corresponding unit test did not have a 
   test annotation, so the test slipped)



## [1.7.26] - 2020-05-11

### Added

- functions `io/internet-avail?`, `str/cr-lf`, `charset-default-encoding`
- simplified REPL setup (automated extended setup)
- module 'tomcat' throws an exception when used and the Tomcat libs are not on 
  the classpath
- module 'xchart' throws an exception when used and the XChart libs are not on 
  the classpath



## [1.7.25] - 2020-05-08

### Fixed

- fixed REPL on Windows to load the 'jansi' library 
- fixed function 'print' to return always 'nil' instead of the print 
  stream's class name



## [1.7.24] - 2020-05-08

### Added

- module `tput` for terminal output on Linux and Mac OSX
- module `ansi` to use colors and cursor control on ANSI
  terminals
- functions `update-in` and `dissoc-in`

### Fixed

- sequential destructuring to return `nil` on empty varargs.
  E.g.: `(let [[a b & c] '(1 2)] [a b c])` c must be `nil` and not an empty list
- an edge case (nil collection) with function `apply`. `(apply + 1 2 nil)` is
  equal to `(apply + 1 2 [])`.

### Changed

- function `io/download` to accept a progress function, a connection timeout, 
  and a read timeout as option
- optional OpenPDF dependency to version 1.3.16



## [1.7.23] - 2020-04-27

### Added

- dark mode support for the REPL. Launch the REPL with the command line option 
  '-colors-darkmode'
  
### Fixed

- accidentally bundled OpenSans and SourceCodePro fonts with the Venice 1.7.22 
  Jar 
- functions `nfirst` and `nlast` to work with Java based lists too
- default sandbox rules to include Venice exception types




## [1.7.22] - 2020-04-26

### Added

- `CallbackPrintStream` class to simplify stdout/stderr capturing in some
  Java integration scenarios

### Fixed

- functions `keep`, `remove`, `every?`, `filter-k`, and `filter-kv` to work 
  with sets as predicate functions too
- a bug in the  _xchart_  module. With the introduction of formal types
  (Venice 1.7.17) the chart stylers need explicit casts, to circumvent
  a flaw in the XChart API.

### Changed

- OpenPDF lib to actual version 1.3.15



## [1.7.21] - 2020-04-11

### Added

- function `deref?` to test if a value is dereferenceable to get the 'boxed' 
  value
- enhancements to the sandbox to allow blacklisted Venice functions sets to 
  be opened by specific functions. 
  E.g.: blacklist all IO functions but allow `print`, `println`, `printf`, 
  and `newline`.
- module `fam` to support functors, applicatives, and monads

### Fixed

- `send-off` to be optionally called without fn args

### Changed

- JLine3 lib to actual version 3.14.1
- build to Gradle 6.3



## [1.7.20] - 2020-03-09

### Added

- macro `with-err-str` that returns the captured text from stderr
- stderr support for functions `print`, `println`, `printf`, `newline`, and `flush`
- stderr support for REPL. While stdout text is printed in grey stderr text 
  is printed in light red



## [1.7.21] - 2020-03-06

### Added

- function `filter-k` to filter maps by key
- function `filter-kv` to filter maps by key and/or value
- improvements to mercator and geoip modules

### Fixed

- an issue with Java Interop exception messages

### Changed

- JLine3 lib to actual version 3.14.0
- build to Gradle 6.2.2



## [1.7.18] - 2020-02-20

### Added

- functions `cast` and `formal-type` to allow explicit casts on Java objects 
- an optional user-agent string for `io/download`. Some servers reply with a 
  403 (access denied) if there is no user-agent sent with the HTTP request.
  E.g.: `(io/download "https://foo/foo.png :binary true :user-agent "Mozilla")`
- new sandbox default rules due to introduction of formal types: 
    - class:java.util.List:*
    - class:java.util.Set:*
    - class:java.util.Map:*



## [1.7.17] - 2020-02-05

### Added

- optional upfront macro expansion for all execution modes (REPL, Launcher, and embedding in Java)
- customizable mercator maps and rendering styles to mercator module

### Changed

- OpenPDF libs to 1.3.12

### Fixed

- Java Interop reflection to use the formal type of the value an instance method returns for subsequent calls on the returned object. This fixes the warnings "An illegal reflective access operation has occurred" on Java 9+. Future Java versions will even deny this access.

  E.g.: java.awt.BufferedImage::createGraphics() defines 'java.awt.Graphics2D' as the formal type in the API and returns a 'sun.java2d.SunGraphics2D' object that is in a private module. 



## [1.7.16] - 2020-01-21

### Added

- performance improvements to function invocation
- throw an exception if a 'recur' expression is not in tail position

### Fixed

- loop-recur to allow multiple expressions in the body



## [1.7.15] - 2020-01-14

### Added

- improvements to REPL for printing to terminal
- function `io/list-files-glob` to list files with a glob pattern

### Fixed

- the launcher when passing a '-file' option to run a script.



## [1.7.14] - 2020-01-11

### Added

- customizable marker and labels to the 'mercator' map module

### Fixed

- 'tomcat-util' module access log file parser
- function 'disj' to work with all set types

### Changed

- JLine3 lib to version 3.13.3
- FlyingSaucer lib to version 9.1.20



## [1.7.13] - 2020-01-10

### Added

- CSV writer
- macro `when-let`

### Fixed

- CSV module embedded quote characters in cells. They are represented by a pair 
  of the quote character. 
- function `map` to accept a keyword as mapping function

### Changed

- improved performance of the CSV reader and the CIDR IP block parser



## [1.7.12] - 2020-01-03

### Added

- modules `cidr`, `mercator`, and `geoip` to map IP address locations to a 2D map.
  Use case: visualize Tomcat client IP addresses from the access log file on
  a world map.
- function `io/zip-list-entry-names`

### Fixed

- macro `load-file` to honor the 'force' option
- deprecated Gradle build properties
- function `empty` to support sets (required for `macroexpand-all`)
- function `map` to support sets (required for `macroexpand-all`)
- transfer of meta data in simple destructuring [x 10] from symbol to bind value

### Changed

- `coalesce` to evaluate its arguments lazy. It's now a macro.



## [1.7.11] - 2019-12-20

### Added

- optional macro expansion at precompilation phase. This gives another 
  performance gain when using Venice as an expression or rules engine. 
  See the benchmark in [Embedding Venice](doc/readme/embedding.md) 
- conj on maps to accept multiple elements
- a tree walker (`prewalk`, `postwalk`) for Venice data structures
- function `macroexpand-all` to recursively expand macros in a s-expression 
- load path file completion for the REPL with `(load-file file-path)`
- load path to support ZIP files in addition to directories
- support for Venice archives for simplified distribution of Venice apps
  (see extension module :app)

### Fixed

- print functions (`println`, `newline`) to use the platform specific 
  newline LF or CR-LF
- an edge case with auto gen symbols for macros
- zipper: adding a ZIP entry from a java.io.File caused a ClassCastException

### Changed

- the sandbox to a whitelist model regarding loading extension modules. 
  This is less error prone if Venice is providing more extension modules 
  in the future.
- JLine3 lib to actual version 3.13.2
- build to Gradle 6.0.1



## [1.7.10] - 2019-11-21

### Fixed

- a problem with caching Java interop meta data under heavy multi-threaded 
  load.



## [1.7.9] - 2019-11-20

### Added

- functions `every-pred`, `any-pred`
- function `locking`, Venice's equivalent for `synchronized` in Java
- auto generated unique symbol names (hash suffix) for macros 
  to be used within syntax quotes. E.g.: `` `(let [a# 100] (println a#))``
  expands to `(let [a__9__auto 100] (println a__9__auto))`
- function `load-file` supports now an optional load-path that defines a 
  set of ';' delimited paths the file is searched for. In
  absence the file is loaded relative to current working directory. The
  load-path is passed to Venice via the command line arg '-loadpath'. 
  The load-path is supported in all run modes: REPL and Venice file/script
  execution.
- functions `futures-fork`, `futures-wait`

### Changed

- function `mod` is now implemented with Java `Math.floorMod(x,y)` instead 
  of `x % y` to change the behavior on negative values: `(mod -1 5)` 
  returns now `4`.
- JLine3 lib to actual version 3.13.1
- Flying-Saucer libs to version 9.1.19



## [1.7.8] - 2019-11-02

### Added

- keywords to work as discriminator function with multi-methods

### Fixed

- 'tomcat-util' module TC installation cleanup



## [1.7.7] - 2019-10-27

### Added

- function `io/list-file-tree` 
- function `str/double-unquote`

### Fixed

- a syntax problem with a comment in the benchmark module causing the 
  module load to fail

### Changed

- function `load-file` and `load-classpath-file` to load the file only 
  once. There is an option to force a reload.



## [1.7.6] - 2019-10-15

### Added

- char data type

### Fixed

- an edge case with java interop matching number args
- tomcat-util module function `process-running?` 



## [1.7.5] - 2019-10-11

### Added

- Java «raw string literals» style triple quoted strings
- function `=`. Returns true if both operands have equivalent type and 
  value. `(= 1 1) ; true`, `(= 1 1.0) ;  false`, `(== 1 1.0) ;  true`

### Fixed

- fixed java interop bean setters

### Changed

- build to Gradle 5.6.2



## [1.7.4] - 2019-10-03

### Added

- functions `merge-with`, `fourth`, `shuffle`
- transducing function `map-indexed`
- sandboxed access to individual system environment variables (function `system-env`).
- enhancement to `io/file` to accept multiple children paths
- enhancement to `io/copy-file` to support file replace option
- enhanced stdout/stderr processing of the `sh` function
- module for semantic versions `(load-module :semver)`
- module for Tomcat management `(load-module :tomcat-util)`

### Fixed

- fixed function `sh`: the subprocess should inherit the environment 
  of the current process if no explicit environment is given

### Changed

- OpenPDF lib to actual version 1.3.11



## [1.7.3] - 2019-09-17

### Added

- function `str/reverse`
- `nfirst` and `nlast` to work with strings as well
- enhancements to the `math` module to support the examples in the 
  [Recursion](doc/readme/recursion.md) read-me 



## [1.7.2] - 2019-09-12

### Added

- collection `set` to work as a function: `(#{1 2 3} 1)`
- macro `condp`
- function `trampoline`

### Fixed

- fixed function `some`



## [1.7.1] - 2019-09-05

### Fixed

- fixed JSON to handle empty objects and empty arrays.
- fixed `reduce-kv`. Can now reduce to any type not just maps.
- fixed `flatten`. Keep it from flattening maps.

### Info

- tested JSON reader on large files. Reading a 800MB JSON file into
  Venice data types took 3.5s on a 2017 MacBook Pro.



## [1.7.0] - 2019-09-01

### Added

- full namespace support

### Fixed

- meta data for `nil` constant

### Changed

- VAVR lib to actual version 0.10.2
- JLine3 lib to actual version 3.12.1



## [1.6.3] - 2019-08-08

### Fixed

- an edge case with symbol lookup

### Incompatible Changes

- renamed 'kira/docoll' to 'kira/foreach'



## [1.6.2] - 2019-07-31

### Added

- benchmark module
- statistics functions `mean`, `median`, `quartiles`, `quantile`, `standard-deviation`
- math functions `log`, `log10`, `pow`



## [1.6.1] - 2019-07-19

### Added

- `remove`, `distinct`, `reverse`, `flatten`, `sorted`, and `halt-when` to the list of transducer functions
- `rf-first`, `rf-every?`, `rf-any?` reducing functions to be used with `transduce`
- function `constantly`

### Fixed

- `comp` function to allow 0-arity: `(map (comp) [1 2 3 4])` => (1 2 3 4)



## [1.6.0] - 2019-07-14

### Added

- Transducers 
- [PDF renderer & tools](doc/readme/pdf.md) 

### Fixed

- `(conj nil item)` to return `(item)`



## [1.5.11] - 2019-06-27

### Changed

- cheat sheet generator to use Venice/Kira as templating engine 

### Fixed

- _maven_ extension module group-id handling



## [1.5.10] - 2019-06-24

### Added

- layout improvements to the PDF cheatsheet. The PDF cheatsheet embeds now the
  'Open Sans' and 'Source Code Pro' open source fonts to be available on all 
  platforms the cheatsheet is opened.
- helper functions to _Kira_ templating to simplify template processing
- extensive documentation to _Kira_ templating: [Kira](doc/readme/ext-kira.md)



## [1.5.9] - 2019-06-19

### Fixed

- runtime dependency on commons-lang3 lib.



## [1.5.8] - 2019-06-15

### Added

- queue datatype (bounded or unbounded)
- registration of Venice functions as JVM shutdown hooks
- a one-shot and a periodic function scheduler `schedule-delay`, `schedule-at-fixed-rate`
- bytebuf hex conversion functions `str/hex-to-bytebuf`, `str/bytebuf-to-hex`, `str/format-bytebuf`
- cryptographic hash functions for PBKDF2 and SHA-512
- encryption/decryption functions for DES, 3DES, and AES256
- REPL wildcard filters for listing symbols: `!env global`, `!env global io/*`, `!env global *file*`



## [1.5.7] - 2019-06-01

### Added

- function `io/zip-list` to list zip file content (similiar to Linux' «unzip -vl x.zip» command)
- function `io/zip-append` to append or replace files to a zip
- function `io/zip-remove` to remove files from a zip

### Fixed

- function `io/zip` to support empty directories



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
