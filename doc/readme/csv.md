# CSV


## CSV reader

The CSV reader reads CSV-data from a source that may be a a string, a bytebuf,
a file, a Java InputStream, or a Java Reader.

The separator and the quote char can optionally be specified. They default to
a comma and a double quote.


```clojure
(csv/read """
          8000,"Zurich",ZH
          5000,"Aarau",AG
          """))
            
;; => (["8000" "Zurich" "ZH"] 
;;     ["5000" "Aarau" "AG"])
```

With alternate quote and separator:

```clojure
(csv/read """
          8000,'Zurich','Wipkingen, X-''1''',ZH
          3000,'Bern','',BE
          5000,'Aarau',,,
          """
          :separator "," 
          :quote "'")
            
;; => (["8000" "Zurich" "Wipkingen, X-'1'" "ZH"] 
;;     ["3000" "Bern" "" "BE"] 
;;     ["5000" "Aarau" nil nil])
```


## CSV writer

Writes data in CSV format to a string or a Java Writer

```clojure
(csv/write-str [[1 "AC" false] [2 "WS" true]])
  
  ;; => "1,AC,false\n2,WS,true"
```

With alternate quote, separator, and newline:

```clojure
(csv/write-str [[1 "AC" false] [2 "WS;'-1'" true]]
               :quote "'"
               :separator ";"
               :newline :cr+lf)
                 
;; => "1;AC;false\r\n2;'WS;''-1';true"
```


Writes data in CSV format to a file:

```clojure
(csv/write (io/file "test.csv") [[1 "AC" false] [2 "WS" true]])
``
