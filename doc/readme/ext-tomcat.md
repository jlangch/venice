# Apache Tomcat

The Apache Tomcat extension module provides functions to start an embedded 
Tomcat server and running servlets.

Dependencies:

 - org.apache.tomcat.embed:tomcat-embed-core:9.0.19
 - javax.annotation:javax.annotation-api:1.3.2

To simplify things there is a 
[Ring style WEB App module](ext-ring.md) available.
 
 
## Start Tomcat from the REPL with the built-in 'HelloWorld' demo 

Start a REPL:

```text
venice> (load-module :tomcat)
venice> (tc/run-tomcat (tc/hello-world-servlet) {:await? false})
```

Open a browser with the URL `http://localhost:8080`

Type `ctrl-c` in the REPL to shutdown the server.


## Define a servlet

```clojure
(import :com.github.jlangch.venice.servlet.IVeniceServlet
        :com.github.jlangch.venice.servlet.VeniceServlet)

(load-module :tomcat)

(defn send-text
  [res status text]
  (. res :setStatus status)
  (. res :setContentType "text/html")
  (-> (. res :getWriter)
      (. :println """<html><body><p>~(str/escape-html text)</p></body></html>""")))

(defn my-hello-world-servlet
  []
  (. :VeniceServlet :new
    (proxify :IVeniceServlet
      { :init (fn [config] nil)
        :destroy (fn [] nil)
        :doGet (fn [req res servlet] (send-text res 200 "Hello World"))
        :doHead (fn [req res servlet] (send-text res 404 "Not Found"))
        :doPost (fn [req res servlet] (send-text res 404 "Not Found"))
        :doPut (fn [req res servlet] (send-text res 404 "Not Found"))
        :doDelete (fn [req res servlet] (send-text res 404 "Not Found"))
        :doOptions (fn [req res servlet] (send-text res 404 "Not Found"))
        :doTrace (fn [req res servlet] (send-text res 404 "Not Found"))
        :getLastModified (fn [req] -1) })))

; run Tomcat
(tc/run-tomcat (my-hello-world-servlet) {:await? false})
```
