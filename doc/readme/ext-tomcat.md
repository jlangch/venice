# Apache Tomcat

The Apache Tomcat extension module provides functions to start an embedded 
Tomcat server and running servlets.

Dependencies:

 - org.apache.tomcat.embed:tomcat-embed-core:9.0.68
 - javax.annotation:javax.annotation-api:1.3.2

To simplify things there is a 
[Ring style WEB App module](ext-ring.md) available.
 
 
## Start Tomcat from the REPL with the built-in 'HelloWorld' demo 

Start a REPL:

```text
venice> (load-module :tomcat ['tomcat :as 'tc])
venice> (def server (tc/start (tc/hello-world-servlet) {:await? false}))
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

  (defn send-text [res status text]
    (. res :setStatus status)
    (. res :setContentType "text/html")
    (-> (. res :getWriter)
        (. :println """<html><body><p>~(str/escape-html text)</p></body></html>""")))

  (defn my-hello-world-servlet  []
    (. :VeniceServlet :new
      (proxify :IVeniceServlet
        { :init (fn [config] nil)
          :destroy (fn [] nil)
          :doGet (fn [req res servlet] (send-text res 200 "Hello World"))
          :doHead (fn [req res servlet] (send-text res 404 "Not Found"))
          :doPost (fn [req res servlet] (send-text res 404 "Not Found"))
          :doPut (fn [req res servlet] (send-text res 404 "Not Found"))
          :doDelete (fn [req res servlet] (send-text res 404 "Not Found"))
          :getLastModified (fn [req] -1) })))

  (defn stop []
    (tc/shutdown server))
 
  ; start Tomcat
  (def server (tc/start (my-hello-world-servlet) {:await? false}))
  
  (println "Tomcat started.")
  (println "Open a browser:      (sh/open \"http://localhost:8080\")")
  (println "Stop it by calling:  (stop)"))
```


## Download required 3rd party libs

```clojure
(do
  (load-module :maven)
  
  (maven/download "org.apache.tomcat.embed:tomcat-embed-core:9.0.68")
  (maven/download "javax.annotation:javax.annotation-api:1.3.2"))
```
