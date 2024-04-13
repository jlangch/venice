# HTTP Client (Java8+)


## Introduction

A HTTP client based on the Java *HttpUrlConnection*. The client runs out-of-the-box
and does not require any 3rd party libraries.

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

Send a request given a request method, an uri and options.

*Request Options:*

| Option             |Description |
| :---               | :---       |
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

*Returns a map with the response fields:*

| Option             |Description |
| :---               | :---       |
| :http-status       | The HTTP status (a long) |
| :content-type      | The content type |
| :content-type-mimetype  | The content type's mimetype. E.g.: "text/plain" |
| :content-type-charset   | The content type's charset. E.g.: :utf-8 |
| :content-encoding  | The content transfer encoding (a keyword), if available else nil. E.g.: "gzip" |
| :content-length    | The content length (a long), if available else -1 |
| :headers           | A map of headers. key: header name, value: list of header values |
| :data-stream       | The response data input stream.<br>If the response content encoding is 'gzip', due to a request header "Accept-Encoding: gzip" wrap the data stream with a gzip input stream: `(io/wrap-is-with-gzip-input-stream (:data-stream response))` to uncompress the data. |   


### Uploading Files

### Uploading Multipart Data

### Processing responses

### Processing server-side-events



## Examples
