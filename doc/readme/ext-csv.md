# CSV


## CSV reader

The CSV reader reads CSV-data from a source that may be a a string, a bytebuf,
a file, a Java InputStream, or a Java Reader.

The separator and the quote char can optionally be specified. They default to
a comma and a double quote.


```clojure
(do
  (load-module :csv)
  
  (csv/read """
            8000,"Zurich",ZH
            5000,"Aarau",AG
            """)) 
```


```clojure
(do
  (load-module :csv)
  
  (csv/read """
            8000|'Zurich'|'Wipkingen 1'|ZH
            5000|'Aarau'||AG
            """
            :separator "|" 
            :quote "'")) 
```


## CSV writer

_not yet available_
