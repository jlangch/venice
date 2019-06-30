# PDF

Venice supports generating PDF files if the [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) 
libs are on the runtime classpath:

 - org.xhtmlrenderer:flying-saucer-core:9.1.18
 - org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.18
 - com.github.librepdf:openpdf:1.2.21
 
Flying Saucer is a pure-Java library for rendering XHTML using CSS 2.1 for layout and formatting with output to PDF.

Flying Saucer documentation is available in the user guide, linked from their website at [Flying Saucer](https://code.google.com/archive/p/flying-saucer/)

- [Introduction Example](Generate PDF)
- [Watermarks][Generate Watermarks]

 
## Generate PDF

... work in progress ...

```clojure
(do 
  (import :com.github.jlangch.venice.pdf.PdfRenderer)
  
  (load-module :kira)
  
  (defn format-ts [t] (time/format t "yyyy-MM-dd"))
  
  ; define the template
  (def template (str/strip-indent """\
     <?xml version="1.0" encoding="UTF-8"?>
     <html lang="en" xmlns="http://www.w3.org/1999/xhtml">
       <head>
         <style type="text/css">
           @page {
             size: A4 portrait;
             margin: 2cm 1.0cm;
             padding: 0;
           }
           body {
             background-color: white;
             font-family: sans-serif;
             font-weight: 400;
           }
           div.title  {
             margin-top: 3cm;
             text-align: center;
           }
           div.date  {
             margin-top: 1cm;
             text-align: center;
           }
         </style>
       </head>
       
       <body>
         <div class="title">${ (kira/escape-xml title) }$</div>
         <div class="date">${ (kira/escape-xml timestamp format-ts) }$</div>
       </body>
     </html>
     """))

  (def data { :title "Hello, world"
              :timestamp (time/local-date 2000 8 1) } )

  ; evaluate the template, render it as PDF, and save it
  (->> data
       (kira/eval template ["${" "}$"])
       (. :PdfRenderer :render)
       (io/spit "test.pdf"))
)
```



## Generate PDF with embedded images

t.b.d.



## PDF Watermarks

t.b.d.


## Custom fonts

t.b.d.


## Page footers

t.b.d.


## Download required 3rd party libs

```clojure
(do
  (load-module :maven)
  
  (maven/download "org.xhtmlrenderer:flying-saucer-core:9.1.18")
  (maven/download "org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.18")
  (maven/download "com.github.librepdf:openpdf:1.2.21"))
```
