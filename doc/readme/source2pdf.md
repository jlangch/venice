# Source Code as PDF

For documentation purposes Venice source code can be rendered 
to HTML or PDF documents. The rendering process is started from a
REPL.


### Example

Open a REPL and use the command `!source-pdf` with three arguments:

 1. the path to the Venice source file (e.g.: /Users/foo/scripts/sudoku.venice)
 2. the destination dir for the PDF (e.g.: /Users/foo/scripts/)
 3. the dir to the fonts (e.g.: /Users/foo/fonts)

To create a HTML and PDF representation from the 'sudoku.venice' script run:

```clojure
venice> !source-pdf /Users/foo/scripts/sudoku.venice /Users/foo/scripts/ /Users/foo/fonts
```

[Sudoku PDF](https://raw.githubusercontent.com/jlangch/venice/master/doc/assets/source-to-pdf/sudoku.venice.pdf)

[Sudoku HTML](https://htmlpreview.github.io/?https://github.com/jlangch/venice/blob/master/doc/assets/source-to-pdf/sudoku.venice.html)


### Prerequisites

The PDF renderer requires two specific fonts in the passed font directory '/Users/foo/fonts':

 - [OpenSans-Regular.ttf](https://fonts.google.com/specimen/Open+Sans)
 - [SourceCodePro-Regular.ttf](https://fonts.google.com/specimen/Source+Sans+Pro)
 

Venice supports generating PDF files if the [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) 
libs are on the runtime classpath:

 - org.xhtmlrenderer:flying-saucer-core:9.1.22
 - org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.22
 - com.github.librepdf:openpdf:1.3.26
 - com.github.librepdf:pdf-toolbox:1.3.26
 
 
Run this script from the REPL to download the fonts and PDF libraries:

```clojure
(do
  (load-module :maven ['maven :as 'm])
  
  ;; Download the PDF libs from Maven
  (println "Downloading PDF libs...")
  (m/download "org.xhtmlrenderer:flying-saucer-core:9.1.22" :dir (repl/libs-dir))
  (m/download "org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.22" :dir (repl/libs-dir))
  (m/download "com.github.librepdf:openpdf:1.3.26" :dir (repl/libs-dir))
  (m/download "com.github.librepdf:pdf-toolbox:1.3.26" :dir (repl/libs-dir))

  ;; Download and unzip the OpenSans font familiy
  (println "Downloading OpenSans font familiy...")
  (-<> "https://fonts.google.com/download?family=Open%20Sans"
       (io/download <> :binary true :user-agent "Mozilla")
       (io/unzip-all "static/OpenSans/*.ttf" <>)
       (map-keys #(str/strip-start % "static/OpenSans/") <>)
       (docoll (fn [[k v]] (io/spit (io/file (repl/fonts-dir) k) v)) <>))

  ;; Download and unzip the SourceCodePro font familiy
  (println "Downloading SourceCodePro font familiy...")
  (-<> "https://fonts.google.com/download?family=Source%20Code%20Pro"
       (io/download <> :binary true :user-agent "Mozilla")
       (io/unzip-all "static/*.ttf" <>)
       (map-keys #(str/strip-start % "static/") <>)
       (docoll (fn [[k v]] (io/spit (io/file (repl/fonts-dir) k) v)) <>))

  ;; Restart the REPL to make the new libs available to the REPL Java VM
  (println "Restarting...")
  (repl/restart))
```
 