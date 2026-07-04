# ASCII Charts


The ASCII charts module provides a simple way to render charts in pure ascii.


## Example Percentage Bar Chart 1

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/ascii-charts/ascii-chart-percentage-1.png" width="500">

``` clojure
(do
  (load-module :ascii-charts)
  (println)
  (println (ascii-charts/percentage-bar-chart 
              "CPU Usage Limit Per Hour"
              [ "00" "01" "02" "03" "04" "05" "06" "07" "08"
                "09" "10" "11" "12" "13" "14" "15" "16" "17"
                "18" "19" "20" "21" "22" "23" ]
              [ 100  100  100    0    0  100  100   50   30  
                  0    0    0    0    0    0    0    0    0
                  0   30   30   30   50  100 ])))
```




## Example Percentage Bar Chart 2

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/ascii-charts/ascii-chart-percentage-2.png" width="500">

``` clojure
(do
  (load-module :ascii-charts)
  (load-module :ansi)
  (println)
  (println (ascii-charts/percentage-bar-chart 
              "Demo Full Percentage Range"
              [ "00" "01" "02" "03" "04" "05" "06" "07" "08"
                "09" "10" "11" "12" "13" "14" "15" "16" "17"
                "18" "19" "20" "21" "22" "23" ]
              [   0     1   10   15   20   25   30   35   40  
                  45   50   55   60   65   70   75   80   85
                  90   95  100  100   95   90 ]
              :resolution  :high
              :size        :medium
              :title-color (str (ansi/ansi :bold) (ansi/ansi :black))
              :axis-color  (ansi/ansi (ansi/fg-color 242))
              :bar-color   (ansi/ansi (ansi/fg-color 35)))))
```


## Example Percentage Bar Chart 3

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/ascii-charts/ascii-chart-percentage-3.png" width="500">

``` clojure
(do
  (load-module :ascii-charts)
  (load-module :ansi)
  (println)
  (println (ascii-charts/percentage-bar-chart 
              "Demo Custom Bar Width"
              [ "Jan" "Feb" "Mar" "Apr" "Mai" "Jun" 
                "Jul" "Aug" "Sep" "Oct" "Nov" "Dec" ]
              [   80    60     0     2    18    25   
                  47    80    55    60    65   100  ]
              :resolution  :high
              :size        :medium
              :title-color (str (ansi/ansi :bold) (ansi/ansi :black))
              :axis-color  (ansi/ansi (ansi/fg-color 242))
              :bar-color   (ansi/ansi (ansi/fg-color 213))
              :bar-width   3)))
```


## Example Percentage Bar Chart 4

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/ascii-charts/ascii-chart-percentage-4.png" width="500">

``` clojure
(do
  (load-module :ascii-charts)
  (println)
  (println (ascii-charts/percentage-bar-chart 
              "Demo Small Percentage Increments"
              [ "00" "01" "02" "03" "04" "05" "06" "07" "08"
                "09" "10" "11" "12" "13" "14" "15" "16" "17"
                "18" "19" "20" "21" "22" "23" ]
              [    0    1    2    3   4     5    6    7    8
                   9   10   11   12   13   14   15   16   17
                  18   19   20   21   22   23 ])))
```
