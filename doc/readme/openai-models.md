# Modules

[OpenAI Modules](https://platform.openai.com/docs/api-reference/modules)


* [Model List](#model-list)
* [Model Retrieve](#model-retrieve)
* [Model Delete](#model-delete)



## Model List

### Sending Requests

`(models-list options*)`


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

  (let [response  (openai/models-list)]
    (openai/assert-response-http-ok response)
    (prn (:data response))))
```

Result:

```clojure
{ :data (
    { :created 1698785189 
      :owned_by "system" 
      :id "dall-e-3" 
      :object "model" } 
      
    { :created 1677532384 
      :owned_by "openai-internal" 
      :id "whisper-1" 
      :object "model" }
          
    { :created 1687882411 
      :owned_by "openai" 
      :id "gpt-4" 
      :object "model" }
      
    ...
     
    {:created 1715367049
     :owned_by "system" 
     :id "gpt-4o" 
     :object "model" } 
  ) 
  
  :object "list"}
```


## Model Retrieve


## Model Delete

