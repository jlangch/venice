# ASCII tables


The ASCII tables module provides a simple way to render tabular data in pure ascii.


## Basic without styling

<table>
<tr>
<td>Without Header & Footer</td>
<td>Header</td>
<td>Header & Footer</td>
</tr>
<tr>
<td>

```clojure
(do
  (load-module :ascii-table)
  (ascii-table/print 
     nil 
     [[1 "1"   "2"  ] 
      [2 "10"  "20" ] 
      [3 "100" "200"]] 
     nil 
     :standard
     1)))))
``` 

</td>
<td>

```clojure
(do
  (load-module :ascii-table)
  (ascii-table/print 
    ["" "header 1" "header 2"] 
    [[1 "1"   "2"  ] 
     [2 "10"  "20" ] 
     [3 "100" "200"]] 
    nil
    :standard
    1)))
``` 

</td>
<td>

```clojure
(do
  (load-module :ascii-table)
  (ascii-table/print 
    ["" "header 1" "header 2"] 
    [[1 "1"   "2"  ] 
     [2 "10"  "20" ] 
     [3 "100" "200"]] 
    ["" "footer 1" "footer 3"] 
    :standard
    1)))
``` 

</td>
</tr>
<tr>
<td>

```
+---+----------+----------+
| 1 | 1        | 2        |
+---+----------+----------+
| 2 | 10       | 20       |
+---+----------+----------+
| 3 | 100      | 200      |
+---+----------+----------+




```

</td>
<td>

```
+---+----------+----------+
|   | header 1 | header 2 |
+---+----------+----------+
| 1 | 1        | 2        |
+---+----------+----------+
| 2 | 10       | 20       |
+---+----------+----------+
| 3 | 100      | 200      |
+---+----------+----------+


```

</td>
<td>

```
+---+----------+----------+
|   | header 1 | header 2 |
+---+----------+----------+
| 1 | 1        | 2        |
+---+----------+----------+
| 2 | 10       | 20       |
+---+----------+----------+
| 3 | 100      | 200      |
+---+----------+----------+
|   | footer 1 | footer 3 |
+---+----------+----------+
```

</td>
</tr>
</table>
    
























The sample data used in the examples is provided by [NASA Planet Compare](https://solarsystem.nasa.gov/planet-compare/).


