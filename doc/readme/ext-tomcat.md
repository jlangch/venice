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

  (def tomcat-opts {:await? false, :base-dir ".", :port 8080})

  (defn my-servlet []
    (tc/create-servlet {:doGet (fn [req res _] (tc/send-ok res "Hello World"))}))

  ; start the Tomcat server
  (let [server (tc/start (my-servlet) tomcat-opts)]
    (defn stop [] (tc/shutdown server)))
  
  (println "Tomcat started on port ~(:port tomcat-opts).")
  (println "Open a browser:      (sh/open \"http://localhost:8080\")")
  (println "Stop it by calling:  (stop)"))
```


## Define a servlet

```clojure
(do
  (load-module :tomcat ['tomcat :as 'tc])

  (def tomcat-opts {:await? false, :base-dir ".", :port 8080})

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
  (let [server (tc/start (my-servlet) tomcat-opts)]
    (defn stop [] (tc/shutdown server)))
  
  (println "Tomcat started on port ~(:port tomcat-opts).")
  (println "Open a browser:      (sh/open \"http://localhost:8080\")")
  (println "Stop it by calling:  (stop)"))
```



## Download required 3rd party libs

Java 8:

```clojure
(do
  (load-module :tomcat)
  (tomcat/download-libs-10.0.x :dir (repl/libs-dir) :silent false))
```

Java 11+:

```clojure
(do
  (load-module :tomcat)
  (tomcat/download-libs-10.1.x :dir (repl/libs-dir) :silent false))
```



## Starting a Tomcat Server from a REPL on a Gitpod workspace

### 1. Start a Venice Gitpod workspace

Start a new Venice Gitpod workspace from [Venice Github Project](https://github.com/jlangch/venice).

Wait until the project has been checked out, compiled and the REPL started.


### 2. Setup the required libraries and directory

Run this script from the REPL:

```clojure
(do
  (load-module :tomcat ['tomcat :as 'tc])
    
  ;; Download the Tomcat 10.1.x libs for Java 11+ from Maven
  (tc/download-libs-10.1.x :dir (repl/libs-dir) :silent false)
  
  ;; Create the Tomcat base directory
  (def tomcat-base-dir (io/file (repl/home-dir) "tomcat"))
  (io/mkdir tomcat-base-dir)
  
  ;; Restart the REPL to make the new libs available to the REPL Java VM
  (repl/restart))
```

*Note: The Tomcat base directory will also be used when starting the Tomcat server!*

The changed classpath (after the REPL restart) can be checked with

```text
venice> !classpath
REPL classpath:
  libs
  libs/jakarta.annotation-api-2.1.1.jar
  libs/jansi-2.4.0.jar
  libs/tomcat-embed-core-10.1.1.jar
  libs/venice-1.10.26.jar
```


### 3. Start a Tomcat server

Run this script from the REPL:

```clojure
(do
  (load-module :tomcat ['tomcat :as 'tc])

  (def tomcat-base-dir (io/file-path (io/file (repl/home-dir) "tomcat")))
  
  (def tomcat-opts {:await? false, :base-dir tomcat-base-dir", :port 8080})

  (defn my-servlet []
    (tc/create-servlet {:doGet (fn [req res _] (tc/send-ok res "Hello World"))}))

  ; start the Tomcat server
  (let [server (tc/start (my-servlet) tomcat-opts)]
    (defn stop [] (tc/shutdown server)))
  
  (println)
  (println "Tomcat started on port ~(:port tomcat-opts).")
  (println "Stop it by calling: (stop)"))
```


### 4. Make the HTTP Port public available

Navigate to the **PORTS** section and make the 8080 port public.

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/gitpod/gitpod-repl-tomcat-ports.png">



### 5. Access the WebApp

From the **PORTS** section copy the URL for port 8080. 

This URL can now be used with any local browser to access the WebApp on the remote Gitpod workspace server.

The simple WebApp looks like this in the Safari browser:

<img src="https://github.com/jlangch/venice/blob/master/doc/assets/gitpod/gitpod-repl-tomcat-webapp.png">



