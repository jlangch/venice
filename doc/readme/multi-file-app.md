# Multi-File Venice App

Assuming you've created a Venice application composed of multiple source
and resource files, how do you distribute and deploy such an application?

To hassle-free deliver the application some kind of packaging is required.

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
staging
├── billing.venice
├── utils
│   ├── util.venice
│   └── render.venice
└── data
    ├── bill.template
    └── logo.jpg
```

Building the application archive from a REPL:

```text
venice> (load-module :app) 
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

This creates the application archive `./build/billing.zip` with 
the main file 'billing.venice'. The main file is automatically 
executed when the app is started.



### A look at the implementation

This fragment of the application archive's main file 'billing.venice' 
demonstrates how to load additional files and resources from the archive.
'billing.venice' is the application's main file and automatically executed
when the app is started. It bootstraps the application.

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
mars$ java -jar libs/venice-1.7.11.jar -app billing.zip
```



## Application JAR

Alternatively the Venice files can be packaged to a Java resource only JAR.


### Build the application JAR

```text
staging
├── billing.venice
├── utils
│   ├── util.venice
│   └── render.venice
└── data
    ├── bill.template
    └── logo.jpg
```

```shell
mars$ cd ~/staging
mars$ jar cf billing.jar utils data billing.venice
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
mars$ java -cp "libs/*" \
           com.github.jlangch.venice.Launcher \
           -cp-file billing.venice
```

Compared to a Venice application archive the application's main file must be
known to launch the app (the '-cp-file' option in the example).


## Uber JAR

_TODO_

