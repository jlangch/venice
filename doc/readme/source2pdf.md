# Source Code as PDF

For documentation purposes Venice source code can be rendered 
to HTML or PDF documents. The rendering process is started from a
REPL.


### Example

Open a REPL and use the command `!source-pdf`:

To create a HTML and PDF representation from the 'sudoku.venice' script run:

```clojure
venice> !source-pdf ./sudoku.venice
```

Output Examples:

[Sudoku PDF](https://raw.githubusercontent.com/jlangch/venice/master/doc/assets/source-to-pdf/sudoku.venice.pdf)  
[Sudoku HTML](https://htmlpreview.github.io/?https://github.com/jlangch/venice/blob/master/doc/assets/source-to-pdf/sudoku.venice.html)


### Prerequisites

The Venice PDF renderer is based on the [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) 
project and requires these Java libraries:

 - org.xhtmlrenderer:flying-saucer-core:9.1.22
 - org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.22
 - com.github.librepdf:openpdf:1.3.30
 - com.github.librepdf:pdf-toolbox:1.3.30
 
and the fonts:

 - OpenSans-Regular.ttf
 - SourceCodePro-Regular.ttf


Run this script from the REPL to download the fonts and PDF libraries:

```clojure
(do
  (load-module :pdf-tools)
  (load-module :fonts)
  
  ;; Download the PDF libs from Maven
  (pdf-tools/download-libs :dir (repl/libs-dir) :silent false)

  ;; Download and unzip the font families
  (fonts/download-font-family "OpenSans" 
                              :dir (repl/fonts-dir) 
                              :extract true 
                              :silent false)
  (fonts/download-font-family "SourceCodePro" 
                              :dir (repl/fonts-dir) 
                              :extract true 
                              :silent false)

  ;; Restart the REPL to make the new libs available to the REPL Java VM
  (println "Restarting...")
  (repl/restart))
```
 