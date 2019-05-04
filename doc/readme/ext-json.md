# JSON

Venice supports JSON if the [Jackson](https://github.com/FasterXML/jackson) libs are on the runtime classpath:

 - com.fasterxml.jackson.core:jackson-core:2.9.8
 - com.fasterxml.jackson.core:jackson-databind:2.9.8
 - com.fasterxml.jackson.core:jackson-core:2.9.8
 - com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.9.8 (optional Jdk8 module)
 
The Jackson _jdk8_ module is loaded automatically if it is available
 

```clojure
(do
   ;; load the Venice JSON extension module
   (load-module :json)
   
   ;; build json from a map (returns a json string)
   (json/to-json {:a 100 :b 100 :c [10 20 30]})
   (json/to-pretty-json [{:a 100 :b 100}, {:a 200 :b 200}])

   ;; pretty print json (returns a json string)
   (json/pretty-print (json/to-json {:a 100 :b 100}))

   ;; parse json from a string (returns a map/list)
   (json/parse """{"a": 100, "b": 100, "c": [10,20,30]}""")
   (json/parse """[{"a": 100,"b": 100}, {"a": 200, "b": 200}]"""))
```
