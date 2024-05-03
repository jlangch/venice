# Venice meets LLMs

* [OpenAI Client](#openai-client)
* [LangChain4J](langchain4j.md)
* [Configuring OpenAI API keys](#configuring-openai-api-keys)



## OpenAI Client

*The OpenAI client is in incubation status. The API might undergo changes.*

The OpenAI client runs out-of-the-box without any dependencies on 3rd party libraries.

* [Chat Completion](#chat-completion)
* [Chat Completion Streaming](#chat-completion-streaming)
* [OpenAI Functions Cookbook](openai-functions.md)
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


#### Parameter «prompt»

A prompt is either a simple string like

```
"Who won the world series in 2020?"
```

or a list of prompt messages

```
[ {"role": "system", "content": "You are a helpful assistant."},
  {"role": "user", "content": "Who won the world series in 2020?"},
  {"role": "assistant", "content": "The Los Angeles Dodgers won the World Series in 2020."},
  {"role": "user", "content": "Where was it played?"} ]
```

Using prompt roles:

| Role       | Description |
| :---       | :---        |
| *system*    | Allows you to specify the way the model answers questions. <br>Classic example: "You are a helpful assistant." |
| *user*      | Equivalent to the queries made by the user. |
| *assistant* | Assistent roles are the model’s responses, based on the user messages |


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

          
See:
 * [OpenAI Chat Completions API](https://platform.openai.com/docs/guides/text-generation/chat-completions-api)
 * [OpenAI API Reference](https://platform.openai.com/docs/api-reference/chat/create)
 * [OpenAI API Messages](https://platform.openai.com/docs/api-reference/chat/create#chat-create-messages)
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

#### Example SQL

The database schema used in this SQL example is taken from the Chinook database. 
See [Venice Chinook](database.md#chinook-dataset-overview)

The schema required for the prompt is generated by

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
    (->> (map (fn [[t c]] (str "Table: " t "\nColumns: " (str/join ", " c)))
              (jdbc/tables-with-columns conn)) 
         (str/join "\n"))))
```


```clojure
;; Dealing with SQL related prompt
(do
  (load-module :openai)

  (let [prompt      [ { :role     "system"
                        :content  "You are a database architect." }
                      { :role     "assistant"
                        :content  """
                                  Generate SQL queries to answer user questions using a database schema 
                                  that will be provided to you.
                                  """ }
                      { :role     "assistant"
                        :content  """
                                  Create a fully formed SQL query to answer questions based on the 
                                  database schema: 
                                  
                                  Table: genre
                                  Columns: genre_id, name
                                  Table: invoice_line
                                  Columns: invoice_line_id, invoice_id, track_id, unit_price, quantity
                                  Table: artist
                                  Columns: artist_id, name
                                  Table: track
                                  Columns: track_id, name, album_id, media_type_id, genre_id, composer, milliseconds, bytes, unit_price
                                  Table: invoice
                                  Columns: invoice_id, customer_id, invoice_date, billing_address, billing_city, billing_state, billing_country, billing_postal_code, total
                                  Table: employee
                                  Columns: employee_id, last_name, first_name, title, reports_to, birth_date, hire_date, address, city, state, country, postal_code, phone, fax, email
                                  Table: album
                                  Columns: album_id, title, artist_id
                                  Table: playlist
                                  Columns: playlist_id, name
                                  Table: media_type
                                  Columns: media_type_id, name
                                  Table: playlist_track
                                  Columns: playlist_id, track_id
                                  Table: customer
                                  Columns: customer_id, first_name, last_name, company, address, city, state, country, postal_code, phone, fax, email, support_rep_id
                                  
                                  The query should be returned in plain text formatted SQL, not in JSON.
                                  """ }
                      { :role     "user"
                        :content  "Hi, who are the top 5 artists by number of tracks?" } ]
        prompt-opts { :temperature 0.1 }
        response    (openai/chat-completion prompt 
                                            :model "gpt-4" 
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
Message: Sure, here is the SQL query to get the top 5 artists by number of tracks:
```

```sql
SELECT a.name AS artist_name, COUNT(t.track_id) AS number_of_tracks
FROM artist a
JOIN album al ON a.artist_id = al.artist_id
JOIN track t ON al.album_id = t.album_id
GROUP BY a.name
ORDER BY number_of_tracks DESC
LIMIT 5;
```

```
This query first joins the artist, album, and track tables together. It then groups the 
results by artist name and counts the number of tracks associated with each artist. The 
results are ordered in descending order by the number of tracks, and finally, the `LIMIT 5` 
clause returns only the top 5 artists.
```



### Chat Completion Streaming

Runs a chat completion in streaming mode. Upon initiating the request the OpenAI 
server send asynchronously *Server-Side-Events* back to the client. These events 
are then handled by a handler function.


#### Sending Requests

`(chat-completion-streaming prompt handler & options)`

The OpenAI api key can be provided in an environment variable "OPENAI_API_KEY" or
explicitly passed as an option `:openai-api-key "sk-xxxxxxxxxxxxx"`.



#### Parameter «prompt»

A prompt is either a simple string like

```
"Who won the world series in 2020?"
```

or a list of prompt message 

```
[ {"role": "system", "content": "You are a helpful assistant."},
  {"role": "user", "content": "Who won the world series in 2020?"},
  {"role": "assistant", "content": "The Los Angeles Dodgers won the World Series in 2020."},
  {"role": "user", "content": "Where was it played?"} ]
```

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
        response  (openai/chat-completion-streaming prompt handler :sync true)]
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
```

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

## Configuring OpenAI API keys

### Possibility 1

Define an environment variable at the OS or Shell level:

```
OPENAI_API_KEY=sk-***********
```


### Possibility 2

On Unix / Linux operating system simply patch the 'repl.env' file in the REPL home
directory. 

`repl.env` is supported with *Venice* V1.12.15 and newer when the REPL has been setup
with:

```
foo>  java -jar venice-1.12.16.jar -setup -colors
```

The setup will create a `repl.sh` that *sources* a `repl.env` at REPL startup time.


Example 'repl.env':

```
# ------------------------------------------------------------------------------
# Environment variables for unix /linux operating systems
#
# This file will be 'sourced' by 'repl.sh' at REPL startup time. You can add
# as many environment variables you like.
# ------------------------------------------------------------------------------

# OpenAI api key
export OPENAI_API_KEY=sk-***********
```

