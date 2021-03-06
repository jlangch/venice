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
   
   ; -> {:tag "a" :content [{:tag "b" :content ["B"]}]}
   
   
   (str (xml/parse-str 
           """
           <?xml version="1.0" encoding="UTF-8"?>
           <a a1="100">
             <b>B1</b>
             <b>B2</b>
           </a>
           """)))
           
   ; -> {:tag "a" 
   ;     :attrs {:a1 "100"} 
   ;     :content [{:tag "b" :content ["B1"]} 
   ;               {:tag "b" :content ["B2"]}]}
```



Alternatively Venice can parse XML data from various sources:


String

```clojure
(xml/parse-str "<a><b>B</b></a>")
```

SAX Parser InputSource

```clojure
(xml/parse (->> (. :java.io.StringReader :new "<a><b>B</b></a>")
                (. :org.xml.sax.InputSource :new)))
```

InputStream

```clojure
(try-with [is (. :java.io.FileInputStream :new (io/file "books.xml"))]
  (xml/parse is))
```

File

```clojure       
(xml/parse (io/file "books.xml"))
```

URI

```clojure       
(xml/parse "https://www.w3schools.com/xml/books.xml")
```


## Navigate through XML documents

The following examples will outline an XPath like navigation through parsed 
XML documents.


### Getting started

The XML [books.xml](https://www.w3schools.com/xml/books.xml):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bookstore>
  <book category="cooking">
    <title lang="en">Everyday Italian</title>
    <author>Giada De Laurentiis</author>
    <year>2005</year>
    <price>30.00</price>
  </book>

  <book category="children">
    <title lang="en">Harry Potter</title>
    <author>J K. Rowling</author>
    <year>2005</year>
    <price>29.99</price>
  </book>

  <book category="web">
    <title lang="en">XQuery Kick Start</title>
    <author>James McGovern</author>
    <author>Per Bothner</author>
    <author>Kurt Cagle</author>
    <author>James Linn</author>
    <author>Vaidyanathan Nagarajan</author>
    <year>2003</year>
    <price>49.99</price>
  </book>

  <book category="web" cover="paperback">
    <title lang="en">Learning XML</title>
    <author>Erik T. Ray</author>
    <year>2003</year>
    <price>39.95</price>
  </book>
</bookstore>
```

Parse the XML

```clojure
(do
   (load-module :xml)
   
   (def nodes (xml/parse "https://www.w3schools.com/xml/books.xml")))
```

`xml/parse` parses the XML into a tree structure like this

```clojure
{:tag "bookstore" :content [{:tag "book"} ...]}
```


## Querying the parsed nodes

### Getting children

Descends into the node's child elements

```clojure
(xml/children nodes)
```

which results in

```clojure
({:tag "bookstore"}
 {:tag "book"
  :attrs {:category "cooking"}
  :content [...]}
 {...}
 {:tag "book"
  :attrs {:category "children"}
  :content [...]})
```

### Select children based on their tag

```clojure
(let [path [(xml/tag= "book")
            (xml/tag= "title")
            xml/text]]
  (xml/path-> path nodes))
```

result:

```clojure
'("Everyday Italian" "Harry Potter" "XQuery Kick Start" "Learning XML")
```


### Select children based on their tag, attributes and position

```clojure
(let [path [(xml/tag= "book")
            (xml/attr= :category "web")
            (xml/tag= "title")
            xml/text
            second]]
  (xml/path-> path nodes))
```

result:

```clojure
"Learning XML"
```

Alternatively the query can be written as:

```clojure
(->> [nodes]
     ((xml/tag= "book"))
     ((xml/attr= :category "web"))
     ((xml/tag= "title"))
     xml/text
     second)
```

### Aggregate total price

```clojure
(let [path [(xml/tag= "book")
            (xml/attr= :category "web")
            (xml/tag= "price")
            xml/text]]
  (reduce + (map decimal (xml/path-> path nodes))))
```

result:

```clojure
89.94M
```

### Define custom tag and attribute predicates

#### Tag predicate

`(xml/tag= "book")` is equivalent to
- `(xml/tagp #(== % "book"))`
- `(xml/tagp (partial == "book"))`
 
#### Attribute predicate   

`(xml/attr= :category "web")` is equivalent to 
- `(xml/attrp :category #(== % "web"))`
- `(xml/attrp :category (partial == "web"))`

  
#### Example 1: regex predicate for tag and attribute value

```clojure
(let [path [(xml/tagp #(match? % "book.*"))
            (xml/attrp :category #(match? % "web.*"))
            (xml/tag= "title")
            xml/text
            second]]
  (xml/path-> path nodes))
```

result:

```clojure
"Learning XML"
```


#### Example 2: has _:cover_ attribute

```clojure
(let [path [(xml/tag= "book")
            (xml/attrp :cover some?)
            (xml/tag= "title")
            xml/text]]
  (xml/path-> path nodes))
```

result:

```clojure
"Learning XML"
```
