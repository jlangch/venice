# Embeddings

[OpenAI Files](https://platform.openai.com/docs/api-reference/embeddings)

## Create Embedding

Creates an embedding vector representing an input text.

### Sending Requests

`(embedding-create input & options)`

#### Parameter «input»

The input text to embed

```
"The quick brown fox jumped over the lazy dog."
```


#### Parameter «options»

| Option            | Description |
| :---              | :---        |
| :uri              | An OpenAI embeddings URI. E.g.: "https://api.openai.com/v1/embeddings". <br>Defaults  to "https://api.openai.com/v1/embeddings" |
| :model            | An OpenAI model. E.g.: "text-embedding-ada-002". Defaults to "text-embedding-ada-002". <br>The model can also be passed as a keyword. E.g.: `:text-embedding-ada-002`, `:text-embedding-3-small`, `:text-embedding-3-large`  |
| :openai-api-key   | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :embed-opts       | An optional map of OpenAI embedding request options Map keys can be keywords or strings. <br>E.g. `{ :encoding_format :float }`. <br>E.g. `{ :dimensions 1536 }. <br>See: [OpenAI Request Options](https://platform.openai.com/docs/api-reference/embeddings/create) |
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

  (let [response  (openai/embedding-create 
                      "Happy Christmas ..."
                      :embed-opts { :model "text-embedding-ada-002" 
                                    :encoding :float} )]
    (openai/assert-response-http-ok response)
    (let [data       (:data response)
          embed-vec  (:embedding (first (:data data)))]
      (prn (:data response)))))
```

Returns:

```
{ :usage {:prompt_tokens 3 :total_tokens 3} 
  :model "text-embedding-ada-002" 
  :data ({ :index 0
           :embedding (-0.0020923046 0.0035093727 -0.020901365  ....  0.013740139 -0.019476553) 
           :object "embedding"}) 
  :object "list"}
```


