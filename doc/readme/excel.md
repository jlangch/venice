# Excel

Venice supports generating Excel files if the [Apache POI](https://poi.apache.org/) 
libs are on the runtime classpath.

Venice is compatible with Apache POI 4.1.x and 5.2.x.


Run this script from the REPL to download the newest Apache POI 5.2.x libraries:

```clojure
(do
  (load-module :excel-install)
  
  ;; Download the EXCEL libs from Maven
  (excel-install/install :dir (repl/libs-dir) :silent false)

  ;; Restart the REPL to make the new libs available to the REPL Java VM
  (println "Restarting...")
  (repl/restart))
```


* [Writing Excel files](#writing-excel-files)
  * [Introduction Example](#introduction-example)
  * [Examples](#write-examples)
     * [Write to an output stream](#write-to-an-output-stream)
     * [Write to a byte buffer](#write-to-a-byte-buffer)
     * [Omit the header row](#omit-the-header-row)
     * [Write to multiple sheets](#write-to-multiple-sheets)
     * [Supported datatypes](#supported-datatypes)
     * [Writing 2D vector data](#writing-2d-vector-data)
     * [Writing to individual cells](#writing-to-individual-cells)
     * [Using formulas](#using-formulas)
  * [Styling](#styling)
     * [Fonts](#fonts)
     * [Cell Styles](#cell-styles)
* [Reading Excel files](#reading-excel-files)



## Writing Excel files


### Introduction Example

The easiest way to write data to an EXCEL is passing pre-processed ready made data in
a table data set and map the sheet columns to the map keys in the table.

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:first "John" :last "Doe"   :age 28 }
                {:first "Sue"  :last "Ford"  :age 26 } ]
        wbook (excel/writer :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-column sheet "First Name" { :field :first })
    (excel/add-column sheet "Last Name" { :field :last })
    (excel/add-column sheet "Age" { :field :age })
    (excel/write-items sheet data)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-001.png" width="400">

To create an XLSX use `(excel/writer :xlsx)` to create a workbook. To create an 
old style XLS use `(excel/writer :xls)` and ensure it is written to a file with 
the `.xls` file name extension.



### Write Examples

#### Write to an output stream

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [os    (io/file-out-stream "sample.xlsx")
        data  [ {:first "John" :last "Doe"   :age 28 }
                {:first "Sue"  :last "Ford"  :age 26 } ]
        wbook (excel/writer :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-column sheet "First Name" { :field :first })
    (excel/add-column sheet "Last Name" { :field :last })
    (excel/add-column sheet "Age" { :field :age })
    (excel/write-items sheet data)
    (excel/auto-size-columns sheet)
    (excel/write->stream wbook os)))
```



#### Write to a byte buffer

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:first "John" :last "Doe"   :age 28 }
                {:first "Sue"  :last "Ford"  :age 26 } ]
        wbook (excel/writer :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-column sheet "First Name" { :field :first })
    (excel/add-column sheet "Last Name" { :field :last })
    (excel/add-column sheet "Age" { :field :age })
    (excel/write-items sheet data)
    (excel/auto-size-columns sheet)
    (excel/write->bytebuf wbook)))
```



#### Omit the header row

To omit the header row pass the option `:no-header-row true` to the excel sheet:

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:first "John" :last "Doe"   :age 28 }
                {:first "Sue"  :last "Ford"  :age 26 } ]
        wbook (excel/writer :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1" { :no-header-row true })]
    (excel/add-column sheet "First Name" { :field :first })
    (excel/add-column sheet "Last Name" { :field :last })
    (excel/add-column sheet "Age" { :field :age })
    (excel/write-items sheet data)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-002.png" width="400">



#### Write to multiple sheets

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data1  [ {:first "John" :last "Doe"   :age 28 }
                 {:first "Sue"  :last "Ford"  :age 26 } ]
        data2  [ {:first "Mark" :last "Smith" :age 40 }
                 {:first "Mary" :last "Jones" :age 42 } ]
        wbook  (excel/writer :xlsx)
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

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-003.png" width="400">



#### Supported datatypes

The Excel writer supports the Venice data types:

 - string
 - integer
 - long
 - double
 - boolean
 - :java.time.LocalDate
 - :java.time.LocalDateTime
 
```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:t-str "text" :t-int 100I :t-long 200 :t-double 1.23  :t-bool true 
                 :t-local-date (time/local-date 2021 1 1)
                 :t-local-ts (time/local-date-time 2021 1 1 15 30 45) } ]
        wbook (excel/writer :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-column sheet "string" { :field :t-str })
    (excel/add-column sheet "integer" { :field :t-int })
    (excel/add-column sheet "long" { :field :t-long })
    (excel/add-column sheet "double" { :field :t-double })
    (excel/add-column sheet "boolean" { :field :t-bool })
    (excel/add-column sheet ":LocalDate" { :field :t-local-date })
    (excel/add-column sheet ":LocalDateTime" { :field :t-local-ts })
    (excel/write-items sheet data)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-006.png" width="400">

 


#### Writing 2D vector data

Write the data of a 2D vector to an excel sheet.

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [wbook (excel/writer :xlsx)
        dt    (time/local-date 2021 1 1)
        ts    (time/local-date-time 2021 1 1 15 30 45)
        data  [[100  101  102  103  104  105]
               [200  "ab" 1.23 dt   ts   false]]]
    (excel/write-data wbook "Data" data)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-005.png" width="400">



#### Writing to individual cells

The functions `excel/write-value` To write values to cells. The row and col numbers are 1-based!

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [wbook (excel/writer :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1")]
    (excel/add-column sheet "First Name" { :field :first })
    (excel/add-column sheet "Last Name" { :field :last })
    (excel/add-column sheet "Age" { :field :age })
    (excel/write-value sheet 1 1 "John")
    (excel/write-value sheet 1 2 "Doe")
    (excel/write-value sheet 1 3 28)
    (excel/write-value sheet 2 1 "Sue")
    (excel/write-value sheet 2 2 "Ford")
    (excel/write-value sheet 2 3 26)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-003.png" width="400">



#### Using formulas

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:a 100 :b 200 }
                {:a 101 :b 201 }
                {:a 102 :b 202 } ]
        wbook (excel/writer :xlsx)
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

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-004.png" width="400">



### Styling

#### Fonts

#### Cell styles



## Reading Excel files


