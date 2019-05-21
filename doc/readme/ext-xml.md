# XML

Parses an XML string into Venice data structures

```clojure
(do
   ;; load the Venice XML extension module
   (load-module :xml)
   
   (str (xml/parse-str "<a><b>B</b></a>"))
   
   (str (xml/parse-str "<a a1="100"><b>B</b></a>")))
```

