# XML

```clojure
(do
   ;; load the Venice XML extension module
   (load-module :xml)
   
   (str (xml/parse-str "<a><b>B</b></a>")))
```
