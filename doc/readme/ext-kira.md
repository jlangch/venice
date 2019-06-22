# Kira Templating

Kira is a simple templating module for Venice.


## Syntax

The `<% %>` tags are used to embed a section of Clojure code with side-effects. This is commonly used for control structures like loops or conditionals.

For example:

```clojure
(load-module :kira)

(kira/eval """<% (docoll #(print (str %>foo<% % " ")) xs)%>""" {:xs [1 2 3]})
;;=> "foo1 foo2 foo3 "
```

The `<%= %>` tags will be substituted for the value of the expression within them. This is used for inserting values into a template.

For example:

```clojure
(load-module :kira)

(kira/eval "Hello <%= name %>" {:name "Alice"})
;;=> "Hello Alice"
```

### Customized Delimiters

The delimiters can be customized:

```clojure
(load-module :kira)

(kira/eval """Hello $${= name }$$""" ["$${" "}$$"] {:name "Alice"})
;;=> "Hello Alice"
```

## API Documentation

### kira/eval

```clojure
(kira/eval source)
(kira/eval source bindings)
(kira/eval source delimiters bindings)
```

Evaluate a template source using an optional map of bindings. The template source can be a string, or any I/O source understood by the standard slurp function.

Example of use:

```clojure
(kira/eval "Hello <%= name %>" {:name "Bob"})
(kira/eval "Hello $= name $" ["$" "$"] {:name "Bob"})
```

### kira/fn

```clojure
(kira/fn args source)
(kira/fn args source delimiters)
```

Compile a template source into a anonymous function. This is a lot faster than `kira/eval` for repeated calls, as the template source is only parsed when the function is created.

Examples of use:

```clojure
(def hello
  (kira/fn [name] "Hello <%= name %>"))

(hello "Alice")
```



## Building blocks

### Escape output

#### Kira has built-in support for escaping XML/HTML:

```clojure
(do
  (load-module :kira)
  
  (def template (str/strip-indent """\
       <formula>
         <predicate><% (kira/escape-xml predicate) %></predicate>
       </formula>"""))

  (def data { :predicate "x > 100" })
  
  (println (kira/eval template data)))
```

Output:

```xml
<formula>
  <predicate>x &gt; 100</predicate>
</formula>
```

#### Custom output conversion

Any Venice function can be used to escape/convert/format output:

```clojure
(do
  (load-module :kira)
  
  (def template """timestamp: <% (print (time/format ts "yyyy-MM-dd HH:mm:ss")) %>""")

  (def data { :ts (time/local-date-time) })
  
  (println (kira/eval template data)))
```

Output:

```text
timestamp: 2019-06-22 19:21:07
```


### Loops

### Conditionals



## Examples

### XML

The XML example demonstrates _Kira_ loops, nested loops and conditionals.

Template blueprint:

```text
<users>
  ${for u in users}$
  <user>
    <firstname>...</firstname>
    <lastname>...</lastname>
    <address>
      <street>...</street>
      <zip>...</zip>
      <city>...</city>
    </address>
    ${if add-email}$
    <emails>
      ${for e in u.emails}$
      <email type="...">...</email>
      ${endfor}$
    </emails>
    ${endif}$
  </user>
  ${endfor}$
</users>
```

Venice template:

```clojure
(do
  (load-module :kira)
  
  (def template (str/strip-indent """\
       <users>
         ${ (kira/docoll users (fn [user] (kira/emit }$
         <user>
           <firstname>${ (kira/escape-xml (:first user)) }$</firstname>
           <lastname>${ (kira/escape-xml (:last user)) }$</lastname>
           <address>
             <street>${ (kira/escape-xml (-> user :location :street)) }$</street>
             <zip>${ (kira/escape-xml (-> user :location :zip)) }$</zip>
             <city>${ (kira/escape-xml (-> user :location :city)) }$</city>
           </address>
           ${ (when add-emails (kira/emit }$
           <emails>
             ${ (kira/docoll (:emails user) (fn [[type email]] (kira/emit }$
             <email type="${ (kira/escape-xml (name type)) }$"> ${ (kira/escape-xml email) }$</email>
             ${ ))) }$
           </emails>
           ${ )) }$
         </user>
         ${ ))) }$
       </users>"""))

  (def data { :users [ {:first "Thomas"
                        :last "Meier"
                        :location { :street "Aareweg 3"
                                    :zip "3000"
                                    :city "Bern" }
                        :emails { :private "thomas.meier@privat.ch"
                                  :business "thomas.meier@business.ch" } }
                       {:first "Anna"
                        :last "Steiger"
                        :location { :street "Auengasse 27"
                                    :zip "5000"
                                    :city "Aarau" }
                        :emails { :private "anna.steiger@privat.ch"
                                  :business "anna.steiger@business.ch" } } ]
              :add-emails true })

  (println (kira/eval template ["${" "}$"] data)))
```

The produced output:

```xml
<users>
  
  <user>
    <firstname>Thomas</firstname>
    <lastname>Meier</lastname>
    <address>
      <street>Aareweg 3</street>
      <zip>3000</zip>
      <city>Bern</city>
    </address>
    <emails>
      <email type="business"> thomas.meier@business.ch</email>
      <email type="private"> thomas.meier@privat.ch</email>
    </emails>
  </user>
  
  <user>
    <firstname>Anna</firstname>
    <lastname>Steiger</lastname>
    <address>
      <street>Auengasse 27</street>
      <zip>5000</zip>
      <city>Aarau</city>
    </address>
    <emails>
      <email type="business"> anna.steiger@business.ch</email>
      <email type="private"> anna.steiger@privat.ch</email>
    </emails>
  </user>
  
</users>
```


To analyze the parsed template just print it:

```clojure
  (println (kira/parse-string template ["${" "}$"]))
```
