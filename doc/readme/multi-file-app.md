# Multi-File Venice App

_documentation in progress..._

```text
billing
├── billing.venice
├── utils
│   ├── util.venice
│   └── render.venice
└── data
    ├── bill.template
    └── logo.jpg
```

Dependent on your needs there are three ways to package a Venice application:

* [Venice Application Archive](#venice-application-archive)
* [Application JAR](#application-jar)
* [Uber JAR](#uber-jar)


## Venice Application Archive

A Venice application archive is a lightweight solution for distributing and deploying 
applications if the application is built from Venice source and resources files only
and does not require 3rd party JARs. 

### Build the application archive

```text
venice> (app/build 
            "billing"
            "billing.venice"
            { "billing.venice"      "staging/billing.venice"
              "utils/util.venice"   "staging/utils/util.venice"
              "utils/render.venice" "staging/utils/render.venice"
              "data/bill.template"  "staging/data/bill.template"
              "data/logo.jpg"       "staging/data/logo.jpg" }
            "./build")
```

This creates the application archive `./build/billing.zip`.


### A look at the implementation

This fragment of the application archive's main file 'billing.venice' 
demonstrates how to load additional files and resources from the archive.

```clojure
(ns billing)

;; load util and render functions
(load-file "utils/util.venice")
(load-file "utils/render.venice")

;; load the billing template
(defn load-bill-template []
  (-> (load-resource "data/bill.template")
      (bytebuf-to-string :utf-8)))

;; implementation code
(println "Started app")
...

```


### Run the application

The billing application may be deployed to file a structure like

```text
foo
├── billing.zip
└── libs
    ├── ...
    └── venice-1.7.11.jar
```

It can be started from a terminal with

```shell
mars$ cd ~/foo
mars$ java -jar libs/venice-1.7.11.jar -app billing
```



## Application JAR

Alternatively the Venice files can be packaged to a Java JAR .


### Build the application JAR

```shell
mars$ cd ~/staging
mars$ jar cf billing.jar \
          billing.venice \
          utils/util.venice \
          utils/render.venice \
          data/bill.template \
          data/logo.jpg
```


### A look at the implementation

This fragment of the application's main file 'billing.venice' 
demonstrates how to load additional files and resources from the JAR.

```clojure
(ns billing)

;; load util and render functions
(load-classpath-file "utils/util.venice")
(load-classpath-file "utils/render.venice")

;; load the billing template
(defn load-bill-template []
  (-> (load-classpath-resource "data/bill.template")
      (bytebuf-to-string :utf-8)))

;; implementation code
(println "Started app")
...

```


### Run the application

The billing application JAR may be deployed to a file structure like

```text
foo
└── libs
    ├── billing.jar
    ├── venice-1.7.11.jar
    └── openpdf-1.3.11.jar
```

It can be started from a terminal with

```shell
mars$ cd ~/foo
mars$ java -server \
           -Xmx2G \
           -cp "libs/*" \
           com.github.jlangch.venice.Launcher \
           -cp-file billing.venice
```


## Uber JAR

_TODO_

