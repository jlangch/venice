# Kira Templating

Kira is a simple templating library for Venice.


## Syntax

The <% %> tags are used to embed a section of Clojure code with side-effects. This is commonly used for control structures like loops or conditionals.

For example:

```clojure
(load-module :kira)
(kira/eval "<% (dotimes [x 3] %>foo<% ) %>")
=> "foofoofoo"
```

The `<%= %>` tags will be substituted for the value of the expression within them. This is used for inserting values into a template.

For example:

```clojure
(load-module :kira)
(kira/eval "Hello <%= name %>" {:name "Alice"})
=> "Hello Alice"
```

### Customized Delimiters

The delimiters can be customized:

```clojure
(load-module :kira)
(kira/eval """Hello $${= name }$$""" ["$${" "}$$"] {:name "Alice"})
=> "Hello Alice"
```
