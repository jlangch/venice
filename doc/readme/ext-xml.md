# XML

Parses an XML string into Venice data structures.

Returns a tree of XML element maps with the
keys :tag (XML element name), :attrs (XML element attributes), 
and :content (XML element content).

```clojure
(do
   ;; load the Venice XML extension module
   (load-module :xml)
   
   (str (xml/parse-str "<a><b>B</b></a>"))
   
   (str (xml/parse-str 
           """<?xml version="1.0" encoding="UTF-8"?>
              <a a1="100">
                <b>B</b>
              </a>""")))
```

