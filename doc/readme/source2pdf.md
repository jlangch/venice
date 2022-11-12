# Source Code as PDF

For documentation purposes Venice source code can be rendered 
to HTML or PDF documents. The rendering process is started from a
REPL.


### Example

Open a REPL and use the command `!source-pdf` with three arguments:

 1. the path to the Venice source file (e.g.: /Users/foo/scripts/sudoku.venice)
 2. the destination dir for the PDF (e.g.: /Users/foo/scripts/)

To create a HTML and PDF representation from the 'sudoku.venice' script run:

```clojure
venice> !source-pdf /Users/foo/scripts/sudoku.venice /Users/foo/scripts
```

Output Examples:

[Sudoku PDF](https://raw.githubusercontent.com/jlangch/venice/master/doc/assets/source-to-pdf/sudoku.venice.pdf)

[Sudoku HTML](https://htmlpreview.github.io/?https://github.com/jlangch/venice/blob/master/doc/assets/source-to-pdf/sudoku.venice.html)


### Prerequisites

The PDF renderer requires two specific fonts in the REPL's font directory:

 - OpenSans-Regular.ttf
 - SourceCodePro-Regular.ttf
 

Venice supports generating PDF files if the [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) 
libs are on the runtime classpath:

 - org.xhtmlrenderer:flying-saucer-core:9.1.22
 - org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.22
 - com.github.librepdf:openpdf:1.3.30
 - com.github.librepdf:pdf-toolbox:1.3.30
 
 
Run this script from the REPL to download the fonts and PDF libraries:

```clojure
(do
  (load-module :maven ['maven :as 'm])
  (load-module :fonts ['fonts :as 'f])
  
  ;; Download the PDF libs from Maven
  (println "Downloading PDF libs...")
  (m/download "org.xhtmlrenderer:flying-saucer-core:9.1.22" :dir (repl/libs-dir))
  (m/download "org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.22" :dir (repl/libs-dir))
  (m/download "com.github.librepdf:openpdf:1.3.30" :dir (repl/libs-dir))
  (m/download "com.github.librepdf:pdf-toolbox:1.3.30" :dir (repl/libs-dir))

  ;; Download and unzip the OpenSans font family
  (println "Downloading OpenSans font familiy...")
  (fonts/download-font-family "OpenSans" (repl/fonts-dir) true)

  ;; Download and unzip the SourceCodePro font family
  (println "Downloading SourceCodePro font familiy...")
  (fonts/download-font-family "SourceCodePro" (repl/fonts-dir) true)

  ;; Restart the REPL to make the new libs available to the REPL Java VM
  (println "Restarting...")
  (repl/restart))
```
 