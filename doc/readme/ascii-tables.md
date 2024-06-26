# ASCII tables


The ASCII tables module provides a simple way to render tabular data in pure ascii.

* [Basic without styling](#basic-without-styling)
* [Header, footer, cell alignment](#header-footer-cell-alignment)
* [Column width and padding](#column-width-and-padding)
* [Borders](#borders)
* [Cell overflow](#cell-overflow)
* [Multi-column text](#multi-column-text)


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



## Header, footer, cell alignment


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
    ;; columns 
    [{:body  {:align :left, :overflow :newline}
      :width 7}
     {:body  {:align :center, :overflow :newline}
      :width 7}
     {:body  {:align :right, :overflow :newline}
      :width 7}] 
    ;; data
    [[1 "1"     "2"    ] 
     [2 "100"   "200"  ] 
     [3 "10000" "20000"]] 
    :double
    1))
     
     
     
``` 

</td>
<td>

```clojure
(do
  (load-module :ascii-table)
  (ascii-table/print 
    ;; columns 
    [{:header {:text "hd 1", :align :left }
      :body   {:align :left, :overflow :newline}
      :width  7}
     {:header {:text "hd 2", :align :center }
      :body   {:align :center, :overflow :newline}
      :width  7}
     {:header {:text "hd 3", :align :right }
      :body   {:align :right, :overflow :newline}
      :width  7}] 
     ;; data
     [[1 "1"     "2"    ] 
      [2 "100"   "200"  ] 
      [3 "10000" "20000"]] 
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
      :width  7}
     {:header {:text "hd 2", :align :center }
      :body   {:align :center, :overflow :newline}
      :footer {:text "ft 2", :align :center }
      :width  7}
     {:header {:text "hd 3", :align :right }
      :body   {:align :right, :overflow :newline}
      :footer {:text "ft 3", :align :right }
      :width  7}] 
     [[1 "1"     "2"    ] 
      [2 "100"   "200"  ] 
      [3 "10000" "20000"]] 
    :double
    1))
``` 

</td>
</tr>
<tr>
<td>

```
╔═════════╤═════════╤═════════╗
║ 1       │    1    │       2 ║
╟─────────┼─────────┼─────────╢
║ 2       │   100   │     200 ║
╟─────────┼─────────┼─────────╢
║ 3       │  10000  │   20000 ║
╚═════════╧═════════╧═════════╝




```

</td>
<td>

```
╔═════════╤═════════╤═════════╗
║ hd 1    │   hd 2  │    hd 3 ║
╠═════════╪═════════╪═════════╣
║ 1       │    1    │       2 ║
╟─────────┼─────────┼─────────╢
║ 2       │   100   │     200 ║
╟─────────┼─────────┼─────────╢
║ 3       │  10000  │   20000 ║
╚═════════╧═════════╧═════════╝



```

</td>
<td>

```
╔═════════╤═════════╤═════════╗
║ hd 1    │   hd 2  │    hd 3 ║
╠═════════╪═════════╪═════════╣
║ 1       │    1    │       2 ║
╟─────────┼─────────┼─────────╢
║ 2       │   100   │     200 ║
╟─────────┼─────────┼─────────╢
║ 3       │  10000  │   20000 ║
╠═════════╪═════════╪═════════╣
║ ft 1    │   ft 2  │    ft 3 ║
╚═════════╧═════════╧═════════╝
```

</td>
</tr>
</table>


## Column width and padding

<table>
<tr>
<td>Column width</td>
</tr>
<tr>
<td>

```clojure
(do
  (load-module :ascii-table)
  (ascii-table/print 
    [{:header {:text "hd 1", :align :left }
      :body   {:align :left, :overflow :newline}
      :footer {:text "ft 1", :align :left }
      :width  5}
     {:header {:text "hd 2", :align :center }
      :body   {:align :center, :overflow :newline}
      :footer {:text "ft 2", :align :center }
      :width  9}
     {:header {:text "hd 3", :align :right }
      :body   {:align :right, :overflow :newline}
      :footer {:text "ft 3", :align :right }
      :width  13}] 
     [[1 "1"     "2"    ] 
      [2 "100"   "200"  ] 
      [3 "10000" "20000"]] 
    :double
    1))
``` 

</td>
</tr>
<tr>
<td>

```
╔═══════╤═══════════╤═══════════════╗
║ hd 1  │    hd 2   │          hd 3 ║
╠═══════╪═══════════╪═══════════════╣
║ 1     │     1     │             2 ║
╟───────┼───────────┼───────────────╢
║ 2     │    100    │           200 ║
╟───────┼───────────┼───────────────╢
║ 3     │   10000   │         20000 ║
╠═══════╪═══════════╪═══════════════╣
║ ft 1  │    ft 2   │          ft 3 ║
╚═══════╧═══════════╧═══════════════╝

```

</td>
</tr>
</table>



<table>
<tr>
<td>Padding 1</td>
<td>Padding 2</td>
<td>Padding 4</td>
</tr>
<tr>
<td>

```clojure
(do
  (load-module :ascii-table)
  (ascii-table/print 
    [{:header {:text "hd 1", :align :left }
      :body   {:align :left, :overflow :newline}
      :footer {:text "ft 1", :align :left }
      :width  5}
     {:header {:text "hd 2", :align :center }
      :body   {:align :center, :overflow :newline}
      :footer {:text "ft 2", :align :center }
      :width  5}
     {:header {:text "hd 3", :align :right }
      :body   {:align :right, :overflow :newline}
      :footer {:text "ft 3", :align :right }
      :width  5}] 
     [[1 "1"     "2"    ] 
      [2 "100"   "200"  ] 
      [3 "10000" "20000"]] 
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
      :width  5}
     {:header {:text "hd 2", :align :center }
      :body   {:align :center, :overflow :newline}
      :footer {:text "ft 2", :align :center }
      :width  5}
     {:header {:text "hd 3", :align :right }
      :body   {:align :right, :overflow :newline}
      :footer {:text "ft 3", :align :right }
      :width  5}] 
     [[1 "1"     "2"    ] 
      [2 "100"   "200"  ] 
      [3 "10000" "20000"]] 
    :double
    2))
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
      :width  5}
     {:header {:text "hd 2", :align :center }
      :body   {:align :center, :overflow :newline}
      :footer {:text "ft 2", :align :center }
      :width  5}
     {:header {:text "hd 3", :align :right }
      :body   {:align :right, :overflow :newline}
      :footer {:text "ft 3", :align :right }
      :width  5}] 
     [[1 "1"     "2"    ] 
      [2 "100"   "200"  ] 
      [3 "10000" "20000"]] 
    :double
    4))
``` 

</td>
</tr>
<tr>
<td>

```
╔═══════╤═══════╤═══════╗
║ hd 1  │  hd 2 │  hd 3 ║
╠═══════╪═══════╪═══════╣
║ 1     │   1   │     2 ║
╟───────┼───────┼───────╢
║ 2     │  100  │   200 ║
╟───────┼───────┼───────╢
║ 3     │ 10000 │ 20000 ║
╠═══════╪═══════╪═══════╣
║ ft 1  │  ft 2 │  ft 3 ║
╚═══════╧═══════╧═══════╝
```

</td>
<td>

```
╔═════════╤═════════╤═════════╗
║  hd 1   │   hd 2  │   hd 3  ║
╠═════════╪═════════╪═════════╣
║  1      │    1    │      2  ║
╟─────────┼─────────┼─────────╢
║  2      │   100   │    200  ║
╟─────────┼─────────┼─────────╢
║  3      │  10000  │  20000  ║
╠═════════╪═════════╪═════════╣
║  ft 1   │   ft 2  │   ft 3  ║
╚═════════╧═════════╧═════════╝
```

</td>
<td>

```
╔═════════════╤═════════════╤═════════════╗
║    hd 1     │     hd 2    │     hd 3    ║
╠═════════════╪═════════════╪═════════════╣
║    1        │      1      │        2    ║
╟─────────────┼─────────────┼─────────────╢
║    2        │     100     │      200    ║
╟─────────────┼─────────────┼─────────────╢
║    3        │    10000    │    20000    ║
╠═════════════╪═════════════╪═════════════╣
║    ft 1     │     ft 2    │     ft 3    ║
╚═════════════╧═════════════╧═════════════╝
```

</td>
</tr>
</table>


## Borders

```clojure
(do
  (load-module :ascii-table)
  
  (defn print-with-border [border]
    (ascii-table/print 
       [{:header {:text "hd 1", :align :left }
         :body   {:align :left, :overflow :newline}
         :width  5}
        {:header {:text "hd 2", :align :center }
         :body   {:align :center, :overflow :newline}
         :width  5}
        {:header {:text "hd 3", :align :right }
         :body   {:align :right, :overflow :newline}
         :width  5}] 
       [[1 "1"   "2"  ] 
        [2 "10"  "20" ] 
        [3 "100" "200"]] 
       border
       1)
     (println)
     (println))
       
   (docoll print-with-border [:none                       
                              :standard                   
                              :standard-no-data           
                              :standard-no-outside        
                              :standard-no-data-no-outside
                              :standard-minimal           
                              :double                     
                              :double-no-data             
                              :bold                       
                              :bold-no-data               
                              :thin                       
                              :thin-no-data               
                              :matrix                     
                              :minimal]))
```

<table>

<tr>
<td>:none</td>
<td>

```
 hd 1    hd 2   hd 3 
 1        1        2 
 2        10      20 
 3       100     200 
```

</td>
</tr>

<tr>
<td>:standard</td>
<td>

```
+-------+-------+-------+
| hd 1  |  hd 2 |  hd 3 |
+-------+-------+-------+
| 1     |   1   |     2 |
+-------+-------+-------+
| 2     |   10  |    20 |
+-------+-------+-------+
| 3     |  100  |   200 |
+-------+-------+-------+
```

</td>
</tr>

<tr>
<td>:standard-no-data</td>
<td>

```
+-------+-------+-------+
| hd 1  |  hd 2 |  hd 3 |
+-------+-------+-------+
| 1     |   1   |     2 |
| 2     |   10  |    20 |
| 3     |  100  |   200 |
+-------+-------+-------+
```

</td>
</tr>

<tr>
<td>:standard-no-outside</td>
<td>

```
 hd 1  |  hd 2 |  hd 3 
-------+-------+-------
 1     |   1   |     2 
-------+-------+-------
 2     |   10  |    20 
-------+-------+-------
 3     |  100  |   200 
```

</td>
</tr>
<tr>
<td>:standard-no-data-no-outside</td>
<td>

```
 hd 1  |  hd 2 |  hd 3 
-------+-------+-------
 1     |   1   |     2 
 2     |   10  |    20 
 3     |  100  |   200 
```

</td>
</tr>


</td>
</tr>


<tr>
<td>:standard-minimal</td>
<td>

```
---------------------
 hd 1    hd 2   hd 3 
---------------------
 1        1        2 
 2        10      20 
 3       100     200 
---------------------
```

</td>
</tr>

<tr>
<td>:double</td>
<td>

```
╔═══════╤═══════╤═══════╗
║ hd 1  │  hd 2 │  hd 3 ║
╠═══════╪═══════╪═══════╣
║ 1     │   1   │     2 ║
╟───────┼───────┼───────╢
║ 2     │   10  │    20 ║
╟───────┼───────┼───────╢
║ 3     │  100  │   200 ║
╚═══════╧═══════╧═══════╝
```

</td>
</tr>

<tr>
<td>:double-no-data</td>
<td>

```
╔═══════╤═══════╤═══════╗
║ hd 1  │  hd 2 │  hd 3 ║
╠═══════╪═══════╪═══════╣
║ 1     │   1   │     2 ║
║ 2     │   10  │    20 ║
║ 3     │  100  │   200 ║
╚═══════╧═══════╧═══════╝
```

</td>
</tr>

<tr>
<td>:bold</td>
<td>

```
┏━━━━━━━┯━━━━━━━┯━━━━━━━┓
┃ hd 1  │  hd 2 │  hd 3 ┃
┣━━━━━━━┿━━━━━━━┿━━━━━━━┫
┃ 1     │   1   │     2 ┃
┠───────┼───────┼───────┨
┃ 2     │   10  │    20 ┃
┠───────┼───────┼───────┨
┃ 3     │  100  │   200 ┃
┗━━━━━━━┷━━━━━━━┷━━━━━━━┛
```

</td>
</tr>

<tr>
<td>:bold-no-data</td>
<td>

```
┏━━━━━━━┯━━━━━━━┯━━━━━━━┓
┃ hd 1  │  hd 2 │  hd 3 ┃
┣━━━━━━━┿━━━━━━━┿━━━━━━━┫
┃ 1     │   1   │     2 ┃
┃ 2     │   10  │    20 ┃
┃ 3     │  100  │   200 ┃
┗━━━━━━━┷━━━━━━━┷━━━━━━━┛
```

</td>
</tr>

<tr>
<td>:thin</td>
<td>

```
┌───────┬───────┬───────┐
│ hd 1  │  hd 2 │  hd 3 │
├───────┼───────┼───────┤
│ 1     │   1   │     2 │
├───────┼───────┼───────┤
│ 2     │   10  │    20 │
├───────┼───────┼───────┤
│ 3     │  100  │   200 │
└───────┴───────┴───────┘
```

</td>
</tr>

<tr>
<td>:thin-no-data</td>
<td>

```
┌───────┬───────┬───────┐
│ hd 1  │  hd 2 │  hd 3 │
├───────┼───────┼───────┤
│ 1     │   1   │     2 │
│ 2     │   10  │    20 │
│ 3     │  100  │   200 │
└───────┴───────┴───────┘
```

</td>
</tr>

<tr>
<td>:thin-round</td>
<td>

```
╭───────┬───────┬───────╮
│ hd 1  │  hd 2 │  hd 3 │
├───────┼───────┼───────┤
│ 1     │   1   │     2 │
├───────┼───────┼───────┤
│ 2     │   10  │    20 │
├───────┼───────┼───────┤
│ 3     │  100  │   200 │
╰───────┴───────┴───────╯
```

</td>
</tr>

<tr>
<td>:thin-round-no-data</td>
<td>

```
╭───────┬───────┬───────╮
│ hd 1  │  hd 2 │  hd 3 │
├───────┼───────┼───────┤
│ 1     │   1   │     2 │
│ 2     │   10  │    20 │
│ 3     │  100  │   200 │
╰───────┴───────┴───────╯
```

</td>
</tr>

<tr>
<td>:matrix</td>
<td>

```
│     1       1       2 │
│     2      10      20 │
│     3     100     200 │
```

</td>
</tr>

<tr>
<td>:minimal</td>
<td>

```
─────────────────────
 hd 1    hd 2   hd 3 
─────────────────────
 1        1        2 
 2        10      20 
 3       100     200 
─────────────────────
```

</td>
</tr>

</table>


## Cell overflow


```clojure
(do
  (load-module :ascii-table)
  (ascii-table/print 
     [{:header {:text ":newline"}
       :body   {:align :left, :overflow :newline}
       :width  20}
      {:header {:text ":clip-right"}
       :body   {:align :center, :overflow :clip-right}
       :width  20}
      {:header {:text ":clip-left"}
       :body   {:align :center, :overflow :clip-left}
       :width  20}
      {:header {:text ":ellipsis-right"}
       :body   {:align :center, :overflow :ellipsis-right}
       :width  20}
      {:header {:text ":ellipsis-left"}
       :body   {:align :center, :overflow :ellipsis-left}
       :width  20}] 
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


## Multi-column text


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

