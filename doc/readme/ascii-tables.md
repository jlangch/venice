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
     1))
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
    1))
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
    1))
``` 

</td>
</tr>
<tr>
<td>

```
+---+-----+-----+
| 1 | 1   | 2   |
+---+-----+-----+
| 2 | 10  | 20  |
+---+-----+-----+
| 3 | 100 | 200 |
+---+-----+-----+




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



## Column and border styles


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
     [{:body  {:align :left, :overflow :newline}
       :width 5}
      {:body  {:align :center, :overflow :newline}
       :width 5}
      {:body  {:align :right, :overflow :newline}
       :width 5}] 
     [[1 "1"   "2"  ] 
      [2 "10"  "20" ] 
      [3 "100" "200"]] 
     :double
     1))
``` 

</td>
<td>

```clojure
(do
  (load-module :ascii-table)
  (ascii-table/print 
     [{:header {:text "hd 1", :align :left }
       :body   {:align :left, :overflow :newline}
       :width  8}
      {:header {:text "hd 2", :align :center }
       :body   {:align :center, :overflow :newline}
       :width  8}
      {:header {:text "hd 3", :align :right }
       :body   {:align :right, :overflow :newline}
       :width  8}] 
     [[1 "1"   "2"  ] 
      [2 "10"  "20" ] 
      [3 "100" "200"]] 
     :double
     1))
``` 

</td>
<td>

```clojure
(do
  (load-module :ascii-table)
  (ascii-table/print 
     [{:header {:text "hd 1", :align :left }
       :body   {:align :left, :overflow :newline}
       :footer {:text "ft 1", :align :left }
       :width  8}
      {:header {:text "hd 2", :align :center }
       :body   {:align :center, :overflow :newline}
       :footer {:text "ft 2", :align :center }
       :width  8}
      {:header {:text "hd 3", :align :right }
       :body   {:align :right, :overflow :newline}
       :footer {:text "ft 3", :align :right }
       :width  8}] 
     [[1 "1"   "2"  ] 
      [2 "10"  "20" ] 
      [3 "100" "200"]] 
     :double
     1))
``` 

</td>
</tr>
<tr>
<td>

```
╔═══════╤═══════╤═══════╗
║ 1     │   1   │     2 ║
╟───────┼───────┼───────╢
║ 2     │   10  │    20 ║
╟───────┼───────┼───────╢
║ 3     │  100  │   200 ║
╚═══════╧═══════╧═══════╝




```

</td>
<td>

```
╔══════════╤══════════╤══════════╗
║ hd 1     │   hd 2   │     hd 3 ║
╠══════════╪══════════╪══════════╣
║ 1        │     1    │        2 ║
╟──────────┼──────────┼──────────╢
║ 2        │    10    │       20 ║
╟──────────┼──────────┼──────────╢
║ 3        │    100   │      200 ║
╚══════════╧══════════╧══════════╝



```

</td>
<td>

```
╔══════════╤══════════╤══════════╗
║ hd 1     │   hd 2   │     hd 3 ║
╠══════════╪══════════╪══════════╣
║ 1        │     1    │        2 ║
╟──────────┼──────────┼──────────╢
║ 2        │    10    │       20 ║
╟──────────┼──────────┼──────────╢
║ 3        │    100   │      200 ║
╠══════════╪══════════╪══════════╣
║ ft 1     │   ft 2   │     ft 3 ║
╚══════════╧══════════╧══════════╝
```

</td>
</tr>
</table>


## Borders


## Cell overflow


```clojure
(do
  (load-module :ascii-table)
  (ascii-table/print 
     [{:header {:text ":newline"}
       :body  {:align :left, :overflow :newline}
       :width 20}
      {:header {:text ":clip-right"}
       :body  {:align :center, :overflow :clip-right}
       :width 20}
      {:header {:text ":clip-left"}
       :body  {:align :center, :overflow :clip-left}
       :width 20}
      {:header {:text ":ellipsis-right"}
       :body  {:align :center, :overflow :ellipsis-right}
       :width 20}
      {:header {:text ":ellipsis-left"}
       :body  {:align :center, :overflow :ellipsis-left}
       :width 20}] 
     [[(str/lorem-ipsum :chars 60)
       (str/lorem-ipsum :chars 60)
       (str/lorem-ipsum :chars 60) 
       (str/lorem-ipsum :chars 60)
       (str/lorem-ipsum :chars 60)]] 
     :double
     1))
``` 

```
╔══════════════════════╤══════════════════════╤══════════════════════╤══════════════════════╤══════════════════════╗
║ :newline             │ :clip-right          │ :clip-left           │ :ellipsis-right      │ :ellipsis-left       ║
╠══════════════════════╪══════════════════════╪══════════════════════╪══════════════════════╪══════════════════════╣
║ Lorem ipsum dolor    │ Lorem ipsum dolor si │ adipiscing elit. Pra │ Lorem ipsum dolor s… │ …dipiscing elit. Pra ║
║ sit amet, consectetu │                      │                      │                      │                      ║
║ r adipiscing elit.   │                      │                      │                      │                      ║
║ Pra                  │                      │                      │                      │                      ║
╚══════════════════════╧══════════════════════╧══════════════════════╧══════════════════════╧══════════════════════╝
```


## Multi-column ascii text layout


<table>
<tr>
<td>Border</td>
<td>Without Border</td>
</tr>
<tr>
<td>

```clojure
(do
  (load-module :ascii-table)
  (ascii-table/print 
    [{:body  {:align :left, :overflow :newline}
      :width 25}
     {:body  {:align :left, :overflow :newline}
      :width 25}] 
     [[(str/lorem-ipsum :chars 150) 
       (str/lorem-ipsum :chars 120)]] 
     :thin
     1))
``` 

</td>
<td>

```clojure
(do
  (load-module :ascii-table)
  (ascii-table/print 
    [{:body  {:align :left, :overflow :newline}
      :width 25}
     {:body  {:align :left, :overflow :newline}
      :width 25}] 
     [[(str/lorem-ipsum :chars 150) 
       (str/lorem-ipsum :chars 120)]] 
     :none
     1))
``` 

</td>
</tr>

<tr>
<td>

```
┌───────────────────────────┬───────────────────────────┐
│ Lorem ipsum dolor sit     │ Lorem ipsum dolor sit     │
│ amet, consectetur         │ amet, consectetur         │
│ adipiscing elit. Praesent │ adipiscing elit. Praesent │
│ ac iaculis turpis. Duis   │ ac iaculis turpis. Duis   │
│ dictum id sem et          │ dictum id sem et          │
│ consectetur. Nullam       │ consectetur.              │
│ lobortis, libero non co   │                           │
└───────────────────────────┴───────────────────────────┘
```

</td>
<td>

```
 Lorem ipsum dolor sit      Lorem ipsum dolor sit     
 amet, consectetur          amet, consectetur         
 adipiscing elit. Praesent  adipiscing elit. Praesent 
 ac iaculis turpis. Duis    ac iaculis turpis. Duis   
 dictum id sem et           dictum id sem et          
 consectetur. Nullam        consectetur.              
 lobortis, libero non co                              
```

</td>
</tr>
</table>

