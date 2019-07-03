# Jackson JSON

Venice supports Jackson if the [Jackson](https://github.com/FasterXML/jackson) libs are on the runtime classpath:

 - com.fasterxml.jackson.core:jackson-core:2.9.9
 - com.fasterxml.jackson.core:jackson-databind:2.9.9
 - com.fasterxml.jackson.core:jackson-core:2.9.9
 - com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.9.9 (optional Jdk8 module)
 
The Jackson _jdk8_ module is loaded automatically if it is available
 

```clojure
(do
   ;; load the Venice JSON extension module
   (load-module :jackson)
   
   ;; build json from a map (returns a json string)
   (jackson/to-json {:a 100 :b 100 :c [10 20 30]})
   (jackson/to-pretty-json [{:a 100 :b 100}, {:a 200 :b 200}])

   ;; pretty print json (returns a json string)
   (jackson/pretty-print (jackson/to-json {:a 100 :b 100}))

   ;; parse json from a string (returns a map/list)
   (jackson/parse """{"a": 100, "b": 100, "c": [10,20,30]}""")
   (jackson/parse """[{"a": 100,"b": 100}, {"a": 200, "b": 200}]"""))
```



#### Download required 3rd party libs

```clojure
(do
  (load-module :maven)
  
  (maven/download "com.fasterxml.jackson.core:jackson-core:2.9.9"))
  (maven/download "com.fasterxml.jackson.core:jackson-databind:2.9.9"))
  (maven/download "com.fasterxml.jackson.core:jackson-core:2.9.9"))
  (maven/download "com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.9.9"))
```

