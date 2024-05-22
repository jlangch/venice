# Files

[OpenAI Files](https://platform.openai.com/docs/api-reference/files)


* [File Upload](#file-upload)
* [File List](#file-list)
* [File Retrieve](#file-retrieve)
* [File Delete](#file-delete)
* [File Retrieve Content](#file-retrieve-content)



## File Upload

Upload a file that can be used across various endpoints. Individual files can be up to 512 MB, and the size of all files uploaded by one organization can be up to 100 GB.

### Sending Requests

`(files-upload file-data file-name file-mimetype purpose & options)`

#### Parameters «file»

«file-data»      The file data, a `bytebuf`
«file-name»      The file name. E.g.: "product-indo-pdf"
«file-mimetype»  The file mimetype. E.g.: "application/pdf"

          
#### Parameter «purpose»

Purpose is one of "assistants", "vision", "batch", "fine-tune"


#### Parameter «options»

| Option            | Description |
| :---              | :---        |
| :openai-api-key   | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :debug            | An optional debug flag (true/false). Defaults  to false.<br>In debug mode prints the HTTP request and response data |
 
 
#### Response value

*The return value is a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |


### Example

```clojure
(do
  (load-module :openai)

  (let [file      "https://raw.githubusercontent.com/jlangch/venice/master/doc/pdfs/fonts-example.pdf"
        response  (openai/files-upload  (io/download file :binary true)
                                        "example.pdf"
                                        "application/pdf" 
                                        "assistants")]
    (openai/assert-response-http-ok response)
    (prn (:data response))))
```

Result:

```clojure
{ :filename "example.pdf" 
  :created_at 1716390396 
  :bytes 35213 
  :status "processed" 
  :status_details nil
  :id "file-qvrjsmEk7yPElJzMOegsf0xf" 
  :purpose "assistants" 
  :object "file"
}
```


## File List

### Sending Requests

`(files-list purpose & options)`

#### Parameter «purpose»

Purpose is one of "assistants", "vision", "batch", "fine-tune"


#### Parameter «options»

| Option            | Description |
| :---              | :---        |
| :openai-api-key   | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :debug            | An optional debug flag (true/false). Defaults  to false.<br>In debug mode prints the HTTP request and response data |
 
 
#### Response value

*The return value is a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |


### Example 1

```clojure
(do
  (load-module :openai)

  (let [response  (openai/files-list nil)]
    (openai/assert-response-http-ok response)
    (prn (:data response))))
```

Result:

```clojure
{ :data ( 
    { :filename "example.pdf" 
      :created_at 1716390396 
      :bytes 35213 
      :status "processed" 
      :status_details nil 
      :id "file-qvrjsmEk7yPElJzMOegsf0xf" 
      :purpose "assistants" 
      :object "file" } 
    { :filename "example.pdf" 
      :created_at 1716390348 
      :bytes 35213 
      :status "processed" 
      :status_details nil 
      :id "file-fGjBnoSJf19KeeUiL0SpDRTC" 
      :purpose "assistants" 
      :object "file"}
  ) 
  :has_more false 
  :object "list" }
```

### Example 2

```clojure
(do
  (load-module :openai)

  (let [response  (openai/files-list "assistants")]
    (openai/assert-response-http-ok response)
    (prn (:data response))))
```



## File Retrieve

Returns information about a specific file.

### Sending Requests

`(files-retrieve file-id & options)`

#### Parameters «file-id»

The ID of the file to use for this request.


#### Parameter «options»

| Option            | Description |
| :---              | :---        |
| :openai-api-key   | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :debug            | An optional debug flag (true/false). Defaults  to false.<br>In debug mode prints the HTTP request and response data |
 
 
#### Response value

*The return value is a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |


### Example

```clojure
(do
  (load-module :openai)

  (let [response  (openai/files-retrieve "file-uo1oroO3MMRFwRAypupJX0pO")]
    (openai/assert-response-http-ok response)
    (prn (:data response))))
```

Result:

```clojure
{ :filename "example.pdf" 
  :created_at 1716390396 
  :bytes 35213 
  :status "processed" 
  :status_details nil 
  :id "file-qvrjsmEk7yPElJzMOegsf0xf" 
  :purpose "assistants" 
  :object "file" }
```



## File Delete

Delete a file.

### Sending Requests

`(files-delete file-id & options)`

#### Parameters «file-id»

The ID of the file to use for this request.


#### Parameter «options»

| Option            | Description |
| :---              | :---        |
| :openai-api-key   | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :debug            | An optional debug flag (true/false). Defaults  to false.<br>In debug mode prints the HTTP request and response data |
 
 
#### Response value

*The return value is a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |


### Example

```clojure
(do
  (load-module :openai)

  (let [response  (openai/files-delete "file-uo1oroO3MMRFwRAypupJX0pO")]
    (openai/assert-response-http-ok response)
    (prn (:data response))))
```

Result:

```clojure
{ :deleted true 
  :id "file-qvrjsmEk7yPElJzMOegsf0xf"
  :object "file" }
```


## File Retrieve Content

Delete a file.

### Sending Requests

`(files-retrieve-content file-id & options)`

#### Parameters «file-id»

The ID of the file to use for this request.


#### Parameter «options»

| Option            | Description |
| :---              | :---        |
| :openai-api-key   | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :debug            | An optional debug flag (true/false). Defaults  to false.<br>In debug mode prints the HTTP request and response data |
 
 
#### Response value

*The return value is a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |


### Example

```clojure
(do
  (load-module :openai)

  (let [response  (openai/files-retrieve-content "file-uo1oroO3MMRFwRAypupJX0pO")]
    (openai/assert-response-http-ok response)
    (let [data       (:data response)
          file       "./example.pdf"]
      (io/spit file data)
      (println "Saved to:" file))))
```

Response if the file does not exist:

```
Exception in thread "main" VncException: HTTP response is not HTTP_OK!

HTTP Status: 404
Mimetype:    application/json

Response:
{
  "error": {
    "code": null,
    "param": "id",
    "message": "No such File object: file-qvrjsmEk7yPElJzMOegsf0xf",
    "type": "invalid_request_error"
  }
}

[Callstack]
    at: ex (openai: line 2006, col 40)
    at: openai/assert-response-http-ok (user: line 5, col 6)
```

Response if the file download is rejected:

```
Exception in thread "main" VncException: HTTP response is not HTTP_OK!

HTTP Status: 400
Mimetype:    application/json

Response:
{
  "error": {
    "code": null,
    "param": null,
    "message": "Not allowed to download files of purpose: assistants",
    "type": "invalid_request_error"
  }
}

[Callstack]
    at: ex (openai: line 2006, col 40)
    at: openai/assert-response-http-ok (user: line 5, col 6)
```