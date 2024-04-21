# Venice meets LLMs

* [OpenAI Client](#openai-client)
* [LangChain4J](#langchain4j)
* [Qdrant Vector DB](#qdrant-vector-db)



## OpenAI Client

*The OpenAI client is in incubation status. The API might undergo changes.*

The OpenAI client runs out-of-the-box without any dependencies on 3rd party libraries.

* [Chat Completion](#chat-completion)
* [Chat Completion Streaming](#chat-completion-streaming)
* Functions
* Audio
* Images
* Embeddings
* Assistants


### Chat Completion

Runs a chat completion.


#### Sending Requests

`(chat-completion prompt & options)`

Send a chat completion request given a prompt and options.

The OpenAI api key can be provided in an environment variable "OPENAI_API_KEY" or
explicitly passed as an option `:openai-api-key "sk-xxxxxxxxxxxxx"`.

To run the request asynchronously just wrap it in a `future` and
deref it, when the result is required.


#### Parameter «options»

| Option             | Description |
| :---               | :---        |
| :uri               | An OpenAI chat completion URI. E.g.: <br>"https://api.openai.com/v1/chat/completions".<br>Defaults to "https://api.openai.com/v1/chat/completions" |
| :model             | An OpenAI model. E.g.: "gpt-3.5-turbo". Defaults to "gpt-3.5-turbo" |
| :prompt-opts       | An optional map of OpenAI chat request prompt options<br>E.g. {:temperature 0.2} <br>See: [OpenAI Request Options](https://platform.openai.com/docs/api-reference/chat/create) |
| :openai-api-key    | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :debug             | An optional debug flag (true/false). Defaults to false. <br>In debug mode prints the HTTP request and response data |
 
#### Return value

*The return value is a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :message   | The final chat completion message if the OpenAI  server returned the HTTP status `HTTP_OK`, else `nil` |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |

### Using prompt roles:

| Role       | Description |
| :---       | :---        |
| *system*    | Allows you to specify the way the model answers questions. <br>Classic example: "You are a helpful assistant." |
| *user*      | Equivalent to the queries made by the user. |
| *assistant* | Assistent roles are the model’s responses, based on the user messages |
          
See:
 * [OpenAI Chat Completions API](https://platform.openai.com/docs/guides/text-generation/chat-completions-api)
 * [OpenAI API Reference](https://platform.openai.com/docs/api-reference/chat/create)
 * [OpenAI API Examples](https://platform.openai.com/examples)
 * [OpenAI API Examples Prompts](https://platform.openai.com/examples?category=code)


#### Example 1

```clojure
;; print the full OpenAI response message
(do
  (load-module :openai)

  (let [prompt    (str "Count to 10, with a comma between each number "
                        "and no newlines. E.g., 1, 2, 3, ...")
        response  (openai/chat-completion prompt)]
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Message:" (openai/pretty-print-json (:data response)))
      (println "Error:"   (:data response)))))
```

```
Status:   200
Mimetype: application/json
Message: {
  "created": 1713455434,
  "usage": {
    "completion_tokens": 28,
    "prompt_tokens": 36,
    "total_tokens": 64
  },
  "model": "gpt-3.5-turbo-0125",
  "id": "chatcmpl-9FOJu9tbkFRFKXZLjjIlQ6jnGDMr2",
  "choices": [{
    "finish_reason": "stop",
    "index": 0,
    "message": {
      "role": "assistant",
      "content": "1, 2, 3, 4, 5, 6, 7, 8, 9, 10"
    },
    "logprobs": null
  }],
  "system_fingerprint": "fp_d9767fc5b9",
  "object": "chat.completion"
}
```

#### Example 2

```clojure
;; print only the OpenAI response message content
(do
  (load-module :openai)

  (let [prompt    (str "Count to 10, with a comma between each number "
                        "and no newlines. E.g., 1, 2, 3, ...")
        response  (openai/chat-completion prompt)]
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Message:" (-> (:data response)
                              (openai/extract-response-message-content)
                              (pr-str)))
      (println "Error:"   (:data response)))))
```

```
Status:   200
Mimetype: application/json
Message: "1, 2, 3, 4, 5, 6, 7, 8, 9, 10"
```

#### Example 3

```clojure
;; Dealing with prompt options
(do
  (load-module :openai)

  (let [prompt      [ { :role     "system"
                        :content  "You will be provided with statements, and your task is to convert them to standard English." }
                      { :role     "user"
                        :content  "She no went to the market." } ]
        prompt-opts { :temperature 0.7
                      :max_tokens 64
                      :top_p 1 }
        response    (openai/chat-completion prompt 
                                            :model "gpt-3.5-turbo" 
                                            :prompt-opts prompt-opts)]
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Message:" (-> (:data response)
                              (openai/extract-response-message-content)
                              (openai/pretty-print-json)))
      (println "Error:"   (:data response)))))
```

```
Status:   200
Mimetype: application/json
Message: She did not go to the market.
```

### Chat Completion Streaming

Runs a chat completion in streaming mode. Upon initiating the request the OpenAI 
server send asynchronously *Server-Side-Events* back to the client. These events 
are then handled by a handler function.


#### Sending Requests

`(chat-completion-streaming prompt handler & options)`

The OpenAI api key can be provided in an environment variable "OPENAI_API_KEY" or
explicitly passed as an option `:openai-api-key "sk-xxxxxxxxxxxxx"`.



#### Parameter «options»

| Option             | Description |
| :---               | :---        |
| :uri               | An OpenAI chat completion URI. E.g.: <br>"https://api.openai.com/v1/chat/completions".<br>Defaults to "https://api.openai.com/v1/chat/completions" |
| :model             | An OpenAI model. E.g.: "gpt-3.5-turbo". Defaults to "gpt-3.5-turbo" |
| :sync              | if *true* runs the request syncronously and waits until the full message response is available.<br>if *false* runs the request asyncronously and returns immediately with the response :data field holding a `future` that can be deref'd (with an optional timeout) to get the full message.<br>Defaults to *true* |
| :prompt-opts       | An optional map of OpenAI chat request prompt options<br>E.g. {:temperature 0.2} <br>See: [OpenAI Request Options](https://platform.openai.com/docs/api-reference/chat/create) |
| :openai-api-key    | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :debug             | An optional debug flag (true/false). Defaults to false. <br>In debug mode prints the HTTP request and response data |
 
#### Return value

*The return value is a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :message   | The final chat completion message if the OpenAI  server returned the HTTP status `HTTP_OK`, else `nil` |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |

*Note: The streaming mode does not support functions!*

See:
 * [OpenAI Chat Completions API](https://platform.openai.com/docs/guides/text-generation/chat-completions-api)
 * [OpenAI API Reference](https://platform.openai.com/docs/api-reference/chat/create)
 * [OpenAI API Examples](https://platform.openai.com/examples)
 * [OpenAI API Examples Prompts](https://platform.openai.com/examples?category=code)


#### Example 1

```clojure
;; synchronous
;; prints the arriving events asynchronously, the response is only
;; returned when the final message is available or the request is bad
(do
  (load-module :openai)

  (let [prompt    (str "Count to 5, with a comma between each number "
                        "and no newlines. E.g., 1, 2, 3, ...")
        handler   (fn [delta accumulated status]
                    (case status
                      :opened  (println "Started...")
                      :data    (println "Delta:" (pr-str delta))
                      :done    (println "Completed.")))
        response  (openai/chat-completion-streaming prompt handler)]
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Message:" (pr-str (:data response)))
      (println "Error:"   (:data response)))))
```

```
Started...
Delta: ""
Delta: "1"
Delta: ","
Delta: " "
Delta: "2"
Delta: ","
Delta: " "
Delta: "3"
Delta: ","
Delta: " "
Delta: "4"
Delta: ","
Delta: " "
Delta: "5"
Delta: nil
Completed.
Status:   200
Mimetype: text/event-stream
Message: "1, 2, 3, 4, 5"
``

#### Example 2

```clojure
;; asynchronous
;; prints the arriving events asynchronously, returns the response
;; immediately with the data `(:data response)` as a future that can 
;; be deref'd to get the final message.
(do
  (load-module :openai)

  (let [prompt    (str "Count to 5, with a comma between each number "
                        "and no newlines. E.g., 1, 2, 3, ...")
        handler   (fn [delta accumulated status]
                    (case status
                      :opened  (println "Started...")
                      :data    (println "Delta:" (pr-str delta))
                      :done    (println "Completed.")))
        response  (openai/chat-completion-streaming prompt handler :sync false)]
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Message:" (pr-str @(:data response)))
      (println "Error:"   (:data response)))))
```

```
Status:   200
Mimetype: text/event-stream
Started...
Delta: ""
Delta: "1"
Delta: ","
Delta: " "
Delta: "2"
Delta: ","
Delta: " "
Delta: "3"
Delta: ","
Delta: " "
Delta: "4"
Delta: ","
Delta: " "
Delta: "5"
Delta: nil
Completed.
Message: "1, 2, 3, 4, 5"
```


## LangChain4J]


*coming soon...*



## Qdrant Vector DB

Qdrant is a Vector Database and Vector Search Engine that empowers
LLM applications with Retrieval Augmented Generation (RAG) to access 
data outside the LLM data world.


### Start Qdrant

Parameters:

| Parameter        | Description |
| :---             | :---        |
| cname            | A unique container name |
| version          | The Qdrant version to use. E.g.: 1.8.3 |
| mapped-rest-port | The published (mapped) Qdrant REST port on the  host. Defaults to 6333 |
| mapped-grpc-port | The published (mapped) Qdrant GRPC port on the host. Defaults to 6334 |
| storage-dir      | Directory where Qdrant persists all the data. |
| config-file      | An optional custom configuration yaml file |
| log              | A log function, may be *nil*. E.g: `(fn [s] (println "Qdrant:" s))`|


```clojure
(do
  (load-module :cargo-qdrant ['cargo-qdrant :as 'qdrant])
   
  ;; Run a Qdrant container labeled as "qdrant"
  (qdrant/start "qdrant" "1.8.3" "./qdrant-storage"))
```


### Stop Qdrant

```clojure
(do
  (load-module :cargo-qdrant ['cargo-qdrant :as 'qdrant])
   
  ;; Stop the Qdrant container labeled as "qdrant"
  (qdrant/stop "qdrant"))
```

