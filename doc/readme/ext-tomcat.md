# Apache Tomcat

The Apache Tomcat extension module provides starting an embedded Tomcat server and running servlets.

dependencies:

 - tomcat-embed-core-9.0.19.jar
 - javax.annotation-api-1.3.2.jar
 
 
## Start Tomcat with the 'HelloWorld' demo servlet

```text
venice> (load-module :tomcat)
venice> (tc/run-tomcat (tc/hello-world-servlet) {:await? false})
```

Open a browser with the URL `http://localhost:8080`

Type `ctrl-c` in the REPL to shutdown the server.


## Define a servlet

```clojure
(defn reply-text[res status text]
  (. res :setStatus status)
  (. res :setContentType "text/html")
  (let [w (. res :getWriter)]
    (. w :println (str "<html><body><p>" text "</p></body></html>"))))

(defn hello-world-servlet
  []
  (. :VeniceServlet :new
    (proxify :IVeniceServlet
      { :init (fn [config] nil)
        :destroy (fn [] nil)
        :doGet (fn [req res servlet] (reply-text res 200 "Hello World"))
        :doHead (fn [req res servlet] (reply-text res 404 "Not Found"))
        :doPost (fn [req res servlet] (reply-text res 404 "Not Found"))
        :doPut (fn [req res servlet] (reply-text res 404 "Not Found"))
        :doDelete (fn [req res servlet] (reply-text res 404 "Not Found"))
        :doOptions (fn [req res servlet] (reply-text res 404 "Not Found"))
        :doTrace (fn [req res servlet] (reply-text res 404 "Not Found"))
        :getLastModified (fn [] -1) })))
```
