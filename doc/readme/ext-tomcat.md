# Apache Tomcat

The Apache Tomcat extension module provides functions to start an embedded 
Tomcat server and running servlets.

Dependencies (Java 8):

 - org.apache.tomcat.embed:tomcat-embed-core:10.0.27
 - jakarta.annotation:jakarta.annotation-api:2.1.1

Dependencies (Java 11+):

 - org.apache.tomcat.embed:tomcat-embed-core:10.1.1
 - jakarta.annotation:jakarta.annotation-api:2.1.1

To simplify things there is a 
[Ring style WEB App module](ext-ring.md) available.
 
 
 
## Start Tomcat from the REPL with a minimal servlet

Start a REPL:

```clojure
(do
  (load-module :tomcat ['tomcat :as 'tc])

  (def opts {:await? false, :base-dir ".", :port 8080})

  (defn my-servlet []
    (tc/create-servlet {:doGet (fn [req res _] (tc/send-ok res "Hello World"))}))

  ; start the Tomcat server
  (let [server (tc/start (my-servlet) opts)]
    (defn stop [] (tc/shutdown server)))
  
  (println "Tomcat started on port ~(:port opts).")
  (println "Open a browser:      (sh/open \"http://localhost:8080\")")
  (println "Stop it by calling:  (stop)"))
```


## Define a servlet

```clojure
(do
  (load-module :tomcat ['tomcat :as 'tc])

  (def opts {:await? false, :base-dir ".", :port 8080})

  (defn send-text [res title text]
    (. res :setStatus 200)
    (. res :setContentType "text/html; charset=utf-8")
    (-> (. res :getWriter)
        (. :println (tc/html-box-page title text))))

  (defn my-servlet []
    (tc/create-servlet
        { :init (fn [config] nil)
          :destroy (fn [servlet] nil)
          :doGet (fn [req res servlet] (send-text res "Demo" "Hello World"))
          :doHead (fn [req res servlet] (tc/send-not-implemented res "HTTP Method HEAD"))
          :doPost (fn [req res servlet] (tc/send-not-implemented res "HTTP Method POST"))
          :doPut (fn [req res servlet] (tc/send-not-implemented res "HTTP Method PUT"))
          :doDelete (fn [req res servlet] (tc/send-not-implemented res "HTTP Method DELETE"))
          :getLastModified (fn [req] -1) }))

  ; start the Tomcat server
  (let [server (tc/start (my-servlet) opts)]
    (defn stop [] (tc/shutdown server)))
  
  (println "Tomcat started on port ~(:port opts).")
  (println "Open a browser:      (sh/open \"http://localhost:8080\")")
  (println "Stop it by calling:  (stop)"))
```



## Download required 3rd party libs

Java 8:

```clojure
(do
  (load-module :maven ['maven :as 'm])
  (m/download "org.apache.tomcat.embed:tomcat-embed-core:10.0.27")
  (m/download "jakarta.annotation:jakarta.annotation-api:2.1.1"))
```

Java 11+:

```clojure
(do
  (load-module :maven ['maven :as 'm])
  (m/download "org.apache.tomcat.embed:tomcat-embed-core:10.1.1")
  (m/download "jakarta.annotation:jakarta.annotation-api:2.1.1"))
```



## Starting a Tomcat Server from a REPL on a Gitpod workspace

### Start a Venice Gitpod workspace

Start a new Venice Gitpod workspace from [Venice Github Project](https://github.com/jlangch/venice).

Wait until the project has been checked out, compiled and the REPL started.


### Setup the required libraries and directory

Run this script from the REPL:

```clojure
(do
  (load-module :maven ['maven :as 'm])
  
  ;; Download the Tomcat libs
  (m/download "org.apache.tomcat.embed:tomcat-embed-core:10.1.1" :dir "/workspace/repl/libs")
  (m/download "jakarta.annotation:jakarta.annotation-api:2.1.1" :dir "/workspace/repl/libs")
  
  ;; Create the Tomcat base directory
  (io/mkdir "/workspace/repl/tomcat")
  
  ;; Restart the REPL to make the new libs available to the REPL Java VM
  (repl/restart))
```

*Note: The Tomcat base directory "/workspace/repl/tomcat" will also be used when starting the server!*

The changed classpath (after the REPL restart) can be checked with

```shell
venice> !classpath
REPL classpath:
  libs
  libs/jakarta.annotation-api-2.1.1.jar
  libs/jansi-2.4.0.jar
  libs/tomcat-embed-core-10.1.1.jar
  libs/venice-1.10.25-SNAPSHOT.jar
```


### Start a Tomcat server

Run the script from the REPL:

```clojure
(do
  (load-module :tomcat ['tomcat :as 'tc])
  
  (def opts {:await? false, :base-dir "/workspace/repl/tomcat", :port 8080})

  (defn my-servlet []
    (tc/create-servlet {:doGet (fn [req res _] (tc/send-ok res "Hello World"))}))

  ; start the Tomcat server
  (let [server (tc/start (my-servlet) opts)]
    (defn stop [] (tc/shutdown server)))
  
  (println)
  (println "Tomcat started on port ~(:port opts).")
  (println "Stop it by calling: (stop)"))
```


### Make the HTTP Port public available

Navigate to the **PORTS** section and make the 8080 port public.

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/gitpod/gitpod-repl-tomcat-ports.png">



### Access the WebApp

From the **PORTS** section copy the URL for port 8080 (its an URL like *https://8080-jlangch-venice-7kxstp3pfji.ws-eu72.gitpod.io/*). 

This URL can now be put to any local browser to access the WebApp.

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/gitpod/gitpod-repl-tomcat-webapp.png">



