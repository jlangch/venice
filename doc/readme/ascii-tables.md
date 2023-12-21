# ASCII tables


The ASCII tables module provides a simple way to render tabular data in pure ascii.


## Basic without styling

<table>
<tr>
<td>

```clojure
(ascii-table/render 
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


