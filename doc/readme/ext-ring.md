# Ring WEB applications module

Venice Ring is a port of Clojure's Ring web applications library.


## Hello World WEB App

```clojure
(do
  (load-module :tomcat)
  (load-module :ring)

  (defn hello-world-handler [request]
    { :status 200
      :headers { "Content-Type" "text/plain; charset=utf-8" }
      :body (str/escape-html "Hello World") })

  ;; A route is defined by a HTTP verb, a URI filter and a handle function.
  ;; If multiple routes match the route with the longest URI filter will be 
  ;; chosen.
  (def routes [
    [:get "/**"  hello-world-handler]
  ])

  (tc/run-tomcat
    (ring/create-servlet (-> (ring/match-routes routes)  ; >--+
                                                         ;    |
                             (ring/mw-print-uri)         ; ^  |
                             (ring/mw-debug :on)))       ; +--+
    {:await? false}))
```


## Hello World WEB App with sessions activated

```clojure
(do
  (load-module :tomcat)
  (load-module :ring)

  (defn hello-world-handler [request]
    { :status 200
      :headers { "Content-Type" "text/plain; charset=utf-8" }
      :body (str/escape-html "Hello World") })

  ;; A route is defined by a HTTP verb, a URI filter and a handle function.
  ;; If multiple routes match the route with the longest URI filter will be 
  ;; chosen.
  (def routes [
    [:get "/**"  hello-world-handler]
  ])

  ;; The 'mw-request-counter' middlware uses the session to store the 
  ;; session's request count and prints it if debug is :on
  (tc/run-tomcat
    (ring/create-servlet (-> (ring/match-routes routes)  ; >--+
                                                         ;    |
                             (ring/mw-request-counter)   ; ^  |
                             (ring/mw-add-session 3600)  ; |  |
                             (ring/mw-print-uri)         ; |  |
                             (ring/mw-debug :on)))       ; +--+
    {:await? false}))
```

## Hello World WEB App with request/response dump

```clojure
(do
  (load-module :tomcat)
  (load-module :ring)

  (defn hello-world-handler [request]
    { :status 200
      :headers { "Content-Type" "text/plain; charset=utf-8" }
      :body (str/escape-html "Hello World") })

  ;; A route is defined by a HTTP verb, a URI filter and a handle function.
  ;; If multiple routes match the route with the longest URI filter will be 
  ;; chosen.
  (def routes [
    [:get "/**"  hello-world-handler]
  ])

  (tc/run-tomcat
    (ring/create-servlet (-> (ring/match-routes routes)  ; >--+
                                                         ;    |
                             (ring/mw-dump-response)     ; ^  |
                             (ring/mw-dump-request)      ; |  |
                             (ring/mw-print-uri)         ; |  |
                             (ring/mw-debug :on)))       ; +--+
    {:await? false}))
```


## Sample WEB App with multiple routes

```clojure
(do
  (load-module :tomcat)
  (load-module :ring)

  (defn hello-world-handler [request]
    { :status 200
      :headers { "Content-Type" "text/plain; charset=utf-8" }
      :body (str/escape-html "Hello World") })

  (defn test-handler [request]
    { :status 200
      :headers { "Content-Type" "text/plain; charset=utf-8" }
      :body (str/escape-html "Test") })

  (defn image-handler [request]
    (let [name (last (str/split (:uri request) "/"))
          file (io/file (io/user-dir) name)]
      (if (io/exists-file? file)
        { :status 200
          :headers { "Content-Type" (io/mime-type name) }
          :body file }
        (ring/not-found-response "File not found"))))

  ;; A route is defined by a HTTP verb, a URI filter and a handle function.
  ;; If multiple routes match the route with the longest URI filter will be 
  ;; chosen.
  (def routes [
    [:get "/**"                   hello-world-handler]
    [:get "/test"                 test-handler]
    [:get "/test/**"              test-handler]
    [:get "/static/images/*.png"  image-handler]
  ])

  (tc/run-tomcat
    (ring/create-servlet (-> (ring/match-routes routes)  ; >--+
                                                         ;    |
                             (ring/mw-dump-response)     ; ^  |
                             (ring/mw-dump-request)      ; |  |
                             (ring/mw-request-counter)   ; |  |
                             (ring/mw-add-session 3600)  ; |  |
                             (ring/mw-print-uri)         ; |  |
                             (ring/mw-debug :on)))       ; +--+
    {:await? false}))
```


## Demo WEB App with navigation and login/logout

Download the [Demo WEB App](../examples/scripts/webapp/login-webapp.venice) to the 
local filesystem as 'login-webapp.venice' and run the WebApp from the REPL:

```text
venice> (load-file "login-webapp.venice")
```

Tomcat logs the startup like:

```
Aug 08, 2019 9:19:52 PM org.apache.coyote.AbstractProtocol init
INFO: Initializing ProtocolHandler ["http-nio-8080"]
Aug 08, 2019 9:19:52 PM org.apache.catalina.core.StandardService startInternal
INFO: Starting service [Tomcat]
Aug 08, 2019 9:19:52 PM org.apache.catalina.core.StandardEngine startInternal
INFO: Starting Servlet engine: [Apache Tomcat/9.0.19]
Aug 08, 2019 9:19:52 PM org.apache.coyote.AbstractProtocol start
INFO: Starting ProtocolHandler ["http-nio-8080"]
=> org.apache.catalina.startup.Tomcat@22df874e
``

Stop the WebApp with `ctrl-c`.
