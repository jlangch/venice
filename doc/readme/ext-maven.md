# Maven 


## Running Maven commands

```clojure 
(do
  (load-module :maven)
  
  (->> (maven/mvn "/Users/foo/projects/my-project"
                  "compile")
       (println)))
```

*Note:  Relies on the environment variable `MAVEN_HOME` to access Maven. As an alternative Maven can be installed locally for the REPL.*


## Installing Maven locally for the REPL


```clojure
(do
  (load-module :maven)  
  (maven/install))
```


## Dependency tree for artifacts

The function `maven/dependency-tree` expects more artifacts in the format "{group-id}:{artifact-id}:{version}" with an optional scope and builds the dependency tree with the help of Maven.

The scope is one of:       
  * `:compile` - build, test and run
  * `:provided` - build and test
  * `:runtime` - test and run
  * `:test` - compile and test

Implicitly excludes dependencies with the group ids (except for :test scope):
  * org.junit.*
  * org.opentest4j
  * org.apiguardian
  * junit
 
 
**Example 1**
 
```clojure
(do
  (load-module :maven)
  
  (maven/dependencies [ "org.knowm.xchart:xchart:3.8.6" ] 
                      :scope :compile
                      :verbose false))
```

prints

```
org.knowm.xchart:xchart:jar:3.8.6:compile
+- de.erichseifert.vectorgraphics2d:VectorGraphics2D:jar:0.13:compile
+- de.rototor.pdfbox:graphics2d:jar:3.0.0:compile
|  \- org.apache.pdfbox:pdfbox:jar:3.0.0:compile
|     +- org.apache.pdfbox:pdfbox-io:jar:3.0.0:compile
|     +- org.apache.pdfbox:fontbox:jar:3.0.0:compile
|     \- commons-logging:commons-logging:jar:1.2:compile
\- com.madgag:animated-gif-lib:jar:1.4:compile
```

**Example 2**

```clojure
(do
  (load-module :maven)
  
  (maven/dependencies [ "org.knowm.xchart:xchart:3.8.6" ] 
                      :scope :compile
                      :verbose true))
```

prints

```
+- de.erichseifert.vectorgraphics2d:VectorGraphics2D:jar:0.13:compile
+- de.rototor.pdfbox:graphics2d:jar:3.0.0:compile
|  \- org.apache.pdfbox:pdfbox:jar:3.0.0:compile
|     +- org.apache.pdfbox:pdfbox-io:jar:3.0.0:compile
|     |  \- (commons-logging:commons-logging:jar:1.2:compile - omitted for duplicate)
|     +- org.apache.pdfbox:fontbox:jar:3.0.0:compile
|     |  +- (org.apache.pdfbox:pdfbox-io:jar:3.0.0:compile - omitted for duplicate)
|     |  \- (commons-logging:commons-logging:jar:1.2:compile - omitted for duplicate)
|     \- commons-logging:commons-logging:jar:1.2:compile
\- com.madgag:animated-gif-lib:jar:1.4:compile
```


*Note:*

*Creates a temporary Maven project with the artifacts as dependencies, runs the Maven dependency:tree command and removes the project afterwards.*



## Download Maven artifacts


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
