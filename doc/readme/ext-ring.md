# Ring WEB applications module

Venice Ring is a port of Clojure's Ring web applications library.


## Hello World WEB App

```clojure
(do
  (load-module :tomcat ['tomcat :as 'tc])
  (load-module :ring)

  (defn hello-world-handler [request]
    { :status 200
      :headers { "Content-Type" "text/html; charset=utf-8" }
      :body (ring/html-box-page "Demo" "Hello World") })

  ;; A route is defined by a HTTP verb, a URI filter and a handle function.
  ;; If multiple routes match the route with the longest URI filter will be 
  ;; chosen.
  (def routes [
    [:get "/**"  hello-world-handler]
  ])

  (defn start []
    (tc/start (ring/create-servlet (-> (ring/match-routes routes)  ; >--+
                                                                   ;    |
                                       (ring/mw-print-uri)         ; ^  |
                                       (ring/mw-debug :on)))       ; +--+
              {:await? false}))
  
  (defn stop []
    (tc/shutdown server))

  ;; start Tomcat
  (def server (start))

  (println "Tomcat started.")
  (println "Open a browser:      (sh/open \"http://localhost:8080\")")
  (println "Stop it by calling:  (stop)"))
```


## Hello World WEB App with sessions activated

```clojure
(do
  (load-module :tomcat ['tomcat :as 'tc])
  (load-module :ring)

  (defn hello-world-handler [request]
    { :status 200
      :headers { "Content-Type" "text/html; charset=utf-8" }
      :body (ring/html-box-page "Demo" "Hello World") })

  ;; A route is defined by a HTTP verb, a URI filter and a handle function.
  ;; If multiple routes match the route with the longest URI filter will be 
  ;; chosen.
  (def routes [
    [:get "/**"  hello-world-handler]
  ])

  ;; The 'mw-request-counter' middlware uses the session to store the 
  ;; session's request count and prints it if debug is :on
  (defn start []
    (tc/start (ring/create-servlet (-> (ring/match-routes routes)  ; >--+
                                                                   ;    |
                                       (ring/mw-request-counter)   ; ^  |
                                       (ring/mw-add-session 3600)  ; |  |
                                       (ring/mw-print-uri)         ; |  |
                                       (ring/mw-debug :on)))       ; +--+
              {:await? false}))
  
  (defn stop []
    (tc/shutdown server))

  ;; start Tomcat
  (def server (start))

  (println "Tomcat started.")
  (println "Open a browser:      (sh/open \"http://localhost:8080\")")
  (println "Stop it by calling:  (stop)"))
```


## Hello World WEB App with request/response dump

```clojure
(do
  (load-module :tomcat)
  (load-module :ring)

  (defn hello-world-handler [request]
    { :status 200
      :headers { "Content-Type" "text/html; charset=utf-8" }
      :body (ring/html-box-page "Demo" "Hello World") })

  ;; A route is defined by a HTTP verb, a URI filter and a handle function.
  ;; If multiple routes match the route with the longest URI filter will be 
  ;; chosen.
  (def routes [
    [:get "/**"  hello-world-handler]
  ])

  (defn start []
    (tc/start (ring/create-servlet (-> (ring/match-routes routes)  ; >--+
                                                                   ;    |
                                       (ring/mw-dump-response)     ; ^  |
                                       (ring/mw-dump-request)      ; |  |
                                       (ring/mw-print-uri)         ; |  |
                                       (ring/mw-debug :on)))       ; +--+
              {:await? false}))
  
  (defn stop []
    (tc/shutdown server))

  ;; start Tomcat
  (def server (start))

  (println "Tomcat started.")
  (println "Open a browser:      (sh/open \"http://localhost:8080\")")
  (println "Stop it by calling:  (stop)"))
```


## Sample WEB App with multiple routes

```clojure
(do
  (load-module :tomcat ['tomcat :as 'tc])
  (load-module :ring)

  (defn hello-world-handler [request]
    { :status 200
      :headers { "Content-Type" "text/html; charset=utf-8" }
      :body (ring/html-box-page "Demo" "Hello World") })

  (defn test-handler [request]
    { :status 200
      :headers { "Content-Type" "text/html; charset=utf-8" }
      :body (ring/html-box-page "Demo" "Test") })

  (defn image-handler [request]
    ;; for simplicity, there is no Path-Traversal check!
    (let [name (last (str/split (:uri request) "/"))
          file (io/file (io/user-dir) name)]
      (if (io/exists-file? file)
        { :status 200
          :headers { "Content-Type" (io/mime-type name) }
          :body file }
        { :status 404
          :headers { "Content-Type" "text/html; charset=utf-8" }
          :body (ring/html-box-page "Not Found" 
                                    "The file \"~{name}\" does not exist!") })))

  ;; A route is defined by a HTTP verb, a URI filter and a handle function.
  ;; If multiple routes match the route with the longest URI filter will be 
  ;; chosen.
  (def routes [
    [:get "/**"                   hello-world-handler]
    [:get "/test"                 test-handler]
    [:get "/test/**"              test-handler]
    [:get "/static/images/*.png"  image-handler]
  ])

  (defn start []
    (tc/start (ring/create-servlet (-> (ring/match-routes routes)  ; >--+
                                                                   ;    |
                                       (ring/mw-dump-response)     ; ^  |
                                       (ring/mw-dump-request)      ; |  |
                                       (ring/mw-request-counter)   ; |  |
                                       (ring/mw-add-session 3600)  ; |  |
                                       (ring/mw-print-uri)         ; |  |
                                       (ring/mw-debug :on)))       ; +--+
              {:await? false}))
  
  (defn stop []
    (tc/shutdown server))

  ;; start Tomcat
  (def server (start))

  (println "Tomcat started.")
  (println "Open a browser:      (sh/open \"http://localhost:8080\")")
  (println "                     (sh/open \"http://localhost:8080/test\")")
  (println "                     (sh/open \"http://localhost:8080/static/images/foo.png\")")
  (println "Stop it by calling:  (stop)"))
```


## Demo WEB App with navigation and login/logout

Download the [Demo WEB App](../examples/scripts/webapp/login-webapp.venice) to the 
local filesystem as 'login-webapp.venice' and run the WebApp from the REPL:

```text
venice> (load-file "login-webapp.venice")
```

Tomcat logs the startup like:

```
Oct 19, 2022 8:19:30 PM org.apache.coyote.AbstractProtocol init
INFO: Initializing ProtocolHandler ["http-nio-8080"]
Oct 19, 2022 8:19:31 PM org.apache.catalina.core.StandardService startInternal
INFO: Starting service [Tomcat]
Oct 19, 2022 8:19:31 PM org.apache.catalina.core.StandardEngine startInternal
INFO: Starting Servlet engine: [Apache Tomcat/9.0.68]
Oct 19, 2022 8:19:31 PM org.apache.coyote.AbstractProtocol start
INFO: Starting ProtocolHandler ["http-nio-8080"]
Tomcat started.
Open a browser:      (sh/open "http://localhost:8080")
Stop it by calling:  (stop)
=> nil
```
