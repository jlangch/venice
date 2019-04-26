# Advanced String features

## Triple quoted, multi-line strings

```clojure
(do
   (def s1 """{ "fruit": "apple", "color": "red" }""")

   (def s2 """{ 
                "fruit": "apple",
                "color": "red" 
              }""")

   ; remove the indentation on multi-line strings
   (def s3 (str/strip-indent """\
                {
                  "fruit": "apple",
                  "color": "red"
                }"""))

   ; remove the indentation on multi-line strings
   (def s4 (str/strip-margin
             """{
                |  "fruit": "apple",
                |  "color": "red"
                |}"""))

   (println s1)
   (println s2)
   (println s3)
   (println s4))
```

## Interpolation 

Interpolation is controlled using `~{}` and `~()` forms. The former is 
used for simple value replacement while the latter can be used to
embed the results of arbitrary function invocation into the produced 
string.

_Interpolation is implemented as a reader macro. It's parsed at read time and turned into a_ 
`(str args)` expression.

```clojure
(do
   (let [x 100] 
      (println "x: ~{x}")
      (println "f(x): ~(inc x)")))
```

```clojure
(do
   (let [x 100] 
      (println """x: ~{x}""")
      (println """f(x): ~(inc x)""")))
```
