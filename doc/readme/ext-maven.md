# Maven Utilities


Download artifacts from a [Maven repository](https://repo1.maven.org/maven2):

```clojure
(load-module :maven)
  
; download the binary 
(maven/download "org.knowm.xchart:xchart:3.5.4")

; download the binary, the sources, and the pom
(maven/download "org.knowm.xchart:xchart:3.5.4" :sources true :pom true)

; download the binary to a directory
(maven/download "org.knowm.xchart:xchart:3.5.4" :dir "./download")

; download the binary and the sources to a directory
(maven/download "org.knowm.xchart:xchart:3.5.4" :dir "./download" :sources true)

; download the binary from an alternativ repo
(maven/download "org.knowm.xchart:xchart:3.5.4" :dir "./download" :repo "https://repo1.maven.org/maven2")
```
