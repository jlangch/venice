# Kira Templating

Kira is a simple templating module for Venice.


## Syntax

The <% %> tags are used to embed a section of Clojure code with side-effects. This is commonly used for control structures like loops or conditionals.

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

## Examples

### XML

Venice template:

```clojure
do
  (load-module :kira)

  (def template (str/strip-indent """\
       <users>
         ${ (docoll #(print (str }$
         <user>
           <firstname>${ (print (:first %)) }$</firstname>
           <lastname>${ (print (:last %)) }$</lastname>
           <address>
             <street>${ (print (-> % :location :street)) }$</street>
             <zip>${ (print (-> % :location :zip)) }$</zip>
             <city>${ (print (-> % :location :city)) }$</city>
           </address>
         </user>
         ${)) users) }$
       </users>
       """))

  (def data { :users [ {:first "Thomas"
                        :last "Meier"
                        :location { :street "Aareweg 3"
                                    :zip "3000"
                                    :city "Bern" }}
                       {:first "Anna"
                        :last "Steiger"
                        :location { :street "Auengasse 27"
                                    :zip "5000"
                                    :city "Aarau" }}  ] } )

  (println (kira/eval template ["${" "}$"] data)))
```

Output:

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
  </user>
  
  <user>
    <firstname>Anna</firstname>
    <lastname>Steiger</lastname>
    <address>
      <street>Auengasse 27</street>
      <zip>5000</zip>
      <city>Aarau</city>
    </address>
  </user>
  
</users>
```