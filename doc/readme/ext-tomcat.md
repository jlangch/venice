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
 
 
## Start Tomcat from the REPL with the built-in 'HelloWorld' demo 

Start a REPL:

```text
venice> (load-module :tomcat ['tomcat :as 'tc])
venice> (def server (tc/start (tc/hello-world-servlet) {:await? false, :base-dir "."}))
  :
venice> (tc/shutdown server)
```

Open a browser with the URL `http://localhost:8080` or from the REPL: `(sh/open "http://localhost:8080")`


## Define a servlet

```clojure
(do
  (import :com.github.jlangch.venice.util.servlet.IVeniceServlet
          :com.github.jlangch.venice.util.servlet.VeniceServlet)

  (load-module :tomcat ['tomcat :as 'tc])

  (defn send-text [res title text]
    (. res :setStatus 200)
    (. res :setContentType "text/html; charset=utf-8")
    (-> (. res :getWriter)
        (. :println (tc/html-box-page title text))))

  (defn my-hello-world-servlet  []
    (. :VeniceServlet :new
      (proxify :IVeniceServlet
        { :init (fn [config] nil)
          :destroy (fn [] nil)
          :doGet (fn [req res servlet] (send-text res "Demo" "Hello World"))
          :doHead (fn [req res servlet] (tc/send-not-implemented res "HTTP Method HEAD"))
          :doPost (fn [req res servlet] (tc/send-not-implemented res "HTTP Method POST"))
          :doPut (fn [req res servlet] (tc/send-not-implemented res "HTTP Method PUT"))
          :doDelete (fn [req res servlet] (tc/send-not-implemented res "HTTP Method DELETE"))
          :getLastModified (fn [req] -1) })))

  (defn stop []
    (tc/shutdown server))
 
  ; start Tomcat
  (def server (tc/start (my-hello-world-servlet) 
                        {:await? false, :base-dir "."}))
  
  (println "Tomcat started.")
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
