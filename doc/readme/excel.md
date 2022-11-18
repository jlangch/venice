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



## Content

1. [Writing Excel files](#writing-excel-files)
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
       * [Row height](#row-height)
       * [Fonts](#fonts)
       * [Cell Styles](#cell-styles)
2. [Reading Excel files](#reading-excel-files)
    * [Open Excel](#open-excel)
    * [Reading Cell Metadata](#reading-cell-metadata)
    * [Reading Cells](#reading-cells)


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

[top](#content)


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

[top](#content)



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

[top](#content)



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

[top](#content)



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

[top](#content)

 


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
    (excel/auto-size-columns (excel/sheet wbook "Data"))
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-005.png" width="400">

[top](#content)



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
    (excel/auto-size-columns (excel/sheet wbook "Sheet 1"))
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-003.png" width="400">

[top](#content)



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

Venice provides the function `excel/cell-address` to help with building logical cell addresses
for formulas:

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:a 100 :b 200 }
                {:a 101 :b 201 }
                {:a 102 :b 202 } ]
        wbook (excel/writer :xlsx)
        sheet (excel/add-sheet wbook "Sheet 1" { :no-header-row true })
        addr  #(excel/cell-address sheet %1 %2)  ;; build logical cell addresses
        sum   #(str "SUM(" %1 "," %2 ")")]       ;; build SUM formula
    (excel/add-column sheet "A" { :field :a })
    (excel/add-column sheet "B" { :field :b })
    (excel/add-column sheet "C" { :field :c })
    (excel/write-items sheet data)
    (excel/cell-formula sheet 1 3 (sum (addr 1 1) (addr 1 2)))
    (excel/cell-formula sheet 2 3 (sum (addr 2 1) (addr 2 2)))
    (excel/cell-formula sheet 3 3 (sum (addr 3 1) (addr 3 2)))
    (excel/evaluate-formulas wbook)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

[top](#content)



### Styling

#### Row height

Set the height of individual rows:

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
    (excel/row-height sheet 2 100)
    (excel/auto-size-columns sheet)
    (excel/write->file wbook "sample.xlsx")))
```

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/excel/excel-write-007.png" width="400">

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
        wbook (excel/writer :xlsx)]
        
    ;; define a font ':header-font'
    (excel/add-font wbook :header-font { :height 12
                                         :bold true
                                         :italic false
                                         :color :BLUE })
   
    ;; define a sheet style ':header' referencing the font ':header-font'
    (excel/add-style wbook :header { :font :header-font })

    (let [sheet (excel/add-sheet wbook "Sheet 1"
                                 { :no-header-row false
                                   :default-header-style :header })]
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

```clojure
(do
  (ns test)

  (load-module :excel)

  (let [data  [ {:first "John" :last "Doe"   :weight 70.5 }
                {:first "Sue"  :last "Ford"  :weight 54.2 } ]
        wbook (excel/writer :xlsx)]

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
                                 { :no-header-row false
                                   :default-header-style :header })]
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



## Reading Excel files

### Open Excel

```clojure
(do
  (ns test)

  (load-module :excel)
  
  (defn create-excel []
    (let [wbook (excel/writer :xlsx)]
      (excel/write-data wbook "Data1" [[100 101]])
      (excel/write-data wbook "Data2" [[300 301 302] [400 401 402]])
      (excel/write->bytebuf wbook)))
  

  (let [wbook (excel/open (create-excel))]
    (println "Sheet count: " (excel/sheet-count wbook))
     
    (println)
    (println "Sheet \"Data1\" (referenced by name):")
    (let [sheet (excel/sheet wbook "Data1")]
      (println "Sheet name : ~(excel/sheet-name sheet)")
      (println "Sheet index: ~(excel/sheet-index sheet)")
      (println "Row range  : ~(excel/sheet-row-range sheet)")
      (println "Col range  : ~(excel/sheet-col-range sheet 1) (row 1)"))
     
    (println)
    (println "Sheet \"Data2\" (referenced by index):")
    (let [sheet (excel/sheet wbook 2)]
      (println "Sheet name : ~(excel/sheet-name sheet)")
      (println "Sheet index: ~(excel/sheet-index sheet)")
      (println "Row range  : ~(excel/sheet-row-range sheet)")
      (println "Col range  : ~(excel/sheet-col-range sheet 1) (row 1)")
      (println "Col range  : ~(excel/sheet-col-range sheet 2) (row 2)"))))
```

Prints to:

```
Sheet count:  2

Sheet "Data1" (referenced by name):
Sheet name :  Data1
Sheet index:  1I
Row range  :  [1 1]
Col range  :  [1 2] (row 1)

Sheet "Data2" (referenced by index):
Sheet name :  Data2
Sheet index:  2I
Row range  :  [1 2]
Col range  :  [1 3] (row 1)
Col range  :  [1 3] (row 2)
```

[top](#content)



### Reading Cell Metadata

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
    (let [wbook (excel/writer :xlsx)]
      (excel/write-data wbook "Data" [["foo" 
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
    (list-comp [r (range 1 2) c (range 1 10)]
      (println "Cell (~{r},~{c}): ~(excel/cell-type sheet r c)"))

    (println)

    (list-comp [r (range 1 2) c (range 1 10)]
      (println "Cell (~{r},~{c}) empty: ~(excel/cell-empty? sheet r c)"))
    nil))
```

Prints to:

```
Cell (1,1):  :string
Cell (1,2):  :boolean
Cell (1,3):  :numeric
Cell (1,4):  :numeric
Cell (1,5):  :numeric
Cell (1,6):  :numeric
Cell (1,7):  :formula
Cell (1,8):  :string
Cell (1,9):  :unknown

Cell (1,1) empty:  false
Cell (1,2) empty:  false
Cell (1,3) empty:  false
Cell (1,4) empty:  false
Cell (1,5) empty:  false
Cell (1,6) empty:  false
Cell (1,7) empty:  false
Cell (1,8) empty:  false
Cell (1,9) empty:  true
Cell (1,10) empty:  true
```

[top](#content)



### Reading Cells

If the Excel document contains formulas call `excel/evaluate-formulas` before reading the cells to get the evaluated formula values, otherwise the cell returns the formula itself!

```clojure
(do
  (ns test)

  (load-module :excel)
  
  (defn create-excel []
    (let [wbook (excel/writer :xlsx)]
      (excel/write-data wbook "Data" [["foo" 
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

[top](#content)

