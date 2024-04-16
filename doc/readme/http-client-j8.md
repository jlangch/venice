# HTTP Client (Java 8+)

* [Overview](#overview)
* [API](#api)
* [Examples](#examples)
* [OpenAI Examples](#openai-examples)



## Overview

A HTTP client based on the Java *HttpUrlConnection*. The client runs out-of-the-box
and does not require any 3rd party libraries and runs on Java 8 and higher.

Main  features:
* Sending GET, POST, PUT, DELETE, ... requests
* Uploading files and multipart data
* Handling Server-Side-Event data streams
* First class support for JSON

*HttpUrlConnection* has the reputation of being outdated and clumsy but nevertheless
it runs on Java 8 and higher and it provides everything required for an HTTP client.
Due to its nature it only supports HTTP/1.1 and HTTP/1.2.



## API

* [Sending Requests](#sending-requests)
* [UploadingFiles](#uploading-files)
* [Uploading Multipart Data](#uploading-multipart-data)
* [Processing responses](#processing-responses)
* [Processing server-side-events](#processing-server-side-events)



### Sending Requests

`(send method uri & options)`

Send a request given a method, an uri, and request options.


#### Parameter «method»

The request method:  `:get`, `:post`, `:put`, `:delete`, ...


#### Parameter «uri»

The request URI


#### Parameter «options»

| Option             | Description |
| :---               | :---        |
| :headers           | A map of request headers. Headers can be single- or multi-value (comma separated):<br>{"X-Header-1" "value1"<br>"X-Header-2" "value1, value2, value3"} |
| :body              | An optional body to send with the request. The body may be of type *string*, *bytebuf*, or `:java.io.InputStream` |
| :conn-timeout      | An optional connection timeout in milliseconds |
| :read-timeout      | An optional read timeout in milliseconds |
| :follow-redirects  | Sets whether HTTP redirects (requests with response code 3xx) should be automatically followed. |
| :hostname-verifier | Sets the hostname verifier. An object of type `:javax.net.ssl.HostnameVerifier`.<br> Use only for HTTPS requests |
| :ssl-socket-factory | Sets the SSL socket factory. An object of type `:javax.net.ssl.SSLSocketFactory`.<br> Use only for HTTPS requests |
| :use-caches         | A boolean indicating whether or not to allow caching. Defaults to false |
| :user-agent         | User agent. Defaults to "Venice HTTP client (legacy)" |
| :debug              | Debug true/false. Defaults to false.In debug mode prints the HTTP request and response data  |


#### Return value

The send function returns a map with the HTTP response data:

| Field              | Description |
| :---               | :---        |
| :http-status       | The HTTP status (a long) |
| :content-type      | The content type. E.g.: "text/plain; charset=utf8" |
| :content-type-mimetype  | The content type's mimetype. E.g.: "text/plain" |
| :content-type-charset   | The content type's charset. E.g.: :utf-8 |
| :content-encoding  | The content transfer encoding (a keyword), if available else nil. E.g.: "gzip" |
| :content-length    | The content length (a long), if available else -1 |
| :headers           | A map of headers. key: header name, value: list of header values |
| :data-stream       | The response data input stream.<br>If the response content encoding is 'gzip', due to a request header "Accept-Encoding: gzip" wrap the data stream with a gzip input stream: `(io/wrap-is-with-gzip-input-stream (:data-stream response))` to uncompress the data.<br><br>See [Processing responses](#processing-responses) to painlessly process responses. |   



### Uploading Files

Upload a file

`(upload-file file uri & options)`

Upload a file given an uri and options.

Sets an implicit "Content-Type" header that is derived from the files's mimetype.


#### Parameter «file»

The file to upload


#### Parameter «uri»

The request URI


#### Parameter options

| Option             | Description |
| :---               | :---        |
| :headers           | A map of request headers. Headers can be single- or multi-value (comma separated):<br>{"X-Header-1" "value1"<br>"X-Header-2" "value1, value2, value3"} |
| :body              | An optional body to send with the request. The body may be of type *string*, *bytebuf*, or `:java.io.InputStream` |
| :conn-timeout      | An optional connection timeout in milliseconds |
| :read-timeout      | An optional read timeout in milliseconds |
| :follow-redirects  | Sets whether HTTP redirects (requests with response code 3xx) should be automatically followed. |
| :hostname-verifier | Sets the hostname verifier. An object of type `:javax.net.ssl.HostnameVerifier`.<br> Use only for HTTPS requests |
| :ssl-socket-factory | Sets the SSL socket factory. An object of type `:javax.net.ssl.SSLSocketFactory`.<br> Use only for HTTPS requests |
| :use-caches         | A boolean indicating whether or not to allow caching. Defaults to false |
| :user-agent         | User agent. Defaults to "Venice HTTP client (legacy)" |
| :debug              | Debug true/false. Defaults to false.In debug mode prints the HTTP request and response data  |


#### Return value

The upload function returns a map with the HTTP response data:

| Field              | Description |
| :---               | :---        |
| :http-status       | The HTTP status (a long) |
| :content-type      | The content type. E.g.: "text/plain; charset=utf8" |
| :content-type-mimetype  | The content type's mimetype. E.g.: "text/plain" |
| :content-type-charset   | The content type's charset. E.g.: :utf-8 |
| :content-encoding  | The content transfer encoding (a keyword), if available else nil. E.g.: "gzip" |
| :content-length    | The content length (a long), if available else -1 |
| :headers           | A map of headers. key: header name, value: list of header values |
| :data-stream       | The response data input stream.<br>If the response content encoding is 'gzip', due to a request header "Accept-Encoding: gzip" wrap the data stream with a gzip input stream: `(io/wrap-is-with-gzip-input-stream (:data-stream response))` to uncompress the data.<br><br>See [Processing responses](#processing-responses) to painlessly process responses. |   





### Uploading Multipart Data

Upload multipart data

`(upload-multipart parts uri & options)`

Upload multipart data given its parts, an uri, and request options

Sets the "Content-Type" header to "multipart/form-data".


#### Parameter «parts»

The upload support string parts, file parts, and generic parts. Any number of parts can be uploaded.


```clojure
{ ;; a string part 
  "Part-1" "xxxxxxxxxxx"

  ;; a file part
  "Part-2" (io/file "/Users/juerg/Desktop/image.png")

  ;; a x-www-form-urlencoded (generic) part 
  "Part-3" { :mimetype  "application/x-www-form-urlencoded"
             :charset   :utf-8
             :data      "color=blue" }

  ;; a generic part
  ;; The charset of a generic part is only required for text based 
  ;; data. When passing binary data the charset can be left out.
  "Part-4" { :filename  "data.xml"
             :mimetype  "application/xml"
             :charset   :utf-8
             :data      "<user><name>foo</name></user>" }})
```

#### Parameter «uri»

The request URI


#### Parameter options

| Option             | Description |
| :---               | :---        |
| :headers           | A map of request headers. Headers can be single- or multi-value (comma separated):<br>{"X-Header-1" "value1"<br>"X-Header-2" "value1, value2, value3"} |
| :body              | An optional body to send with the request. The body may be of type *string*, *bytebuf*, or `:java.io.InputStream` |
| :conn-timeout      | An optional connection timeout in milliseconds |
| :read-timeout      | An optional read timeout in milliseconds |
| :follow-redirects  | Sets whether HTTP redirects (requests with response code 3xx) should be automatically followed. |
| :hostname-verifier | Sets the hostname verifier. An object of type `:javax.net.ssl.HostnameVerifier`.<br> Use only for HTTPS requests |
| :ssl-socket-factory | Sets the SSL socket factory. An object of type `:javax.net.ssl.SSLSocketFactory`.<br> Use only for HTTPS requests |
| :use-caches         | A boolean indicating whether or not to allow caching. Defaults to false |
| :user-agent         | User agent. Defaults to "Venice HTTP client (legacy)" |
| :debug              | Debug true/false. Defaults to false.In debug mode prints the HTTP request and response data  |


#### Return value

The upload function returns a map with the HTTP response data:

| Field              | Description |
| :---               | :---        |
| :http-status       | The HTTP status (a long) |
| :content-type      | The content type. E.g.: "text/plain; charset=utf8" |
| :content-type-mimetype  | The content type's mimetype. E.g.: "text/plain" |
| :content-type-charset   | The content type's charset. E.g.: :utf-8 |
| :content-encoding  | The content transfer encoding (a keyword), if available else nil. E.g.: "gzip" |
| :content-length    | The content length (a long), if available else -1 |
| :headers           | A map of headers. key: header name, value: list of header values |
| :data-stream       | The response data input stream.<br>If the response content encoding is 'gzip', due to a request header "Accept-Encoding: gzip" wrap the data stream with a gzip input stream: `(io/wrap-is-with-gzip-input-stream (:data-stream response))` to uncompress the data.<br><br>See [Processing responses](#processing-responses) to painlessly process responses. |   



### Processing responses

Slurps the response data from the response' input stream.

`(slurp-response response & options)`

Returns the data according to the mimetype and charset of the 'Content-Type' response header.

Handles a 'Content-Encoding' transparently. Supports the encodings 'gzip' and 'deflate'. Other encodings are rejected with an exception.

The functions returns the response data based on the response mimetype:

| Mimetype          | Description |
| :---              | :---        |
| application/xml   | Returns a string according to the content type charset |
| application/json  | Returns the JSON response according to the content type charset.<br>Depending on the option `:json-parse-mode` returns the JSON parsed to a Venice map, as a JSON pretty printed string, or as a raw JSON string |
| text/plain        | Returns a string according to the content type charset |
| text/html         | Returns a string according to the content type charset |
| text/xml          | Returns a string according to the content type charset |
| text/csv          | Returns a string according to the content type charset |
| text/css          | Returns a string according to the content type charset |
| text/json         | Returns the JSON response according to the content type charset.<br>Depending on the option `:json-parse-mode` returns the JSON parsed to a Venice map, as a JSON pretty printed string, or as a raw JSON string |
| text/event-stream | Throws an exception. An event stream can not be slurped. Use the function `process-server-side-events` instead! |
| *else*            | Returns the response data as a byte buffer |


#### Parameter «response»

A response returned from one of the HTTP send or upload functions. 


#### Parameter «options»

| Option             | Description |
| :---               | :---        |
| :json-parse-mode   | The option is used with JSON mimetypes.<br> `:data` - parse the response to a Venice data map<br>`:raw` - return the reponse as received<br>`:pretty-print` - return a pretty printed JSON string<br>Defaults to `:data` |
| :json-key-fn       | A single argument function that transforms JSON property names. This option is only available in `:data` parse mode. E.g.: `:json-key-fn keyword` |


### Processing server-side-events

Processes the server side events (SSE) sent from the server.

`(process-server-side-events response handler)`

Calls for every received SSE event the passed handler function.

Note: The response must be of the mimetype "text/event-stream" otherwise the processor throws an exception!

The event handler is a three argument function:
  
`(defn handler [type event event-count] ...)`

| Handler argument | Description |
| :---             | :---        |
| *type*           | the notification type: <br> `:opened` - streaming started <br> `:data` - streamed event<br> `:closed` - streaming closed by the server |
| *event*          | the streamed event, available only if the notification type is `:data`, else `nil` |
| *event-count*    | the streamed event count, starting with 1 and incremented with every event sent |

If the event handler returns the value `:stop` the processer stops
handling any further events and closes the data stream to signal
the server not to send any further events and close the server side
stream as well.

Server side events are passed as maps to the handler. E.g. :

```clojure
{ :id    "1"
  :event "score"
  :data  [ "GOAL Liverpool 1 - 1 Arsenal"
           "GOAL Manchester United 3 - 3 Manchester City" ] }
```

**Warning:**
  
When not used over HTTP/2, SSE suffers from a limitation to
the maximum number of open connections, which can be especially
painful when opening multiple tabs, as the limit is per browser and
is set to a very low number (6). The issue has been marked as
"Won't fix" in Chrome and Firefox. This limit is per browser + domain,
which means that you can open 6 SSE connections across all of the
tabs. 
  
When using HTTP/2, the maximum number of simultaneous HTTP streams
is negotiated between the server and the client (defaults to 100).

The Java 8 Http Client does not support HTTP/2!




## Examples

* [Sending Requests Examples](#sending-requests-examples)
* [Uploading Files Examples](#uploading-files-examples)
* [Uploading Multipart Data Examples](#uploading-multipart-data-examples)
* [Processing server-side-events Examples](#processing-server-side-events-examples)


### Sending Requests Examples

GET (get, JSON response converted to a pretty printed JSON string)

```clojure
(do
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])

  (let [response (hc/send :get 
                          "http://localhost:8080/employees" 
                          :headers { "Accept" "application/json, text/plain" }
                          :debug true)
        status   (:http-status response)]
    (println "Status:" status)
    (println (hc/slurp-response response :json-parse-mode :pretty-print))))
```

```json
[{
  "role": "secretary",
  "name": "susan",
  "id": "1000"
},{
  "role": "assistant",
  "name": "john",
  "id": "1001"
},{
  "role": "team-lead",
  "name": "mary",
  "id": "1002"
}]
```


GET (get, JSON response converted to Venice data)

```clojure
(do
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])

  (let [response (hc/send :get 
                          "http://localhost:8080/employees" 
                          :headers { "Accept" "application/json, text/plain" }
                          :debug true)
        status   (:http-status response)]
    (println "Status:" status)
    (prn (hc/slurp-response response :json-parse-mode :data :json-key-fn keyword))))
```

```clojure
({:name "mary"
  :role "team-lead"
  :id "1002"}
 {:name "hanna"
  :role "secretary2"
  :id "1003"}
 {:name "john"
  :role "clerk"
  :id "1001"})
```


POST (create)

```clojure
(do
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])
  
  (let [response (hc/send :post 
                          "http://localhost:8080/employees" 
                          :headers {"Accept"       "application/json, text/plain"
                                    "Content-Type" "application/json"}
                          :body (json/write-str { "name" "hanna", 
                                                  "role" "secretary" })
                          :debug true)
        status   (:http-status response)]
    (println "Status:" status)
    (println (hc/slurp-response response :json-parse-mode :pretty-print))))
```

```json
{
  "role": "secretary",
  "name": "hanna",
  "id": "1003"
}
```


PUT (update)

```clojure
(do
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])
  
  (let [response (hc/send :put 
                          "http://localhost:8080/employees/1001" 
                          :headers {"Accept"       "application/json, text/plain"
                                    "Content-Type" "application/json"}
                          :body (json/write-str { "id"   "1001", 
                                                  "name" "john", 
                                                  "role" "clerk" })
                          :debug true)
        status   (:http-status response)]
    (println "Status:" status)
    (println (hc/slurp-response response :json-parse-mode :pretty-print))))
```

```json
{
  "role": "clerk",
  "name": "john",
  "id": "1001"
}
```


DELETE (delete)

```clojure
(do
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])
  
  (let [response (hc/send :delete 
                          "http://localhost:8080/employees/1000" 
                          :headers { "Accept" "text/plain" }
                          :debug true)
        status   (:http-status response)]
    (println "Status:" status)
    (println (hc/slurp-response response))))
```

```
Employee with the id 1000 deleted!
```

GET over SSL

```clojure
(do
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])
  (load-module :java ['java :as 'j])

  (import :com.github.jlangch.venice.util.ssl.CustomHostnameVerifier)
  (import :com.github.jlangch.venice.util.ssl.Server_X509TrustManager)
  (import :com.github.jlangch.venice.util.ssl.TrustAll_X509TrustManager)
  (import :com.github.jlangch.venice.util.ssl.SSLSocketFactory)
  (import :java.security.cert.X509Certificate)

  (defn verify-host [hostname]
      (case hostname
        "localhost"  true
        "foo.org"    true
        false))

  (defn check-trust-server  [certs auth-type]
    (doseq [c certs] (. c :checkValidity))
    (any? #(= "Foo" (. (. % :getIssuerDN) :getName)) certs))


  (let [trust-manager-all     (. :TrustAll_X509TrustManager :new)
        trust-manager-server  (. :Server_X509TrustManager :new (j/as-bipredicate check-trust-server))
        hostname-verifier     (. :CustomHostnameVerifier :new verify-host)
        response (hc/send :get 
                          "https://localhost:8080/employees" 
                          :headers { "Accept" "application/json, text/plain" }
                          :hostname-verifier  hostname-verifier
                          :ssl-socket-factory (. :SSLSocketFactory trust-manager-all)
                          :debug true)
        status   (:http-status response)]
    (println "Status:" status)
    (println (hc/slurp-response response))))
```

OAuth blueprint

```clojure
(do
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])

  (defn get-access-token [api-key api-key-secret]
    (let [encoded-secret (-> (str api-key ":" api-key-secret)
                              (bytebuf-from-string :utf-8)
                              (str/encode-base64))
          response (hc/send :post 
                            "https://.../oauth2/token" 
                            :headers { "Accept" "application/json, text/plain"
                                       "Authorization" (str "Basic " encoded-secret)
                                       "Content-Type" "application/x-www-form-urlencoded" }
                            :body "grant_type=client_credentials")
          status   (:http-status response)
          mimetype (:content-type-mimetype response)
          charset  (:content-type-charset response)]
      (if (and (= 200 status) (= "application/json" mimetype))
        (as-> (:data-stream response) v
              (hc/slurp-json v charset)
              (get v "access_token"))
        (throw (ex VncException "Failed to get OAuth access token")))))
  
  (defn list-member [access-token list-id]
    (let [response (hc/send :get 
                            (str "https://.../1.1/lists/members.json?list_id=" list-id) 
                            :headers { "Accept" "application/json, text/plain" 
                                        "Authorization" (str "Bearer "  accessToken)})
          status   (:http-status response)]
      (println "Status:" status)
      (println (hc/slurp-response response :json-parse-mode :pretty-print)))))
```



### Uploading Files Examples

```clojure
(do
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])
  
  (let [response (hc/upload-file
                      (io/file "/Users/foo/image.png") 
                      "http://localhost:8080/upload" 
                      :headers { "Accept" "text/plain" }
                      :debug true)
        status   (:http-status response)]
    (println "Status:" status)))
```


### Uploading Multipart Data Examples

```clojure
(do
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])
  
  (let [response (hc/upload-multipart
                      { "image1" (io/file "/Users/foo/image1.png") 
                        "image2" (io/file "/Users/foo/image2.png") }                                
                      "http://localhost:8080/upload" 
                      :headers { "Accept" "text/plain" }
                      :debug true)
        status   (:http-status response)]
    (println "Status:" status)))
```


### Processing server-side-events Examples

```clojure       
(do
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])

  (let [response (hc/send :get 
                          "http://localhost:8080/events" 
                          :headers { "Accept"         "text/event-stream" 
                                     "Cache-Control"  "no-cache"
                                     "Connection"     "keep-alive"}
                          :conn-timeout 0
                          :read-timeout 0
                          :debug true)]
        (println "Status:" (:http-status response))

        ;; process the first 10 events and close the stream
        (hc/process-server-side-events 
          response
          (fn [type event event-count]
            (case type
              :opened (do (println "\\nStreaming started")
                          :ok)
              :data   (do (println "Event: " (pr-str event))
                          ;; only process 10 events
                          (if (< event-count 10) :ok :stop))
              :closed (do (println "Streaming closed")
                          :ok))))))
```

```
Status: 200

Streaming started
Event:  {:data ["Counter 1001"] :event "demo" :id "1001"}
Event:  {:data ["Counter 1002"] :event "demo" :id "1002"}
Event:  {:data ["Counter 1003"] :event "demo" :id "1003"}
Event:  {:data ["Counter 1004"] :event "demo" :id "1004"}
Event:  {:data ["Counter 1005"] :event "demo" :id "1005"}
Event:  {:data ["Counter 1006"] :event "demo" :id "1006"}
Event:  {:data ["Counter 1007"] :event "demo" :id "1007"}
Event:  {:data ["Counter 1008"] :event "demo" :id "1008"}
Event:  {:data ["Counter 1009"] :event "demo" :id "1009"}
Event:  {:data ["Counter 1010"] :event "demo" :id "1010"}
Streaming closed
```


## OpenAI Examples

### Chat Completion

```clojure
do
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])
  
  ;; get the OpenAI API Key from the environemnt var "OPENAI_API_KEY"
  (defn- openai-api-key [] (system-env "OPENAI_API_KEY"))

  (let [body      { :model "gpt-3.5-turbo"
                  :messages [ { :role "user"
                                :content """
                                        Count to 10, with a comma between each number and no \
                                        newlines. E.g., 1, 2, 3, ...
                                        """ } ] } 
      response  (hc/send :post 
                  "https://api.openai.com/v1/chat/completions"
                  :headers { "Content-Type" "application/json"
                            "Authorization" "Bearer ~(openai-api-key)"}
                  :body (json/write-str body )
                  :debug false)]
  (println "Status:" (:http-status response))
  (println (hc/slurp-response response :json-parse-mode :pretty-print))))
```

Returns the response:

```
{
  "created": 1713302066,
  "usage": {
    "completion_tokens": 28,
    "prompt_tokens": 37,
    "total_tokens": 65
  },
  "model": "gpt-3.5-turbo-0125",
  "id": "chatcmpl-9EkQE6O4khw25Fi8MLUvRsu36lfrn",
  "choices": [{
    "finish_reason": "stop",
    "index": 0,
    "message": {
      "role": "assistant",
      "content": "1, 2, 3, 4, 5, 6, 7, 8, 9, 10"
    },
    "logprobs": null
  }],
  "system_fingerprint": "fp_c2295e73ad",
  "object": "chat.completion"
}
```
