# HTTP Client (Java 8+)

* [Overview](#overview)
* [Api](#api)
* [Examples](#examples)



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

### Sending Requests

`(send method uri & options)`

Send a request given a method, an uri, and request options.


#### Parameter method

The request method:  `:get`, `:post`,`:put`,`:delete`, ...


#### Parameter uri

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
| :data-stream       | The response data input stream.<br>If the response content encoding is 'gzip', due to a request header "Accept-Encoding: gzip" wrap the data stream with a gzip input stream: `(io/wrap-is-with-gzip-input-stream (:data-stream response))` to uncompress the data. |   



### Uploading Files

Upload a file

`(upload-file file uri & options)`

Upload a file given an uri and options.


#### Parameter file

The file to upload


#### Parameter uri

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
| :data-stream       | The response data input stream.<br>If the response content encoding is 'gzip', due to a request header "Accept-Encoding: gzip" wrap the data stream with a gzip input stream: `(io/wrap-is-with-gzip-input-stream (:data-stream response))` to uncompress the data. |   





### Uploading Multipart Data

Upload multipart data

`(upload-multipart parts uri & options)`

Upload multipart data given its parts, an uri, and request options



#### Parameter parts

The upload support string parts, file parts, and generic parts. Any number of parts can be uploaded.


```
{ ;; a string part 
  ;Part-1" "xxxxxxxxxxx"

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

#### Parameter uri

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
| :data-stream       | The response data input stream.<br>If the response content encoding is 'gzip', due to a request header "Accept-Encoding: gzip" wrap the data stream with a gzip input stream: `(io/wrap-is-with-gzip-input-stream (:data-stream response))` to uncompress the data. |   



### Processing responses

Slurps the response data from the response' input stream.

`(slurp-response response & options)`

Returns the data according to the mimetype and charset of the 'Content-Type' response header.

Handles a 'Content-Encoding' transparently. Supports the encodings 'gzip' and 'deflate'. Other encodings are rejected with an exception.

The functions returns the response data based on the response mimetype:

| Mimetype          | Description |
| :---              | :---        |
| application/xml   | Returns a string according to the content type charset |
| application/json  | Returns the parsed JSON as a map according to the content type charset |
| text/plain        | Returns a string according to the content type charset |
| text/html         | Returns a string according to the content type charset |
| text/xml          | Returns a string according to the content type charset |
| text/csv          | Returns a string according to the content type charset |
| text/css          | Returns a string according to the content type charset |
| text/json         | Returns the parsed JSON as a map according to the content type charset |
| text/event-stream | Throws an exception. An event stream can not be slurped. Use the function `process-server-side-events` instead! |
| else              | Returns a byte buffer |


#### Parameter response

A response returned from one of the HTTP send or upload functions. 


#### Parameter options

| Option             | Description |
| :---               | :---        |
| :json-parse-mode   | The option is used with JSON mimetypes.
                        * `:data` - parse the response to a Venice data map
                        * `:raw` - return the reponse as received
                        * `:pretty-print` - return a pretty print JSON string
                       Defaults to `:data` |
| :json-key-fn       | A single argument function that transforms JSON property names. This option is only available in `:data` parse mode. E.g.: `:json-key-fn keyword` |


### Processing server-side-events





## Examples

* [Sending Requests Examples](#sending-requests-examples)
* [Uploading Files Examples](#uploading-files-examples)
* [Uploading Multipart Data Examples](#uploading-multipart-data-examples)
* [Processing server-side-events Examples](#processing-server-side-events-examples)



### Sending Requests Examples

### Uploading Files Examples

### Uploading Multipart Data Examples

### Processing server-side-events Examples
