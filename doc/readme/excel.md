# Excel

Venice's *:excel* module provides functions to read and write Excel files. 
It is based on the [Apache POI](https://poi.apache.org/) library.

Venice is compatible with Apache POI 5.x. To use charts 
with Excel documents Apache POI 5.2.0 or newer is required.

*Note:* Use Venice 1.12.34+ or newer to have all features demonstrated here
available.

Run this script from the REPL to download the Apache POI 5.4.0 
libraries:

```clojure
(do
  (load-module :excel-install)
  
  ;; Download the EXCEL libs from Maven
  (excel-install/install :dir (repl/libs-dir) :silent false)

  ;; Restart the REPL to make the new libs available to the REPL Java VM
  (println "\Restarting REPL...")
  (repl/restart))
```

</br>

## Content

1. [Writing Excel files](#writing-excel-files)
    * [Introduction Example](#introduction-example)
    * [Create and Modify](#create-and-modify)
       * [Create New Excel](#create-new-excel)
       * [Modify Existing Excel](#modify-existing-excel)
    * [Writing Modes](#writing-modes)
       * [Write tabular data](#write-tabular-data)
       * [Write single cell values](#write-single-cell-values)
       * [Write raw 2D vector data](#write-raw-2d-vector-data)
    * [Write to file, stream, bytes](#write-to-file-stream-bytes)
       * [Write to a file](#write-to-a-file)
       * [Write to an output stream](#write-to-an-output-stream)
       * [Write to a byte buffer](#write-to-a-byte-buffer)
    * [Examples](#write-examples)
       * [Omit the header row](#omit-the-header-row)
       * [Write to multiple sheets](#write-to-multiple-sheets)
       * [Supported data types](#supported-data-types)
       * [Insert, Copy, Clear and Delete Rows](#insert-copy-clear-and-delete-rows)
       * [Merge Cells](#merge-cells)
       * [Row and Col Ranges](#row-and-col-ranges)
       * [Formulas](#formulas)
       * [Hyperlinks](#hyperlinks)
       * [Images](#images)
       * [Charts](#charts)
    * [Styling](#styling)
       * [Page Setup, Headers, Footers](#page-setup-headers-footers)
       * [Row height](#row-height)
       * [Col width](#col-width)
       * [Fonts](#fonts)
       * [Cell Styles](#cell-styles)
       * [Background colors](#background-colors)
       * [Shading alternate rows](#shading-alternate-rows)
       * [Styling cells](#styling-cells)
       * [Styling cell region](#styling-cell-region)
       * [Freeze Panes](#freeze-panes)
2. [Reading Excel files](#reading-excel-files)
    * [Open Excel](#open-excel)
    * [Reading Cell Metadata](#reading-cell-metadata)
    * [Reading Cell Data Type](#reading-cell-data-type)
    * [Reading Cells](#reading-cells)

</br>

## Writing Excel files

The function `excel/writer` opens a new XLS or XLSX Excel file for writing. 


### Introduction Example

The easiest way to write data to an EXCEL is passing pre-processed ready made data in
a table data set and map the sheet columns to the map keys in the table.

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:first "John" :last "Doe"   :age 28 }
                {:first "Sue"  :last "Ford"  :age 26 } ]
        wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-column sheet "First Name" { :field :first })
    (excel/add-column sheet "Last Name" { :field :last })
    (excel/add-column sheet "Age" { :field :age })
    (excel/write-items sheet data)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-001.png" width="400">

To create an XLSX use `(excel/create :xlsx)` to create a workbook. To create an 
old style XLS use `(excel/writer :xls)` and ensure it is written to a file with 
the `.xls` file name extension.

[top](#content)



### Create and Modify

#### Create New Excel

Creating a new Excel from scratch (create-modify-write).

```clojure
(do
  (ns test)

  (load-module :excel)
  
  ;; create a new Excel, modify and save it as "sample.xlsx"
  (let [wbook  (excel/create :xlsx)
        sheet1 (excel/add-sheet wbook "Data1")
        sheet2 (excel/add-sheet wbook "Data2")]
    (excel/write-values sheet1 1 1 "John" "Doe" 28)
    (excel/write-values sheet1 2 1 "Sue" "Ford" 26)
    (excel/write-values sheet2 1 1 "France" "Paris")
    (excel/write-values sheet2 2 1 "Italy" "Rome")
    (excel/write->file wbook "sample.xlsx")))
```

[top](#content)


#### Modify Existing Excel

Modify an existing Excel (read-modify-write).

```clojure
(do
  (ns test)

  (load-module :excel)
  
  ;; create a new Excel and save it as "sample.xlsx"
  (let [wbook  (excel/create :xlsx)
        sheet1 (excel/add-sheet wbook "Data1")
        sheet2 (excel/add-sheet wbook "Data2")]
    (excel/write-values sheet1 1 1 "John" "Doe" 28)
    (excel/write-values sheet1 2 1 "Sue" "Ford" 26)
    (excel/write-values sheet2 1 1 "France" "Paris")
    (excel/write-values sheet2 2 1 "Italy" "Rome")
    (excel/write->file wbook "sample.xlsx"))

  ;; load the Excel from "sample.xlsx", modify, and save it as "sample1.xlsx"
  (let [wbook (excel/open "sample.xlsx")
        sheet (excel/sheet wbook 1)]        
    (excel/write-value sheet 1 3 38)  ;; correct John Doe's age 28 -> 38
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample1.xlsx")))
```

[top](#content)


### Writing Modes

The Excel module supports three writing modes:
 
   * [Write tabular data](#write-tabular-data)
   * [Write single cell values](#write-single-cell-values)
   * [Write raw 2D vector data](#write-raw-2d-vector-data)

All writing modes support cell styling.


#### Write tabular data

The tabular data is passed as a sequence of maps. Each sequence item is written to an Excel
row and the item is written to column cells in the row. The columns are explicitly defined.

> [!NOTE] 
If your date does not come in the required data format just apply `filter` and `map` to convert your data

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [os    (io/file-out-stream "sample.xlsx")
        data  [ {:first "John" :last "Doe"   :age 28 }
                {:first "Sue"  :last "Ford"  :age 26 } ]
        wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-column sheet "First Name" { :field :first })
    (excel/add-column sheet "Last Name" { :field :last })
    (excel/add-column sheet "Age" { :field :age })
    (excel/write-items sheet data)
    (excel/auto-size-columns sheet)
    (excel/write->stream wbook os)))
```

[top](#content)

 
#### Write single cell values

The function `excel/write-value` writes values to cells. The row and col numbers are 1-based!

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/write-value sheet 1 1 "John")
    (excel/write-value sheet 1 2 "Doe")
    (excel/write-value sheet 1 3 28)
    (excel/write-value sheet 2 1 "Sue")
    (excel/write-value sheet 2 2 "Ford")
    (excel/write-value sheet 2 3 26)
    (excel/auto-size-columns (excel/sheet wbook "Sheet 1"))
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-003.png" width="400">

</br>

The function `excel/write-values` writes multiple values to consecutive cells in row starting at a column. The row and col numbers are 1-based!

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/write-values sheet 1 1 "John" "Doe" 28)
    (excel/write-values sheet 2 1 "Sue" "Ford" 26)
    (excel/auto-size-columns (excel/sheet wbook "Sheet 1"))
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-003.png" width="400">

[top](#content)


#### Write raw 2D vector data
 
Write the data of a 2D vector to an excel sheet.

```clojure
(do
  (load-module :excel)
  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Data")
        dt    (time/local-date 2021 1 1)
        ts    (time/local-date-time 2021 1 1 15 30 45)
        data  [[100  101  102  103  104  105]
               [200  "ab" 1.23 dt   ts   false]]]
    (excel/write-data sheet data)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-005.png" width="400">

[top](#content)


Write the data starting at a row/col offset:

```clojure
(do
  (load-module :excel)
  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Data")]
    (excel/write-data sheet [[100 101 102] [200 201 203]])
    (excel/write-data sheet [[300 301 302] [400 401 403]] 3 4)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-012.png" width="400">

[top](#content)



### Write to file, stream, bytes

#### Write to a file

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:first "John" :last "Doe"   :age 28 }
                {:first "Sue"  :last "Ford"  :age 26 } ]
        wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-column sheet "First Name" { :field :first })
    (excel/add-column sheet "Last Name" { :field :last })
    (excel/add-column sheet "Age" { :field :age })
    (excel/write-items sheet data)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

[top](#content)


#### Write to an output stream

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [os    (io/file-out-stream "sample.xlsx")
        data  [ {:first "John" :last "Doe"   :age 28 }
                {:first "Sue"  :last "Ford"  :age 26 } ]
        wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-column sheet "First Name" { :field :first })
    (excel/add-column sheet "Last Name" { :field :last })
    (excel/add-column sheet "Age" { :field :age })
    (excel/write-items sheet data)
    (excel/auto-size-columns sheet)
    (excel/write->stream wbook os)))
```

[top](#content)


#### Write to a byte buffer

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:first "John" :last "Doe"   :age 28 }
                {:first "Sue"  :last "Ford"  :age 26 } ]
        wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-column sheet "First Name" { :field :first })
    (excel/add-column sheet "Last Name" { :field :last })
    (excel/add-column sheet "Age" { :field :age })
    (excel/write-items sheet data)
    (excel/auto-size-columns sheet)
    (excel/write->bytebuf wbook)))
```

[top](#content)


### Write Examples


#### Omit the header row

To omit the header row pass the option `:no-header-row true` to the excel sheet:

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:first "John" :last "Doe"   :age 28 }
                {:first "Sue"  :last "Ford"  :age 26 } ]
        wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1" { :no-header-row true })]
    (excel/add-column sheet "First Name" { :field :first })
    (excel/add-column sheet "Last Name" { :field :last })
    (excel/add-column sheet "Age" { :field :age })
    (excel/write-items sheet data)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-002.png" width="400">

[top](#content)



#### Write to multiple sheets

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data1  [ {:first "John" :last "Doe"   :age 28 }
                 {:first "Sue"  :last "Ford"  :age 26 } ]
        data2  [ {:first "Mark" :last "Smith" :age 40 }
                 {:first "Mary" :last "Jones" :age 42 } ]
        wbook  (excel/create :xlsx)
        sheet1 (excel/add-sheet wbook "Sheet 1")
        sheet2 (excel/add-sheet wbook "Sheet 2")]
    (excel/add-column sheet1 "First Name" { :field :first })
    (excel/add-column sheet1 "Last Name" { :field :last })
    (excel/add-column sheet1 "Age" { :field :age })
    (excel/add-column sheet2 "First Name" { :field :first })
    (excel/add-column sheet2 "Last Name" { :field :last })
    (excel/add-column sheet2 "Age" { :field :age })
    (excel/write-items sheet1 data1)
    (excel/write-items sheet2 data2)
    (excel/auto-size-columns sheet1)
    (excel/auto-size-columns sheet2)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-026a.png" width="400">

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-026b.png" width="400">

[top](#content)



#### Supported data types

The Excel writer supports the Venice data types:

 - string
 - integer
 - long
 - double
 - boolean
 - date (:java.time.LocalDate)
 - datetime (:java.time.LocalDateTime)
 
```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:t-str "text" 
                 :t-int 100I 
                 :t-long 200 
                 :t-double 1.23  
                 :t-bool true 
                 :t-date (time/local-date 2021 1 1)
                 :t-datetime (time/local-date-time 2021 1 1 15 30 45) } ]
        wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-column sheet "string" { :field :t-str })
    (excel/add-column sheet "integer" { :field :t-int })
    (excel/add-column sheet "long" { :field :t-long })
    (excel/add-column sheet "double" { :field :t-double })
    (excel/add-column sheet "boolean" { :field :t-bool })
    (excel/add-column sheet "date" { :field :t-date })
    (excel/add-column sheet "datetime" { :field :t-datetime })
    (excel/write-items sheet data)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-006.png" width="400">

[top](#content)





#### Insert, Copy, Clear and Delete Rows

```clojure
(do
  (ns test)

  (load-module :excel)
  
  (defn create-excel []
    (let [wbook (excel/create :xlsx)
          sheet (excel/add-sheet wbook "Sheet 1")]
      (excel/write-values sheet 1 1 "John" "Doe" 28)
      (excel/write-values sheet 2 1 "Sue" "Ford" 26)
      (excel/write-values sheet 3 1 "Peter" "Adams" 45"Teacher")
      (excel/auto-size-columns (excel/sheet wbook "Sheet 1"))
      (excel/write->file wbook "sample.xlsx")))
     
   (create-excel)
    
   (let [wbook (excel/open "sample.xlsx")
         sheet (excel/sheet wbook "Sheet 1")]        
     (excel/copy-row-to-end sheet 1)
     (excel/copy-row-to-end sheet 1)
     (excel/copy-row-to-end sheet 1)
     (excel/copy-row sheet 2 3)
     (excel/clear-row sheet 4)
     (excel/delete-row sheet 5)
     (excel/insert-empty-row sheet 2)    
     (excel/write->file wbook "sample1.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-024.png" width="400">

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-025.png" width="400">

[top](#content)



#### Merge cells

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Population")]
    (excel/col-width sheet 2 70)
    (excel/col-width sheet 3 70)
    (excel/add-merge-region sheet 2 2 2 3)
    (excel/write-value sheet 2 2 "Contry Population")
    (excel/write-value sheet 3 2 "Country")
    (excel/write-value sheet 3 3 "Population")
    (excel/write-value sheet 4 2 "Germany")
    (excel/write-value sheet 4 3 83_783_942)
    (excel/write-value sheet 5 2 "Italy")
    (excel/write-value sheet 5 3 60_461_826)
    (excel/write-value sheet 6 2 "Austria")
    (excel/write-value sheet 6 3 9_006_398)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-010.png" width="400">

[top](#content)



#### Row and Col Ranges

**Row range**

```clojure
(do
  (load-module :excel)

  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/write-values sheet 1 1 "John" "Doe" 28)
    (excel/write-values sheet 2 1 "Mary" "Smith" 28)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx"))

  (let [wbook (excel/open "sample.xlsx")
        sheet (excel/sheet wbook "Sheet 1")]
    (excel/sheet-row-range sheet)))
```

Prints: `[1 2]`


**Col range**

```clojure
(do
  (load-module :excel)

  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/write-values sheet 1 1 "John" "Doe" 28)
    (excel/write-values sheet 2 1 "Mary" "Smith" 28)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx"))

  (let [wbook (excel/open "sample.xlsx")
        sheet (excel/sheet wbook "Sheet 1")]
    (excel/sheet-col-range sheet 1)))
```

Prints: `[1 3]`






#### Formulas

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:a 100 :b 200 }
                {:a 101 :b 201 }
                {:a 102 :b 202 } ]
        wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1" { :no-header-row true })]
    (excel/add-column sheet "A" { :field :a })
    (excel/add-column sheet "B" { :field :b })
    (excel/add-column sheet "C" { :field :c })
    (excel/write-items sheet data)
    (excel/cell-formula sheet 1 3 "SUM(A1,B1)")
    (excel/cell-formula sheet 2 3 "SUM(A2,B2)")
    (excel/cell-formula sheet 3 3 "SUM(A3,B3)")
    (excel/evaluate-formulas wbook)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

This can be further simplified to:

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:a 100, :b 200, :c {:formula "SUM(A1,B1)"}}
                {:a 101, :b 201, :c {:formula "SUM(A2,B2)"}}
                {:a 102, :b 202, :c {:formula "SUM(A3,B3)"}} ]
        wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1" { :no-header-row true })]
    (excel/add-column sheet "A" { :field :a })
    (excel/add-column sheet "B" { :field :b })
    (excel/add-column sheet "C" { :field :c })
    (excel/write-items sheet data)
    (excel/evaluate-formulas wbook)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-004.png" width="400">

Venice provides the function `excel/addr->string` to help with building logical 'A1' style cell addresses
for formulas:

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:a 100 :b 200 }
                {:a 101 :b 201 }
                {:a 102 :b 202 } ]
        wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1" { :no-header-row true })
        sum   #(str/format "SUM(%s,%s)" 
                           (excel/addr->string (first %1) (second %1))
                           (excel/addr->string (first %2) (second %2)))]
    (excel/add-column sheet "A" { :field :a })
    (excel/add-column sheet "B" { :field :b })
    (excel/add-column sheet "C" { :field :c })
    (excel/write-items sheet data)
    (excel/cell-formula sheet 1 3 (sum [1 1] [1 2]))
    (excel/cell-formula sheet 2 3 (sum [2 1] [2 2]))
    (excel/cell-formula sheet 3 3 (sum [3 1] [3 2]))
    (excel/evaluate-formulas wbook)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

[top](#content)



#### Hyperlinks

*URLs*

```clojure
(do
  (load-module :excel)
  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-font wbook :hyperlink { :underline true
                                       :color :BLUE })
    (excel/add-style wbook :hyperlink { :font :hyperlink })
    (excel/write-values sheet 1 1 "John" "Doe")
    (excel/write-values sheet 2 1 "Sue" "Ford")
    (excel/add-url-hyperlink sheet 1 3 "https://john.doe.org/" "https://john.doe.org/")
    (excel/add-url-hyperlink sheet 2 3 "https://sue.ford.org/" "https://sue.ford.org/")
    (excel/cell-style sheet 1 3 :hyperlink)
    (excel/cell-style sheet 2 3 :hyperlink)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-027.png" width="400">

*EMails*

```clojure
(do
  (load-module :excel)
  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-font wbook :hyperlink { :underline true
                                       :color :BLUE })
    (excel/add-style wbook :hyperlink { :font :hyperlink })
    (excel/write-values sheet 1 1 "John" "Doe")
    (excel/write-values sheet 2 1 "Sue" "Ford")
    (excel/add-email-hyperlink sheet 1 3 "john.doe@foo.org" "john.doe@foo.org")
    (excel/add-email-hyperlink sheet 2 3 "sue.ford@foo.org" "sue.ford@foo.org")
    (excel/cell-style sheet 1 3 :hyperlink)
    (excel/cell-style sheet 2 3 :hyperlink)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-028.png" width="400">


[top](#content)


#### Images

```clojure
(do
  (load-module :excel)
  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")
        image "com/github/jlangch/venice/images/venice.png"
        data  (io/load-classpath-resource image)]
    (excel/add-image sheet  2 2 data :PNG)
    (excel/add-image sheet 12 2 data :PNG 0.8 0.8)  ;; scale by factor 0.8
    (excel/add-image sheet 20 2 data :PNG 0.4 0.4)  ;; scale by factor 0.4
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-019.png" width="400">

[top](#content)


#### Charts

**Line Chart**

```clojure
(do
  (load-module :excel)
  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")
        data  [[""       "Bears" "Dolphins" "Whales"]
               ["2017"        8        150       80 ]
               ["2018"       54         77       54 ]
               ["2019"       93         32      100 ]
               ["2020"      116         11       76 ]
               ["2021"      137          6       93 ]
               ["2022"      184          1       72 ]]]
    (excel/write-data sheet data)

    (excel/add-line-chart sheet
                          "Wildlife Population"                   ;; chart title 
                          (excel/cell-address-range 10 25 1 10)   ;; chart position
                          :RIGHT                                  ;; legend position
                          "Year"                                  ;; category axis title 
                          :BOTTOM                                 ;; category axis position
                          "Population"                            ;; value axis title
                          :LEFT                                   ;; value axis position
                          false                                   ;; 3D
                          true                                    ;; vary colors
                          (excel/cell-address-range 2 7 1 1)      ;; category names
                          [ (excel/line-data-series
                               "Bears"
                               true
                               :CIRCLE
                               (excel/cell-address-range 2 7 2 2))
                            (excel/line-data-series
                               "Dolphins"
                               true
                               :CIRCLE
                               (excel/cell-address-range 2 7 3 3))
                            (excel/line-data-series
                               "Whales"
                               true
                               :CIRCLE
                               (excel/cell-address-range 2 7 4 4)) ])

    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-020.png" width="400">


**Bar Chart**

```clojure
(do
  (load-module :excel)
  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")
        data  [[""       "Bears" "Dolphins" "Whales"]
               ["2017"        8        150       80 ]
               ["2018"       54         77       54 ]
               ["2019"       93         32      100 ]
               ["2020"      116         11       76 ]
               ["2021"      137          6       93 ]
               ["2022"      184          1       72 ]]]
    (excel/write-data sheet data)

    (excel/add-bar-chart sheet
                         "Wildlife Population"                   ;; chart title 
                         (excel/cell-address-range 10 25 1 7)    ;; chart position
                         :RIGHT                                  ;; legend position
                         "Year"                                  ;; category axis title 
                         :BOTTOM                                 ;; category axis position
                         "Population"                            ;; value axis title
                         :LEFT                                   ;; value axis position
                         false                                   ;; 3D
                         false                                   ;; bar horizontal
                         :STANDARD                               ;; grouping
                         false                                   ;; vary colors
                         (excel/cell-address-range 2 7 1 1)      ;; category names
                         [ (excel/bar-data-series
                             "Bears"
                             (excel/cell-address-range 2 7 2 2))
                           (excel/bar-data-series
                             "Dolphins"
                             (excel/cell-address-range 2 7 3 3))
                           (excel/bar-data-series
                             "Whales"
                             (excel/cell-address-range 2 7 4 4)) ])

    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-021.png" width="400">


**Area Chart**

```clojure
(do
  (load-module :excel)
  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")
        data  [[""       "Bears" "Dolphins" "Whales"]
               ["2017"        8        150       80 ]
               ["2018"       54         77       54 ]
               ["2019"       93         32      100 ]
               ["2020"      116         11       76 ]
               ["2021"      137          6       93 ]
               ["2022"      184          1       72 ]]]
    (excel/write-data sheet data)

    (excel/add-area-chart sheet
                          "Bears Population"                      ;; chart title
                          (excel/cell-address-range 10 25 1 7)    ;; chart position
                          :RIGHT                                  ;; legend position
                          "Year"                                  ;; category axis title 
                          :BOTTOM                                 ;; category axis position
                          "Population"                            ;; value axis title
                          :LEFT                                   ;; value axis position
                          false                                   ;; 3D
                          (excel/cell-address-range 2 7 1 1)      ;; category names
                          [ (excel/area-data-series
                              "Bears"
                              (excel/cell-address-range 2 7 2 2)) ])

    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-022.png" width="400">


**Pie Chart**

```clojure
(do
  (load-module :excel)
  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")
        data  [[""       "Bears" "Dolphins" "Whales"]
               ["2017"        8        150       80 ]
               ["2018"       54         77       54 ]
               ["2019"       93         32      100 ]
               ["2020"      116         11       76 ]
               ["2021"      137          6       93 ]
               ["2022"      184          1       72 ]]]
    (excel/write-data sheet data)

    (excel/add-pie-chart sheet
                         "Wildlife Population 2017"              ;; chart title 
                         (excel/cell-address-range 10 25 1 7)    ;; chart position
                         :RIGHT                                  ;; legend position
                         false                                   ;; 3D
                         true                                    ;; vary colors
                         (excel/cell-address-range 1 1 2 4)      ;; category names
                         [ (excel/pie-data-series
                             (excel/cell-address-range 2 2 2 4)) ])

    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-023.png" width="400">

[top](#content)




### Styling

#### Page Setup, Headers, Footers

Excel page model:

```
        +--- 1)                                                      +--- 2)
      /                                                            /
+----+------------------------------------------------------------+----+
|    |                                                            |    |   
+----+------------------+----------------------+------------------+----+ --- 5)
|    |   LEFT HEADER    |     CENTER HEADER    |   RIGHT HEADER   |    | 
+----+------------------+----------------------+------------------+----+ --- 3)
|    |                                                            |    |
|    |                                                            |    |       1)  page margin left
|    |                                                            |    |       2)  page margin right
|    |                          EXCEL CELLS                       |    |       3)  page margin top
|    |                                                            |    |       4)  page margin bottom
|    |                                                            |    |       5)  header margin
|    |                                                            |    |       6)  footer margin
|    |                                                            |    |
+----+------------------+----------------------+------------------+----+ --- 4)
|    |   LEFT FOOTER    |     CENTER FOOTER    |   RIGHT FOOTER   |    |
+----+------------------+----------------------+------------------+----+ --- 6)
|    |                                                            |    |
+----+------------------------------------------------------------+----+

note: all margins in inches
```


```clojure
(do
  (load-module :excel)
  (let [wbook       (excel/create :xlsx)
        sheet       (excel/add-sheet wbook "Data")
        fit-width?  true
        grid-lines? true]
    (excel/write-values sheet 1 1 "John" "Doe" 28)
    (excel/write-values sheet 2 1 "Sue" "Ford" 26)
    (excel/auto-size-columns sheet)
    (excel/print-layout sheet :A4 :LANDSCAPE fit-width? grid-lines? 1.25 1.25)
    (excel/page-margins sheet 1.25 1.25 2.5 2.5)
    (excel/header sheet "H/LEFT" :LEFT 24 true)
    (excel/header sheet "H/CENTER" :CENTER 24 true)
    (excel/header sheet "H/RIGHT" :RIGHT 24 true)
    (excel/footer sheet "F/LEFT" :LEFT 24 true)
    (excel/footer sheet "F/CENTER" :CENTER 24 true)
    (excel/footer sheet "F/RIGHT" :RIGHT 24 true)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-page-layout-001.png" width="400">


**Customizing header and footer**

```clojure
(do
  (load-module :excel)
  (let [wbook       (excel/create :xlsx)
        sheet       (excel/add-sheet wbook "Data")
        fit-width?  true
        grid-lines? true]
    (excel/write-values sheet 1 1 "John" "Doe" 28)
    (excel/write-values sheet 2 1 "Sue" "Ford" 26)
    (excel/auto-size-columns sheet)
    (excel/print-layout sheet :A4 :LANDSCAPE fit-width? grid-lines? 1.25 1.25)
    (excel/page-margins sheet 1.25 1.25 2.5 1.25)
    (excel/header sheet "Example Report" :CENTER 24 true)
    (excel/footer sheet "{date}  {time}" :LEFT 11 false)
    (excel/footer sheet "{page} / {num-pages}" :RIGHT 11 false)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-page-layout-002.png" width="400">

[top](#content)


#### Row height

Set the height of individual rows:

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:first "John" :last "Doe"   :age 28 }
                {:first "Sue"  :last "Ford"  :age 26 } ]
        wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-column sheet "First Name" { :field :first })
    (excel/add-column sheet "Last Name" { :field :last })
    (excel/add-column sheet "Age" { :field :age })
    (excel/write-items sheet data)
    
    (excel/row-height sheet 2 50) ;; set the height of row 2 to 50
    
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-007.png" width="400">

[top](#content)



#### Col width

Set the width of individual columns:

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [os    (io/file-out-stream "sample.xlsx")
        data  [ {:first "John" :last "Doe"   :age 28 }
                {:first "Sue"  :last "Ford"  :age 26 } ]
        wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-column sheet "First Name" { :field :first })
    (excel/add-column sheet "Last Name" { :field :last })
    (excel/add-column sheet "Age" { :field :age })
    (excel/write-items sheet data)
    
    ;; column width
    (excel/col-width sheet 1 80)
    (excel/col-width sheet 2 80)
    (excel/col-width sheet 3 60)
    
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-017.png" width="400">

[top](#content)



#### Fonts

Define a named font with optional attributes on the workbook.

```
  (excel/add-font wbook :header-font { :height 12
                                       :bold true
                                       :italic false
                                       :color :BLUE })
```

| Option    | Description                           |
| --------- | ------------------------------------- |
| :name s   | font name, e.g. 'Arial'               |
| :height n | height in points, e.g. 12             |
| :bold b   | bold, e.g. true, false                |
| :italic b | italic, e.g. true, false              |
| :color c  | color, either an Excel indexed color<br>or a HTML color, e.g. :PLUM, "#00FF00"<br>Note: only XLSX supports 24 bit colors |


```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:first "John" :last "Doe"   :age 28 }
                {:first "Sue"  :last "Ford"  :age 26 } ]
        wbook (excel/create :xlsx)]
        
    ;; define a font ':header-font'
    (excel/add-font wbook :header-font { :height 12
                                         :bold true
                                         :italic false
                                         :color :BLUE })
   
    ;; define a sheet style ':header' referencing the font ':header-font'
    (excel/add-style wbook :header { :font :header-font })

    (let [sheet (excel/add-sheet wbook "Sheet 1"
                                 { :default-header-style :header })]
      (excel/add-column sheet "First Name" { :field :first })
      (excel/add-column sheet "Last Name" { :field :last })
      (excel/add-column sheet "Age" { :field :age })
      (excel/write-items sheet data)
      (excel/auto-size-columns sheet)
      (excel/write->file wbook "sample.xlsx"))))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-008.png" width="400">

[top](#content)



#### Cell styles

Define a named font with optional attributes on the workbook.

```
  ;; define a font ':header-font'
  (excel/add-font wbook :header-font { :height 12
                                       :bold true
                                       :italic false
                                       :color :BLUE })
  
  ;; define a style to be used with the header row
  (excel/add-style wbook :header { :font :header-font
                                   :bg-color :GREY_25_PERCENT
                                   :h-align :center
                                   :rotation 0
                                   :border-top :thin
                                   :border-bottom :thin })
  
  ;; define a style to be used with the 'weight' column
  (excel/add-style wbook :weight { :format, "#,##0.0"
                                   :h-align :right })
```

| Option           | Description                                    |
| ---------------- | ---------------------------------------------- |
| :format s        | <div>cell format, e.g. "#0"<br><br>Default formats by data type :<br> <ul><li>long: "#0"</li> <li>integer: "#0"</li> <li>float: "#,##0.00"</li> <li>double: "#,##0.00"</li> <li>date: "d.m.yyyy"</li> <li>datetime: "d.m.yyyy hh:mm:ss"</li></ul></div> |
| :font r          | font reference, e.g. :header-font              |
| :bg-color c      | background color, either an Excel indexed color<br>or a HTML color, e.g. :PLUM, "#00FF00"<br>Note: only XLSX supports 24 bit colors     |
| :wrap-text b     | wrap text, e.g. true, false                    |
| :h-align e       | horizontal alignment {:left, :center, :right}  |
| :v-align e       | vertical alignment {:top, :middle, :bottom}    |
| :rotation r      | rotation angle [degree], e.g. 45               |
| :border-top s    | border top style, e.g. :thin                   |
| :border-right s  | border right style, e.g. :none                 |
| :border-bottom s | border bottom style, e.g. :thin                |
| :border-left s   | border left style, e.g. :none                  |

Available border styles:

:none, :dotted, :medium-dashed, :medium-dash-dot-dot,
:thin, :thick, :dash-dot, :slanted-dash-dot,
:medium, :double, :medium-dash-dot,
:dashed, :hair, :dash-dot-dot

**Example 1:**

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:first "John" :last "Doe"   :weight 70.5 }
                {:first "Sue"  :last "Ford"  :weight 54.2 } ]
        wbook (excel/create :xlsx)]

    ;; define a font ':header-font'
    (excel/add-font wbook :header-font { :bold true })
    
    ;; define a sheet style ':header' referencing the font ':header-font'
    (excel/add-style wbook :header { :font :header-font
                                     :bg-color :GREY_25_PERCENT
                                     :h-align :center
                                     :rotation 0
                                     :border-top :thin
                                     :border-bottom :thin })
                                     
    ;; define a style to be used with the 'weight' column
    (excel/add-style wbook :weight { :format "#,##0.0"
                                     :h-align :right })

    (let [sheet (excel/add-sheet wbook "Sheet 1"
                                 { :default-header-style :header })]
      (excel/add-column sheet "First Name" { :field :first })
      (excel/add-column sheet "Last Name" { :field :last })
      (excel/add-column sheet "Weight" { :field :weight
                                         :body-style :weight })
      (excel/write-items sheet data)
      (excel/auto-size-columns sheet)
      (excel/write->file wbook "sample.xlsx"))))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-009.png" width="400">

[top](#content)


**Example 2:**

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Population")]
    
    (excel/add-font wbook :title       { :bold true 
                                         :color :WHITE })
    (excel/add-style wbook :title      { :font :title
                                         :bg-color "#282e9c"
                                         :h-align :center
                                         :v-align :middle })
    (excel/add-style wbook :subtitle   { :bg-color "#cfd1fc"
                                         :h-align :center })
    (excel/add-style wbook :country    { :bg-color "#f2f3fc" })
    (excel/add-style wbook :population { :format "#,###0"
                                         :bg-color "#f2f3fc" })

    (excel/row-height sheet 2 20)
    (excel/row-height sheet 3 7)
    (excel/col-width sheet 2 70)
    (excel/col-width sheet 3 70)
    (excel/add-merge-region sheet 2 2 2 3)
    
    ;; write values to cell with associated cell style
    (excel/write-value sheet 2 2 "Country Population" :title)
    (excel/write-value sheet 3 2 "" :country)
    (excel/write-value sheet 3 3 "" :population)
    (excel/write-value sheet 4 2 "Country" :subtitle)
    (excel/write-value sheet 4 3 "Population" :subtitle)
    (excel/write-value sheet 5 2 "Germany" :country)
    (excel/write-value sheet 5 3 83_783_942 :population)
    (excel/write-value sheet 6 2 "Italy" :country)
    (excel/write-value sheet 6 3 60_461_826 :population)
    (excel/write-value sheet 7 2 "Austria" :country)
    (excel/write-value sheet 7 3 9_006_398 :population)
    
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-011.png" width="400">

[top](#content)



### Background colors

```clojure
(do
  (load-module :excel)
  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Data")]

    ;; single cells
    (excel/bg-color sheet 1 1 "#27ae60")
    (excel/bg-color sheet 1 2 "#52be80")
    (excel/bg-color sheet 1 3 "#7dcea0")

    ;; range of cells in a row
    (excel/bg-color sheet 1 4 6 "#3498db") ;; row 1, col 4-6

    ;; area of cells with alternating row colors
    (excel/bg-color sheet 1 6 7 9 "#aed6f1")  ;; row 1-6,  col 7-9
    (excel/bg-color sheet 1 6 10 12 "#bb8fce" "#d2b4de")
    (excel/bg-color sheet 1 6 13 15 "#f1c40f" "#f4d03f" "#f7dc6f")
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-018.png" width="600">

[top](#content)



### Shading alternate rows

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Data")]
    (excel/write-data sheet [[100 101 102 nil 103 104 105]
                             [200 201 202 nil 203 204 205]
                             [300 301 302 nil 303 304 305]
                             [400 401 402 nil 403 404 405]
                             [500 501 502 nil 503 504 505]
                             [600 601 602 nil 603 604 605]])
                             
    (excel/bg-color sheet 1 6 1 3 "#a9cafc" "#d9e7fc")  ;; left box
    (excel/bg-color sheet 1 6 5 7 "#fcaedc" "#fce3f2")  ;; right box
    
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-013.png" width="400">

[top](#content)


### Styling cells

```clojure
(do
  (ns test)
  
  (load-module :excel)
  
  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-font wbook :bold { :bold true
                                  :color "#54039c" })
    (excel/add-style wbook :style-1 { :font :bold
                                      :h-align :left
                                      :rotation 0 })
    (excel/add-style wbook :style-2 { :bg-color "#cae1fa"
                                      :h-align :center
                                      :rotation 0
                                      :border-top :thin
                                      :border-left :thin
                                      :border-bottom :thin
                                      :border-right :thin})
    (excel/add-style wbook :style-3 { :h-align :right
                                      :format "#,##0.00" })

    ;; write cell values
    (excel/write-value sheet 2 1 100)
    (excel/write-value sheet 2 2 200)
    (excel/write-value sheet 2 3 300)

    ;; sytle the cells
    (excel/cell-style sheet 2 1 :style-1)
    (excel/cell-style sheet 2 2 :style-2)
    (excel/cell-style sheet 2 3 :style-3)

    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-014.png" width="400">

[top](#content)



### Styling cell region

```clojure
(do
  (ns test)
          
  (load-module :excel)
          
  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-style wbook :style { :bg-color "#cae1fa"
                                    :h-align :center
                                    :format "#,##0.00" })

    ;; write cell values
    (excel/write-value sheet 2 2 100)
    (excel/write-value sheet 2 3 200)
    (excel/write-value sheet 2 4 300)
    (excel/write-value sheet 3 2 101)
    (excel/write-value sheet 3 3 201)
    (excel/write-value sheet 3 4 301)

    ;; set the style for a cell region
    (excel/cell-style sheet 2 3 2 4 :style)

    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-015.png" width="400">

[top](#content)



### Freeze Panes

Freeze the top row:

```clojure
(do
  (ns test)
  
  (load-module :excel)
  
  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
      (excel/write-data sheet [(map #(str "Col " %) (range 1 11))])
      (excel/write-data sheet (partition 10 (range 100 500)) 2 1)
      
      (excel/freeze-pane sheet 1 0) ;; freeze the first row
      
      (excel/auto-size-columns sheet)
      (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-016.png" width="400">

[top](#content)


</br>


## Reading Excel files

The function `excel/open` opens an XLS or XLSX Excel file for reading. 

The file is specified by:

- a string file path: `"./sample.xlsx"`
- a file: `(io/file "./sample.xlsx")`
- a byte buffer
- a input stream: `(io/file-in-stream "./sample.xlsx")`

        
### Open Excel

```clojure
(do
  (ns test)

  (load-module :excel)
  
  (defn create-excel []
    (let [wbook  (excel/create :xlsx)
          sheet1 (excel/add-sheet wbook "Data1")
          sheet2 (excel/add-sheet wbook "Data2")]
      (excel/write-data sheet1 [[100 101]])
      (excel/write-data sheet2 [[300 301 302] [400 401 402]])
      (excel/write->bytebuf wbook)))
  

  (let [wbook (excel/open (create-excel))]
    (println "Sheet count: " (excel/sheet-count wbook))
     
    (println)
    (println "Sheet \"Data1\" (referenced by name):")
    (let [sheet (excel/sheet wbook "Data1")]
      (println "  Sheet name : ~(excel/sheet-name sheet)")
      (println "  Sheet index: ~(excel/sheet-index sheet)")
      (println "  Row range  : ~(excel/sheet-row-range sheet)")
      (println "  Col range  : ~(excel/sheet-col-range sheet 1) (row 1)"))
     
    (println)
    (println "Sheet \"Data2\" (referenced by index):")
    (let [sheet (excel/sheet wbook 2)]
      (println "  Sheet name : ~(excel/sheet-name sheet)")
      (println "  Sheet index: ~(excel/sheet-index sheet)")
      (println "  Row range  : ~(excel/sheet-row-range sheet)")
      (println "  Col range  : ~(excel/sheet-col-range sheet 1) (row 1)")
      (println "  Col range  : ~(excel/sheet-col-range sheet 2) (row 2)"))))
```

Prints to:

```
Sheet count:  2

Sheet "Data1" (referenced by name):
  Sheet name : Data1
  Sheet index: 1
  Row range  : [1 1]
  Col range  : [1 2] (row 1)

Sheet "Data2" (referenced by index):
  Sheet name : Data2
  Sheet index: 2
  Row range  : [1 2]
  Col range  : [1 3] (row 1)
  Col range  : [1 3] (row 2)
```

[top](#content)


### Reading Cell Metadata

Reading cell type, format, empty, locked and hidden status.

```clojure
(do
  (load-module :excel)

  (defn print-cell-meta [sheet row col]
    (println (str (excel/addr->string row col) ">  "
                  "type: " (name (excel/cell-type sheet row col))
                  ", format: " (excel/cell-data-format-string sheet row col)
                  ", empty: " (excel/cell-empty? sheet row col)
                  ", locked: " (excel/cell-locked? sheet row col)
                  ", hidden: " (excel/cell-hidden? sheet row col))))

  (let [wbook (excel/create :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/write-values sheet 1 1 "John" "Doe" 28)
    (excel/write-values sheet 2 1 "Mary" "Smith" 28)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx"))

  (let [wbook     (excel/open "sample.xlsx")
        sheet     (excel/sheet wbook "Sheet 1")
        row       1
        col-range (excel/sheet-col-range sheet 1)
        col-list  (range (first col-range) (inc (second col-range)))]
        (docoll #(print-cell-meta sheet (first %) (second %))
                (map vector (repeat row) col-list))))
```

Prints style info for row 1:

```
A1>  type: string, empty: false, locked: true, hidden: false
B1>  type: string, empty: false, locked: true, hidden: false
C1>  type: numeric, empty: false, locked: true, hidden: false
```


### Reading Cell Data Type

Each cell has one of the predefined cell data types:

  - `:notfound` (cell does not exist)
  - `:blank` (blank cell)
  - `:string` (string cell)
  - `:boolean` (boolean cell)
  - `:numeric` (numeric cell type: integer numbers, fractional numbers, dates)
  - `:formula`  (formula cell)
  - `:error`  (formula error)
  - `:unknown` (unknown cell type)


```clojure
(do
  (ns test)

  (load-module :excel)
  
  (defn create-excel []
    (let [wbook (excel/create :xlsx)
          sheet (excel/add-sheet wbook "Data")]
      (excel/write-data sheet [["foo" 
                                false 
                                100 
                                100.123
                                (time/local-date 2021 1 1)
                                (time/local-date-time 2021 1 1 15 30 45)
                                {:formula "SUM(C1,D1)"}
                                "" 
                                nil]])
      (excel/write->bytebuf wbook)))

  (let [wbook (excel/open (create-excel))
        sheet (excel/sheet wbook "Data")]
    (excel/evaluate-formulas wbook)
    (list-comp [r (range 1 2) c (range 1 10)]
      (println "Cell (~{r},~{c}): ~(excel/cell-type sheet r c)"))
    (println "Cell (1,7): ~(excel/cell-formula-result-type sheet 1 7) (formula result type)")
    
    (println)

    (list-comp [r (range 1 2) c (range 1 10)]
      (println "Cell (~{r},~{c}) empty: ~(excel/cell-empty? sheet r c)"))
    
    nil))
```

Prints to:

```
Cell (1,1): :string
Cell (1,2): :boolean
Cell (1,3): :numeric
Cell (1,4): :numeric
Cell (1,5): :numeric
Cell (1,6): :numeric
Cell (1,7): :formula
Cell (1,8): :string
Cell (1,9): :notfound
Cell (1,7): :numeric (formula result type)

Cell (1,1) empty: false
Cell (1,2) empty: false
Cell (1,3) empty: false
Cell (1,4) empty: false
Cell (1,5) empty: false
Cell (1,6) empty: false
Cell (1,7) empty: false
Cell (1,8) empty: false
Cell (1,9) empty: true
```

[top](#content)



### Reading Cells

If the Excel document contains formulas, call `excel/evaluate-formulas` before reading 
the cells to get the evaluated formula values, otherwise the cell returns the 
formula itself!


**Reading typed values:**

```clojure
(do
  (ns test)

  (load-module :excel)
  
  (defn create-excel []
    (let [wbook (excel/create :xlsx)
          sheet (excel/add-sheet wbook "Data")]
      (excel/write-data sheet [["foo" 
                                false 
                                100 
                                100.123
                                (time/local-date 2021 1 1)
                                (time/local-date-time 2021 1 1 15 30 45)
                                {:formula "SUM(C1,D1)"}
                                "" 
                                nil]])
      (excel/write->bytebuf wbook)))

  (let [wbook (excel/open (create-excel))
        sheet (excel/sheet wbook "Data")]
    (excel/evaluate-formulas wbook) ;; evaluate the formulas!
    (println "Cell (1,1): ~(pr-str (excel/read-string-val sheet 1 1))")
    (println "Cell (1,2): ~(pr-str (excel/read-boolean-val sheet 1 2))")
    (println "Cell (1,3): ~(pr-str (excel/read-long-val sheet 1 3))")
    (println "Cell (1,4): ~(pr-str (excel/read-double-val sheet 1 4))")
    (println "Cell (1,5): ~(pr-str (excel/read-date-val sheet 1 5))")
    (println "Cell (1,6): ~(pr-str (excel/read-datetime-val sheet 1 6))")
    (println "Cell (1,7): ~(pr-str (excel/read-double-val sheet 1 7))")
    (println "Cell (1,8): ~(pr-str (excel/read-string-val sheet 1 8))")
    (println "Cell (1,9): ~(pr-str (excel/read-string-val sheet 1 9))")))
```

Prints to:

```
Cell (1,1): "foo"
Cell (1,2): false
Cell (1,3): 100
Cell (1,4): 100.123
Cell (1,5): 2021-01-01
Cell (1,6): 2021-01-01T15:30:45
Cell (1,7): 200.123
Cell (1,8): ""
Cell (1,9): nil
```

**Reading generic values:**

The Excel module provides the function `excel/read-val` to read the generic raw value of a cell and returning a Venice nil, string boolean, double or timestamp value. Actually Excel just supports blank, string, boolean and number cells. Integer and date cells are just number cells of type double with a format. The function returns a timestamp if a number cell has a date format attached.


```clojure
(do
  (ns test)

  (load-module :excel)
  
  (defn create-excel []
    (let [wbook (excel/create :xlsx)
          sheet (excel/add-sheet wbook "Data")]
      (excel/write-data sheet [["foo" 
                                false 
                                100 
                                100.123
                                (time/local-date 2021 1 1)
                                (time/local-date-time 2021 1 1 15 30 45)
                                {:formula "SUM(C1,D1)"}
                                "" 
                                nil]])
      (excel/write->bytebuf wbook)))

  (let [wbook (excel/open (create-excel))
        sheet (excel/sheet wbook "Data")]
    (excel/evaluate-formulas wbook) ;; evaluate the formulas!
    
    (list-comp [c (range 1 10)]      
      (printf "Cell (1,%d): %s%n" c (excel/read-val sheet 1 c)))
    nil))
```

Prints to:

```
Cell (1,1): "foo"
Cell (1,2): false
Cell (1,3): 100.0
Cell (1,4): 100.123
Cell (1,5): 2021-01-01T00:00
Cell (1,6): 2021-01-01T15:30:45
Cell (1,7): 200.123
Cell (1,8): ""
Cell (1,9): nil
```


[top](#content)





