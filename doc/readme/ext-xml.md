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
                <b>B1</b>
                <b>B2</b>
              </a>""")))
```



Alternatively Venice can parse XML data from a _SAX InputSource_, an _InputStream_, 
a _File_ or an _URI_:


SAX InputSource

```clojure
(xml/parse (->> (. :StringReader :new "<a><b>B</b></a>")
                (. :InputSource :new)))
```

InputStream

```clojure
(try-with [is (. :java.io.FileInputStream :new (io/file "example.xml"))]
   (xml/parse is))
```

File

```clojure       
(xml/parse (io/file "example.xml"))
```

URI

```clojure       
(xml/parse "https://www.w3schools.com/xml/books.xml")
```


## Navigate through XML documents

The following examples will outline an XPath like navigation through parsed 
XML documents.


### Getting started

The XML:

```xml
<book>
  <table-of-contents/>
  <chapter name="Introduction">
    <para>Here is the intro</para>
    <para>Another paragraph</para>
  </chapter>
  <chapter name="Conclusion">
    <para>All done now</para>
  </chapter>
</book>
```

Parse the XML

```clojure
(do
   (load-module :xml)
   
   (def nodes (xml/parse-str 
	               """<?xml version="1.0" encoding="UTF-8"?>
	                  <book>
	                    <table-of-contents/>
	                    <chapter name="Introduction">
	                      <para>Here is the intro</para>
	                      <para>Another paragraph</para>
	                    </chapter>
	                    <chapter name="Conclusion">
	                      <para>All done now</para>
	                    </chapter>
	                  </book>""")))
```

`xml/parse-str` parses the XML into a tree structure like this

```clojure
{:tag "book" :content [{:tag "table-of-contents"} ...]}
```


## Querying the parsed nodes

### Getting children

Descends into the node's child elements

```clojure
(xml/children [nodes])
```

which results in

```clojure
({:tag "table-of-contents"}
 {:tag "chapter"
  :attrs {:name "Introduction"}
  :content [...]}
 {:tag "chapter"
  :attrs {:name "Conclusion"}
  :content [...] })
```

### Select children based on their tag

```clojure
(let [path [(xml/tag= "chapter")
            (xml/tag= "para")
            xml/text]]
  (xml/path-> path nodes))
```

which results in

```clojure
'("Here is the intro" "Another paragraph" "All done now")
```


### Select children based on their tag and attributes

```clojure
(let [path [(xml/tag= "chapter")
            (xml/attr= :name "Introduction")
            (xml/tag= "para")
            xml/text
            second]]
  (xml/path-> path nodes))
```

which results in

```clojure
"Another paragraph"
```

Alternatively the query can be written as:

```clojure
(->> [nodes]
     ((xml/tag= "chapter"))
     ((xml/attr= :name "Introduction"))
     ((xml/tag= "para"))
     xml/text
     second)
```
