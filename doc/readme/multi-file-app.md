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

The billing application may be deployed to file structure like

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

_TODO_



## Uber JAR

_TODO_

