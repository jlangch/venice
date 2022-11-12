# PDF

Venice supports generating PDF files if the [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) 
libs are on the runtime classpath:

 - org.xhtmlrenderer:flying-saucer-core:9.1.22
 - org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.22
 - com.github.librepdf:openpdf:1.3.30
 - com.github.librepdf:pdf-toolbox:1.3.30
 
Flying Saucer is a pure-Java library for rendering XHTML using CSS 2.1 for layout and formatting with output to PDF.

Flying Saucer documentation is available in the user guide, linked from their website at [Flying Saucer](https://code.google.com/archive/p/flying-saucer/)

Run this script from the REPL to download the PDF libraries:

```clojure
(do
  (load-module :pdf-tools)
  
  ;; Download the PDF libs from Maven
  (pdf-tools/download-libs :dir (repl/libs-dir) :silent false)

  ;; Restart the REPL to make the new libs available to the REPL Java VM
  (println "Restarting...")
  (repl/restart))
```


## PDF Generation

The PDF generation workflow is built on two steps:

1. Build the document's CSS styled XHTML representation. The XHTML can be easily produced by Venice's Kira template engine

2. Generate the PDF from the XHTML


References:

* [Introduction Example](#introduction-example)
* [Tables](#tables)
* [Images](#images)
* [Fonts](#fonts)
* [Table of Content](#table-of-content)
* [Page Footers](#page-footers)
* [Watermarks](#watermarks)
* [Text to PDF](#text-to-pdf)
* [Merge PDFs](#merge-pdfs)
* [Copy PDF](#copy-pdf)
* [Kira Template Engine](ext-kira.md)



## Introduction Example

```clojure
(do
  (ns test)

  (load-module :kira)

  ;; ensure PDF libs are available when loading this file
  (pdf/check-required-libs)



  (defn format-ts [t] (time/format t "yyyy-MM-dd"))

  ; define the template
  (def template
     """
     <?xml version="1.0" encoding="UTF-8"?>
     <html lang="en" xmlns="http://www.w3.org/1999/xhtml">
       <head>
         <style type="text/css">
           @page {
             size: A4 portrait;
             margin: 2cm 1cm;
             padding: 0;
           }
           body {
             background-color: white;
             font-family: sans-serif;
             font-weight: 400;
           }
           div.title {
             margin: 3cm 0 0 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.subtitle {
             margin: 1cm 0 4cm 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.head {
             margin-top: 1cm;
             text-align: center;
           }
           div.date {
             margin-top: 1cm;
             text-align: center;
           }
         </style>
       </head>

       <body>
         <div class="title">Venice PDF Introduction</div>
         <div class="subtitle">Example</div>

         <div class="head">${= (kira/escape-xml title) }$</div>
         <div class="date">${= (kira/escape-xml timestamp test/format-ts) }$</div>
       </body>
     </html>
     """)

  (def data { :title "Hello, world"
              :timestamp (time/local-date 2000 8 1) } )

  ; evaluate the template, render, and save it
  (->> data
       (kira/eval template ["${" "}$"])
       (pdf/render)
       (io/spit "introduction-example.pdf"))
)
```

[Generated PDF](https://github.com/jlangch/venice/blob/master/doc/pdfs/introduction-example.pdf)

[top](#pdf-generation)



## Tables

```clojure
(do
  (ns test)

  (load-module :kira)

  ;; ensure PDF libs are available when loading this file
  (pdf/check-required-libs)



  (defn format-birth-date [s] (if (string? s) s (time/format s "yyyy-MM-dd")))

  ; define the template
  (def template
     """
     <?xml version="1.0" encoding="UTF-8"?>
     <html lang="en" xmlns="http://www.w3.org/1999/xhtml">
       <head>
         <style type="text/css">
           @page {
             size: A4 portrait;
             margin: 2cm 1cm;
             padding: 0;
           }
           body {
             background-color: white;
             font-family: sans-serif;
             font-weight: 400;
           }
           div.title {
             margin: 3cm 0 0 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.subtitle {
             margin: 1cm 0 4cm 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           table.people {
             margin-top: 2cm;
             width: 100%;
             border-collapse: collapse;
             border-spacing: 0px;
             empty-cells: show;
             vertical-align: top;
             table-layout: fixed;
             font-size: 9pt;
           }
           table.people > tbody > tr > td {
             padding: 1mm;
             word-wrap: break-word;
             vertical-align: top;
           }
           table.people > tbody > tr:first-child {
             font-weight: 600;
             padding: 1mm;
           }
           table.people > tbody > tr > td:first-child {
             width: 20%;
           }
           table.people > tbody > tr > td:nth-child(2) {
             width: 20%;
           }
           table.people > tbody > tr > td:nth-child(3) {
             width: 20%;
           }
           table.people > tbody > tr > td:nth-child(4) {
             width: 10%;
             text-align: center;
           }
           table.people > tbody > tr > td:nth-child(5) {
             width: 10%;
             text-align: right;
           }
           table.people > tbody > tr > td:nth-child(6) {
             width: 20%;
             text-align: right;
           }
           table.people1 > tbody > tr:first-child {
             background-color: #FFFFFF;
           }
           table.people1 > tbody > tr > td {
             border-style: none;
           }
           table.people1 > tbody > tr:first-child > td {
             border-top: 2px solid #000000;
             border-bottom: 2px solid #000000;
           }
           table.people2 > tbody > tr:first-child {
             background-color: #E0E0E0;
           }
           table.people2 > tbody > tr > td {
             border: 1px solid #CBCBCB;
           }
           table.people3 > tbody > tr:nth-child(2n+1) {
             background-color: #F0F0F0;
           }
           table.people3 > tbody > tr:first-child {
             background-color: #C0C0C0;
           }
           table.people3 > tbody > tr > td {
             border: 1px solid #B0B0B0;
           }
         </style>
       </head>

       <body>
         <div class="title">Venice PDF Tables</div>
         <div class="subtitle">Example</div>

         <table class="people people1">
           <tbody>
             ${ (doseq [[last first city gender age birth] persons] }$
             <tr>
               <td>${= (kira/escape-xml last) }$</td>
               <td>${= (kira/escape-xml first) }$</td>
               <td>${= (kira/escape-xml city) }$</td>
               <td>${= (kira/escape-xml gender) }$</td>
               <td>${= (kira/escape-xml age) }$</td>
               <td>${= (kira/escape-xml birth test/format-birth-date) }$</td>
             </tr>
             ${ ) }$
           </tbody>
         </table>

         <table class="people people2">
           <tbody>
             ${ (doseq [[last first city gender age birth] persons] }$
             <tr>
               <td>${= (kira/escape-xml last) }$</td>
               <td>${= (kira/escape-xml first) }$</td>
               <td>${= (kira/escape-xml city) }$</td>
               <td>${= (kira/escape-xml gender) }$</td>
               <td>${= (kira/escape-xml age) }$</td>
               <td>${= (kira/escape-xml birth test/format-birth-date) }$</td>
             </tr>
             ${ ) }$
           </tbody>
         </table>

         <table class="people people3">
           <tbody>
             ${ (doseq [[last first city gender age birth] persons] }$
             <tr>
               <td>${= (kira/escape-xml last) }$</td>
               <td>${= (kira/escape-xml first) }$</td>
               <td>${= (kira/escape-xml city) }$</td>
               <td>${= (kira/escape-xml gender) }$</td>
               <td>${= (kira/escape-xml age) }$</td>
               <td>${= (kira/escape-xml birth test/format-birth-date) }$</td>
             </tr>
             ${ ) }$
           </tbody>
         </table>

       </body>
     </html>
     """)

  (def data {
       :persons
           [ [ "Last Name" "First Name" "City"   "Gender" "Age" "Birthdate"                   ]
             [ "Meier"     "Peter"      "Bern"    "m"      42    (time/local-date 1977 10  1) ]
             [ "Schmid"    "Hans"       "Luzern"  "m"      56    (time/local-date 1963  8 12) ]
             [ "Winter"    "Maria"      "Aarau"   "f"      23    (time/local-date 1996  4  8) ]
             [ "Halter"    "Carla"      "Zürich"  "f"       9    (time/local-date 2010  9 28) ] ]
      } )

  ; evaluate the template, render, and save it
  (->> data
       (kira/eval template ["${" "}$"])
       (pdf/render)
       (io/spit "tables-example.pdf"))
)
```

[Generated PDF](https://github.com/jlangch/venice/blob/master/doc/pdfs/tables-example.pdf)

[top](#pdf-generation)



## Images

Images can be loaded from the classpath or from a memory resource pool. The ladder
is used with dynamically created resources like charts.

#### Classpath (static images)

Package the static images in a resource JAR like `resource.jar` and place the JAR
on the Venice's classpath. Refer to the image via a classpath URI:

```html
<img src="classpath:/images/logo.png"/>
```

#### Memory Resource Pool (dynamic images)

Dynamically created images can be passed to the renderer as in-memory resources. 

```clojure
; compute the in-memory resources and give it a names as a reference
(def images { "/images/chart_2018.png" (create-chart  2018)
              "/images/chart_2019.png" (create-chart  2019) } )
  ....
  
; pass the in-memory resources to the renderer
(pdf/render xhtml :resources images)
```

These images are then referred to as:

```html
<img src="memory:/images/chart_2018.png"/>
```

#### Example

```clojure
(do
  (ns test)

  (load-module :kira)
  (load-module :xchart)

  ;; ensure XChart and PDF libs are available when loading this file
  (xchart/check-required-libs)
  (pdf/check-required-libs)



  (defn chart []
    (xchart/to-bytes-with-dpi
      (xchart/xy-chart
        { "a" { :x [0.0  3.0  5.0  7.0  9.0]
                :y [0.0  8.0 12.0  9.0  8.0] }
          "b" { :x [0.0  2.0  4.0  6.0  9.0]
                :y [2.0  9.0  7.0  3.0  7.0] }
          "c" { :x [0.0  1.0  3.0  8.0  9.0]
                :y [1.0  2.0  4.0  3.0  4.0] } }
        { :title "Area Chart"
          :render-style :area
          :legend { :position :inside-ne }
          :x-axis { :title "X" :decimal-pattern "#0.#" }
          :y-axis { :title "Y" :decimal-pattern "#0.#" }
          :theme :xchart } )
      :png
      300))

  ; define the template
  (def template
     """
     <?xml version="1.0" encoding="UTF-8"?>
     <html lang="en" xmlns="http://www.w3.org/1999/xhtml">
       <head>
         <style type="text/css">
           @page {
             size: A4 portrait;
             margin: 2cm 1cm;
             padding: 0;
           }
           body {
             background-color: white;
             font-family: sans-serif;
             font-weight: 400;
           }
           .logo {
             margin-top: 2cm;
             text-align: right;
           }
           .logo img {
             width: 7cm;
           }
           div.title {
             margin: 3cm 0 0 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.subtitle {
             margin: 1cm 0 4cm 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.chart {
             margin-top: 1cm;
             margin-left: 2cm;
             margin-right: 2cm;
             padding: 2mm;
             border: 1px solid #C0C0C0;
           }
           div.chart img {
             width: 100%;
           }
         </style>
       </head>

       <body>
         <div class="logo">
           <img src="classpath:/images/venice.png"/>
         </div>

         <div class="title">Venice PDF Images</div>
         <div class="subtitle">Example</div>

         <div class="chart">
           <img src="memory:/chart_1.png"/>
         </div>
       </body>
     </html>
     """)

  (def data { :title "Hello, world" } )

  ; evaluate the template, render, and save it
  (-<> data
       (kira/eval template ["${" "}$"] <>)
       (pdf/render <> :resources { "/chart_1.png" (chart) })
       (io/spit "image-example.pdf" <>))
)
```

[Generated PDF](https://github.com/jlangch/venice/blob/master/doc/pdfs/image-example.pdf)

[top](#pdf-generation)



## Fonts

PDF supports 14 standard fonts:

- Times (v3) (in regular, italic, bold, and bold italic)
- Courier (in regular, oblique, bold and bold oblique)
- Helvetica (v3) (in regular, oblique, bold and bold oblique)
- Symbol
- Zapf Dingbats

Other fonts have to be made explicitly available to the renderer.
The PDF renderer supports any number of additional True-Type fonts. 
These fonts can be embedded into the PDF to be available on all client 
platforms regardless whether the font is locally installed or not.

The PDF renderer loads custom True-Type fonts from URIs. The custom 
schema `classpath:/` allows the renderer to load the font files from
the class path. E.g.:
- `classpath:/OpenSans-Regular.ttf`
- `classpath:/fonts/OpenSans-Regular.ttf`

A CSS font definition that embeds 'OpenSans-Regular' in the PDF looks 
like:

```css
   @font-face {
      font-family: 'Open Sans';
      src: url('classpath:/fonts/OpenSans-Regular.ttf');
      font-style: normal;
      font-weight: normal;
      -fs-pdf-font-embed: embed;
      -fs-pdf-font-encoding: Identity-H;
   }
```

The font files can be packaged in a resource JAR like `fonts.jar` and be placed
on the Venice's classpath.

A `fonts.jar` file containing the OFL fonts 'Open Sans' and 'Source Code Pro'
may look like:

```text
Length     Date/Time        Name
-------  ---------------- -----------------------------
      0  2019-07-01 13:20 fonts/
 211716  2019-06-20 09:04 fonts/SourceCodePro-Bold.ttf
 213252  2019-06-20 09:04 fonts/SourceCodePro-Light.ttf
 212880  2019-06-20 09:04 fonts/SourceCodePro-Regular.ttf
 224592  2019-06-20 09:04 fonts/OpenSans-Bold.ttf
 212896  2019-06-20 09:04 fonts/OpenSans-Italic.ttf
 217360  2019-06-20 09:04 fonts/OpenSans-Regular.ttf
```

A pre-built `fonts.jar` with these fonts can be downloaded from Venice GitHub 
[Demo Fonts](https://github.com/jlangch/venice/blob/master/doc/pdfs/fonts.jar)

Google hosts Open Source fonts at [Google Fonts](https://fonts.google.com)


```clojure
(do
  (ns test)

  (load-module :kira)

  ;; ensure PDF libs are available when loading this file
  (pdf/check-required-libs)

  ;; ensure the fonts are available when loading this file
  (->> ["fonts/OpenSans-Regular.ttf"
        "fonts/OpenSans-Italic.ttf"
        "fonts/SourceCodePro-Regular.ttf"]
       (docoll (fn [r]
                 (when-not (io/classpath-resource? r)
                   (throw (. :VncException :new
                             "Font classpath resource '~{r}' not found!"))))))


  ; define the template
  (def template
     """
     <?xml version="1.0" encoding="UTF-8"?>
     <html lang="en" xmlns="http://www.w3.org/1999/xhtml">
       <head>
         <style type="text/css">
           @font-face {
              font-family: 'Open Sans';
              src: url('classpath:/fonts/OpenSans-Regular.ttf');
              font-style: normal;
              font-weight: normal;
              -fs-pdf-font-embed: embed;
              -fs-pdf-font-encoding: Identity-H;
           }
           @font-face {
              font-family: 'Open Sans Italic';
              src: url('classpath:/fonts/OpenSans-Italic.ttf');
              font-style: normal;
              font-weight: normal;
              -fs-pdf-font-embed: embed;
              -fs-pdf-font-encoding: Identity-H;
           }
           @font-face {
              font-family: 'Source Code Pro';
              src: url('classpath:/fonts/SourceCodePro-Regular.ttf');
              font-style: normal;
              font-weight: normal;
              -fs-pdf-font-embed: embed;
              -fs-pdf-font-encoding: Identity-H;
           }
           @page {
             size: A4 portrait;
             margin: 2cm 1cm;
             padding: 0;
           }
           body {
             background-color: white;
             font-family: sans-serif;
             font-weight: 400;
           }
           div.title {
             margin: 3cm 0 0 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.subtitle {
             margin: 1cm 0 4cm 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }

           div.head {
             font-family: sans-serif;
             margin-top: 1cm;
             font-weight: 700;
           }
           div.serif {
             font-family: serif;
           }
           div.sans-serif {
             font-family: sans-serif;
           }
           div.open-sans {
             font-family: 'Open Sans', sans-serif;
           }
           div.open-sans-italic {
             font-family: 'Open Sans', sans-serif;
             font-style: italic;
           }
           div.source-code-pro {
             font-family: 'Source Code Pro', monospace;
           }
         </style>
       </head>

       <body>
         <div class="title">Venice PDF Fonts</div>
         <div class="subtitle">Example</div>

         <div class="head">Sans Serif</div>
         <div class="sans-serif">${= (kira/escape-xml text) }$</div>

         <div class="head">Serif</div>
         <div class="serif">${= (kira/escape-xml text) }$</div>

         <div class="head">Open Sans</div>
         <div class="open-sans">${= (kira/escape-xml text) }$</div>

         <div class="head">Open Sans Italic</div>
         <div class="open-sans-italic">${= (kira/escape-xml text) }$</div>

         <div class="head">Source Code Pro</div>
         <div class="source-code-pro">${= (kira/escape-xml text) }$</div>
       </body>
     </html>
     """)

  ; create a Lorem Ipsum text block
  (def data { :text (str/lorem-ipsum :paragraphs 1) } )

  ; Evaluate the template, render, and save it.
  (->> data
       (kira/eval template ["${" "}$"])
       (pdf/render)
       (io/spit "fonts-example.pdf"))
)
```

[Generated PDF](https://github.com/jlangch/venice/blob/master/doc/pdfs/fonts-example.pdf)

[top](#pdf-generation)



## Table of Content

```clojure
(do
  (ns test)

  (load-module :kira)

  ;; ensure PDF libs are available when loading this file
  (pdf/check-required-libs)



  ; define the template
  (def template
     """
     <?xml version="1.0" encoding="UTF-8"?>
     <html lang="en" xmlns="http://www.w3.org/1999/xhtml">
       <head>
         <!-- Bookmarks -->
         <bookmarks>
           <bookmark name="Chapter 1" href="#xil_1"/>
           <bookmark name="Chapter 2" href="#xil_2"/>
           <bookmark name="Chapter 3" href="#xil_3"/>
           <bookmark name="Chapter 4" href="#xil_4"/>
         </bookmarks>

         <style type="text/css">
           @page {
             size: A4 portrait;
             margin: 2cm 1cm;
             padding: 0;
           }
           body {
             background-color: white;
             font-family: sans-serif;
             font-weight: 400;
           }
           a:link, a:visited, a:hover, a:active {
             text-decoration: none;
             background-color: transparent;
             color: #000000;
           }
           h2 {
             color: #000000;
             font-size: 18pt;
             margin: 0 0 1cm 0;
             padding: 0;
           }
           ol.toc {
            list-style-type: none;
           }
           ol.toc ol,
           ol.toc ol ol,
           ol.toc ol ol ol,
           ol.toc ol ol ol ol,
           ol.toc ol ol ol ol ol,
           ol.toc ol ol ol ol ol ol {
             list-style-type: none;
             padding-left: 0;
             margin-left: 0;
           }
           .toc-title {
             margin-top: 4cm;
           }
           .toc {
             margin-top: 2cm;
           }
           .toc a::after {
             content: leader('.') target-counter(attr(href), page);
           }
           .toc li {
             padding-bottom: 6pt;
           }
           .toc li li,
           .toc li li li,
           .toc li li li li,
           .toc li li li li li,
           .toc li li li li li li {
             padding-left: 0.6cm;
             padding-bottom: 0;
           }
           div.page {
             page-break-before: always;
             margin: 0;
             padding: 0;
           }
           div.title {
             margin: 3cm 0 0 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.subtitle {
             margin: 1cm 0 4cm 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.text {
             margin-top: 1cm;
           }
         </style>
       </head>

       <body>
         <div class="title">Venice PDF Table of Content</div>
         <div class="subtitle">Example</div>

         <div class="page">
           <h2 class="toc-title">Content</h2>
           <ol class="toc">
             <li><a href="#xil_1">Chapter 1</a></li>
             <li><a href="#xil_2">Chapter 2</a></li>
             <li><a href="#xil_3">Chapter 3</a></li>
             <li><a href="#xil_4">Chapter 4</a></li>
           </ol>
         </div>

         <div class="page">
           <h2 id="xil_1">Chapter 1</h2>
           <div class="text">${= (kira/escape-xml text) }$</div>
           <div class="text">${= (kira/escape-xml text) }$</div>
         </div>

         <div class="page">
           <h2 id="xil_2">Chapter 2</h2>
           <div class="text">${= (kira/escape-xml text) }$</div>
           <div class="text">${= (kira/escape-xml text) }$</div>
         </div>

         <div class="page">
           <h2 id="xil_3">Chapter 3</h2>
           <div class="text">${= (kira/escape-xml text) }$</div>
           <div class="text">${= (kira/escape-xml text) }$</div>
         </div>

         <div class="page">
           <h2 id="xil_4">Chapter 4</h2>
           <div class="text">${= (kira/escape-xml text) }$</div>
           <div class="text">${= (kira/escape-xml text) }$</div>
         </div>
       </body>
     </html>
     """)

  ; create a Lorem Ipsum text block
  (def data { :text (str/lorem-ipsum :paragraphs 1) } )


  ; evaluate the template, render, and save it
  (->> data
       (kira/eval template ["${" "}$"])
       (pdf/render)
       (io/spit "toc-example.pdf"))
)
```

[Generated PDF](https://github.com/jlangch/venice/blob/master/doc/pdfs/toc-example.pdf)

[top](#pdf-generation)



## Page Footers

```clojure
(do
  (ns test)

  (load-module :kira)

  ;; ensure PDF libs are available when loading this file
  (pdf/check-required-libs)



  ; define the template
  (def template
     """
     <?xml version="1.0" encoding="UTF-8"?>
     <html lang="en" xmlns="http://www.w3.org/1999/xhtml">
       <head>
         <style type="text/css">
           @page :first {
             size: A4 portrait;
             margin: 2cm 1cm;
             padding: 0;
             @bottom-left { content: element(footer_first); }
             @bottom-right { }
           }
           @page {
             size: A4 portrait;
             margin: 2cm 1cm;
             padding: 0;
             @bottom-left { content: element(footer_left); }
             @bottom-right { content: element(footer_right); }
           }
           body {
             background-color: white;
             font-family: sans-serif;
             font-weight: 400;
           }
           div.title {
             margin: 3cm 0 0 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.subtitle {
             margin: 1cm 0 4cm 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.text {
             margin-top: 1cm;
           }
           hr {
             border: none;
             height: 1px;
             background-color: #303030;
           }
           span.page:before {
             content: counter(page);
           }
           span.pagecount:before {
             content: counter(pages);
           }
           #footer_first {
             position: running(footer_first);
             font-size: 8pt;
             text-align: center;
           }
           #footer_left {
             position: running(footer_left);
             text-align: left;
           }
           #footer_right {
             position: running(footer_right);
             text-align: right;
             padding-right: 2mm;
           }
         </style>
       </head>

       <body>
         <!-- Footer -->
         <div id="footer_first"><hr/>${= (kira/escape-xml footer-front-page) }$</div>
         <div id="footer_left"><hr/>${= (kira/escape-xml footer-left) }$</div>
         <div id="footer_right"><hr/><span class="page"/> / <span class="pagecount"/></div>

         <div class="title">Venice PDF Footer</div>
         <div class="subtitle">Example</div>

         <div style="page-break-before: always;"/>
         <div class="text">${= (kira/escape-xml text) }$</div>
         <div class="text">${= (kira/escape-xml text) }$</div>

         <div style="page-break-before: always;"/>
         <div class="text">${= (kira/escape-xml text) }$</div>
         <div class="text">${= (kira/escape-xml text) }$</div>

         <div style="page-break-before: always;"/>
         <div class="text">${= (kira/escape-xml text) }$</div>
         <div class="text">${= (kira/escape-xml text) }$</div>

         <div style="page-break-before: always;"/>
         <div class="text">${= (kira/escape-xml text) }$</div>
         <div class="text">${= (kira/escape-xml text) }$</div>
       </body>
     </html>
     """)

  ; create a Lorem Ipsum text block
  (def data { :footer-front-page "Aarestrasse 51, 3012 Bern, Tel. 099 100 20 30, Fax 099 100 20 31, info@foo.ch, www.foo.ch"
              :footer-left "Demo"
              :text (str/lorem-ipsum :paragraphs 1) } )


  ; evaluate the template, render, and save it
  (->> data
       (kira/eval template ["${" "}$"])
       (pdf/render)
       (io/spit "footer-example.pdf"))
)
```

[Generated PDF](https://github.com/jlangch/venice/blob/master/doc/pdfs/footer-example.pdf)

[top](#pdf-generation)



## Watermarks

```clojure
(do
  (ns test)

  (load-module :kira)

  ;; ensure PDF libs are available when loading this file
  (pdf/check-required-libs)



  ; define the template
  (def template
     """
     <?xml version="1.0" encoding="UTF-8"?>
     <html lang="en" xmlns="http://www.w3.org/1999/xhtml">
       <head>
         <style type="text/css">
           @page {
             size: A4 portrait;
             margin: 2cm 1cm;
             padding: 0;
           }
           body {
             background-color: white;
             font-family: sans-serif;
             font-weight: 400;
           }
           div.title {
             margin: 3cm 0 0 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.subtitle {
             margin: 1cm 0 4cm 0;
             text-align: center;
             font-size: 24pt;
             font-weight: 600;
           }
           div.text {
             margin-top: 1cm;
           }
         </style>
       </head>

       <body>
         <div class="title">Venice PDF Watermarks</div>
         <div class="subtitle">Example</div>

         <div style="page-break-before: always;"/>
         <div class="text">${= (kira/escape-xml text) }$</div>
         <div class="text">${= (kira/escape-xml text) }$</div>

         <div style="page-break-before: always;"/>
         <div class="text">${= (kira/escape-xml text) }$</div>
         <div class="text">${= (kira/escape-xml text) }$</div>

         <div style="page-break-before: always;"/>
         <div class="text">${= (kira/escape-xml text) }$</div>
         <div class="text">${= (kira/escape-xml text) }$</div>

         <div style="page-break-before: always;"/>
         <div class="text">${= (kira/escape-xml text) }$</div>
         <div class="text">${= (kira/escape-xml text) }$</div>
       </body>
     </html>
     """)

  ; create a Lorem Ipsum text block
  (def data { :text (str/lorem-ipsum :paragraphs 1) } )

  (def watermark { :text              "CONFIDENTIAL"
                   :font-size         64.0
                   :font-char-spacing 10.0
                   :color             "#000000"
                   :opacity           0.4
                   :outline-color     "#000000"
                   :outline-opacity   0.8
                   :outline-width     0.5
                   :angle             45.0
                   :over-content      true
                   :skip-top-pages    1
                   :skip-bottom-pages 0 })

  ; evaluate the template, render, and save it
  (-<> data
       (kira/eval template ["${" "}$"] <>)
       (pdf/render <>)
       (pdf/watermark <> watermark)
       (io/spit "watermark-example.pdf" <>))
)
```

[Generated PDF](https://github.com/jlangch/venice/blob/master/doc/pdfs/watermark-example.pdf)

[top](#pdf-generation)



## Text to PDF

Venice supports simplified text to PDF

```clojure
(do 
  (ns test)
  
  (load-module :kira)
  
  ; define the template
  (def text 
     """
     Lorem Ipsum is simply dummy text of the printing and typesetting
     industry. Lorem Ipsum has been the industry's standard dummy
     text ever since the 1500s, when an unknown printer took a galley
     of type and scrambled it to make a type specimen book. It has
     survived not only five centuries, but also the leap into electronic
     typesetting, remaining essentially unchanged. It was popularised in
     the 1960s with the release of Letraset sheets containing Lorem
     Ipsum passages, and more recently with desktop publishing
     software like Aldus PageMaker including versions of Lorem Ipsum.
     
     x
      x x
       x x
        x x
         x x
          x x x
     <form-feed>
     Lorem Ipsum is simply dummy text of the printing and typesetting
     industry. Lorem Ipsum has been the industry's standard dummy
     text ever since the 1500s, when an unknown printer took a galley
     of type and scrambled it to make a type specimen book. It has
     survived not only five centuries, but also the leap into electronic
     typesetting, remaining essentially unchanged. It was popularised in
     the 1960s with the release of Letraset sheets containing Lorem
     Ipsum passages, and more recently with desktop publishing
     software like Aldus PageMaker including versions of Lorem Ipsum.
     """)

  ; render the PDF, and save it
  (let [pdf (pdf/text-to-pdf text :font-size 10 
                                  :font-weight 300 
                                  :font-monospace false)]
    (io/spit "text2pdf-example.pdf" pdf)))
```

[Generated PDF](https://github.com/jlangch/venice/blob/master/doc/pdfs/text2pdf-example.pdf)

[top](#pdf-generation)



## Merge PDFs

Merge multiple PDFs into a single one

```clojure
(do 
  (let [pdf (pdf/merge (pdf/text-to-pdf "PDF #1" :font-size 64)
                       (pdf/text-to-pdf "PDF #2" :font-size 64)
                       (pdf/text-to-pdf "PDF #3" :font-size 64)
                       (pdf/text-to-pdf "PDF #4" :font-size 64)
                       (pdf/text-to-pdf "PDF #5" :font-size 64))]
    (io/spit "merge-example.pdf" pdf)))
```

[Generated PDF](https://github.com/jlangch/venice/blob/master/doc/pdfs/merge-example.pdf)

[top](#pdf-generation)



## Copy PDF

Copy pages from a PDF to a new PDF

```clojure
(do 
  (let [pdf (pdf/merge (pdf/text-to-pdf "PDF #1" :font-size 64)
                       (pdf/text-to-pdf "PDF #2" :font-size 64)
                       (pdf/text-to-pdf "PDF #3" :font-size 64)
                       (pdf/text-to-pdf "PDF #4" :font-size 64)
                       (pdf/text-to-pdf "PDF #5" :font-size 64))]
    
    ;; resulting pages #1, #1, #1, #2, #3, #4, #5, #4, #3
    ;;                 :1  :1  :1  :2-4        :-1 :-2 :-3
    ;; ---------------------------------------------------------
    (->> (pdf/copy pdf :1 :1 :1 :2-4 :-1 :-2 :-3)
         (io/spit "copy-example.pdf"))))
```

[Generated PDF](https://github.com/jlangch/venice/blob/master/doc/pdfs/copy-example.pdf)

[top](#pdf-generation)
