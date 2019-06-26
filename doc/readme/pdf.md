# PDF

Venice supports generating PDF files if the [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) 
libs are on the runtime classpath:

 - org.xhtmlrenderer:flying-saucer-core:9.1.18
 - org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.18
 - com.github.librepdf:openpdf:1.2.18
 
Flying Saucer is a pure-Java library for rendering arbitrary well-formed XML (or XHTML) 
using CSS 2.1 for layout and formatting, output to Swing panels, PDF, and images.

Documentation is available in Flying Saucer user guide, linked from their website at [Flying Saucer](https://code.google.com/archive/p/flying-saucer/)


 
## Generate PDF

t.b.d.



## Generate PDF with image resource

t.b.d.



## PDF Watermarks

t.b.d.


## Download required 3rd party libs

```clojure
(do
  (load-module :maven)
  
  (maven/download "org.xhtmlrenderer:flying-saucer-core:9.1.18")
  (maven/download "org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.18")
  (maven/download "com.github.librepdf:openpdf:1.2.18"))
```
