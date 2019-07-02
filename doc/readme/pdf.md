# PDF

Venice supports generating PDF files if the [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) 
libs are on the runtime classpath:

 - org.xhtmlrenderer:flying-saucer-core:9.1.18
 - org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.18
 - com.github.librepdf:openpdf:1.2.21
 
Flying Saucer is a pure-Java library for rendering XHTML using CSS 2.1 for layout and formatting with output to PDF.

Flying Saucer documentation is available in the user guide, linked from their website at [Flying Saucer](https://code.google.com/archive/p/flying-saucer/)


## PDF Generation

The PDF generation workflow is built on two steps:

1. Build the document's XHTML representation. The XHTML can be easily produced by Venice's Kira template engine

2. Generate the PDF from the XHTML


References:

* [Kira Template](ext-kira.md)
* [Introduction Example](#introduction-example)
* [Tables](#tables)
* [Embedded Images](#embedded-images)
* [Custom Embedded Fonts](#custom-embedded-fonts)
* [Table of Content](#table-of-content)
* [Page Footers](#page-footers)
* [Watermarks](#watermarks)
* [Download Libraries](#download-required-3rd-party-libs)



## Introduction Example

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
             margin: 3cm 0 5cm 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.head  {
             margin-top: 1cm;
             text-align: center;
           }
           div.date  {
             margin-top: 1cm;
             text-align: center;
           }
         </style>
       </head>
       
       <body>
         <div class="title">Introduction Example</div>
         <div class="head">${ (kira/escape-xml title) }$</div>
         <div class="date">${ (kira/escape-xml timestamp format-ts) }$</div>
       </body>
     </html>
     """))

  (def data { :title "Hello, world"
              :timestamp (time/local-date 2000 8 1) } )

  ; evaluate the template, render, and save it
  (->> data
       (kira/eval template ["${" "}$"])
       (pdf/render)
       (io/spit "test.pdf"))
)
```

[Generated PDF](https://github.com/jlangch/venice/blob/master/doc/pdfs/introduction-example.pdf)


## Tables

t.b.d.


## Embedded Images

t.b.d.


## Custom Embedded Fonts

The PDF renderer loads custom True-Type fonts from the classpath.

The font files are searched on the specified base url (e.g. `classpath:///`) and 
optional alternative base paths (e.g. `fonts`).

_Note: alternative base paths are always relative to the specified base url_

Search on  classpath `/*.ttf`, `/fonts/*.ttf`, and `/images/*.ttf`:

```clojure
(. :PdfRenderer :render xhtml "classpath:///" ["fonts" "images"])
```


It's best to package the font files in a JAR like `fonts.jar` and place the JAR
on the Venice classpath.

A `fonts.jar` file containing the OFL fonts 'Open Sans' and 'Source Code Pro'
may look like:

```text
   Length         Date/Time Name
----------  ---------------- ----
         0  2019-07-01 13:20 fonts/
    213252  2019-06-20 09:04 fonts/SourceCodePro-Light.ttf
    212896  2019-06-20 09:04 fonts/OpenSans-Italic.ttf
    212880  2019-06-20 09:04 fonts/SourceCodePro-Regular.ttf
    224592  2019-06-20 09:04 fonts/OpenSans-Bold.ttf
    217360  2019-06-20 09:04 fonts/OpenSans-Regular.ttf
    211716  2019-06-20 09:04 fonts/SourceCodePro-Bold.ttf
```

A pre-built `fonts.jar` with these fonts can be downloaded from Venice GitHub 
[Demo Fonts](https://github.com/jlangch/venice/blob/master/doc/pdfs/fonts.jar)


```clojure
(do 
  (import :com.github.jlangch.venice.pdf.PdfRenderer)
  
  (load-module :kira)
  
  (def text (str/lorem-ipsum :paragraphs 1))
  
  ; define the template
  (def template (str/strip-indent """\
     <?xml version="1.0" encoding="UTF-8"?>
     <html lang="en" xmlns="http://www.w3.org/1999/xhtml">
       <head>
         <style type="text/css">
           @font-face {
              font-family: 'Open Sans';
              src: url('OpenSans-Regular.ttf');
              font-style: normal;
              font-weight: normal;
              -fs-pdf-font-embed: embed;
              -fs-pdf-font-encoding: Identity-H;
           }
           @font-face {
              font-family: 'Open Sans Bold';
              src: url('OpenSans-Bold.ttf');
              font-style: normal;
              font-weight: normal;
              -fs-pdf-font-embed: embed;
              -fs-pdf-font-encoding: Identity-H;
           }
           @font-face {
              font-family: 'Source Code Pro';
              src: url('SourceCodePro-Regular.ttf');
              font-style: normal;
              font-weight: normal;
              -fs-pdf-font-embed: embed;
              -fs-pdf-font-encoding: Identity-H;
           }
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
             margin: 3cm 0 5cm 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.head  {
             font-family: sans-serif;
             margin-top: 1cm;
             font-weight: 700;
           }
           div.serif  {
             font-family: serif;
           }
           div.sans-serif  {
             font-family: sans-serif;
           }
           div.open-sans  {
             font-family: 'Open Sans', sans-serif;
           }
           div.open-sans-bold  {
             font-family: 'Open Sans Bold', sans-serif;
           }
           div.source-code-pro  {
             font-family: 'Source Code Pro', sans-serif;
           }
         </style>
       </head>
       
       <body>
         <div class="title">Custom Embedded Fonts</div>

         <div class="head">Sans Serif</div>
         <div class="sans-serif">${ (kira/escape-xml text) }$</div>

         <div class="head">Serif</div>
         <div class="serif">${ (kira/escape-xml text) }$</div>
         
         <div class="head">Open Sans</div>
         <div class="open-sans">${ (kira/escape-xml text) }$</div>
         
         <div class="head">Open Sans Bold</div>
         <div class="open-sans-bold">${ (kira/escape-xml text) }$</div>
        
         <div class="head">Source Code Pro</div>
         <div class="source-code-pro">${ (kira/escape-xml text) }$</div>
       </body>
     </html>
     """))

  ; create a Lorem Ipsum text block
  (def data { :text (str/lorem-ipsum :paragraphs 1) } )
  
  ; Evaluate the template, render, and save it.  
  (-<> data
       (kira/eval template ["${" "}$"] <>)
       (pdf/render <> "classpath:///" ["fonts" "images"])
       (io/spit "test.pdf" <>))
)
```

[Generated PDF](https://github.com/jlangch/venice/blob/master/doc/pdfs/custom-embedded-fonts.pdf)




## Table of Content

t.b.d.


## Page Footers

t.b.d.


## Watermarks

t.b.d.


## Download required 3rd party libs

```clojure
(do
  (load-module :maven)
  
  (maven/download "org.xhtmlrenderer:flying-saucer-core:9.1.18")
  (maven/download "org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.18")
  (maven/download "com.github.librepdf:openpdf:1.2.21"))
```
