# Ring WEB applications library

Venice Ring is a port of Clojure's Ring web applications library.


## Sample WEB App

```clojure
(load-module :tomcat)
(load-module :ring)

(defn hello-world-handler [request]
  { :status 200
    :headers { "Content-Type" "text/plain; charset=utf-8" }
    :body "Hello World" })

(defn test-handler [request]
  { :status 200
    :headers { "Content-Type" "text/plain; charset=utf-8" }
    :body "Test" })

(def routes [
  [:get "/**"       hello-world-handler]
  [:get "/test"     test-handler]
  [:get "/test/**"  test-handler]
])

(tc/run-tomcat
  (ring/create-servlet (ring/match-routes routes))
  {:await? false})
```
