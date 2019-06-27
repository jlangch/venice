# PDF

Venice supports generating PDF files if the [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) 
libs are on the runtime classpath:

 - org.xhtmlrenderer:flying-saucer-core:9.1.18
 - org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.18
 - com.github.librepdf:openpdf:1.2.21
 
Flying Saucer is a pure-Java library for rendering XHTML using CSS 2.1 for layout and formatting with output to PDF.

Flying Saucer documentation is available in the user guide, linked from their website at [Flying Saucer](https://code.google.com/archive/p/flying-saucer/)


 
## Generate PDF

t.b.d.



## Generate PDF with embedded images

t.b.d.



## PDF Watermarks

t.b.d.


## Download required 3rd party libs

```clojure
(do
  (load-module :maven)
  
  (maven/download "org.xhtmlrenderer:flying-saucer-core:9.1.18")
  (maven/download "org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.18")
  (maven/download "com.github.librepdf:openpdf:1.2.21"))
```
