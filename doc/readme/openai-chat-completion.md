# Chat Completion

[OpenAI Chat](https://platform.openai.com/docs/api-reference/chat)


* [Completion](#completion)
    * [Sending Requests](#sending-requests)
    * [Examples](#examples)
        * [Example: Counting numbers (full model response)](#example-counting-numbers-full-model-response))
        * [Example: Counting numbers (model response content)](#example-counting-numbers-model-response-content)
        * [Example: Text correction](#example-text-correction)
        * [Example: Text data extraction](#example-text-data-extraction)
        * [Example: Generating SQL](#example-generating-sql)
        * [Example: Chain of Thought Prompting](#example-chain-of-thought-prompting)
* [Streaming](#streaming)
    * [Examples Streaming](#examples-streaming)
        * [Example sync](#example-sync)
        * [Example async](#example-async)
        
        
        
## Completion


### Sending Requests

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
| :model             | An OpenAI model. E.g.: "gpt-4o-mini". Defaults to "gpt-4o" |
| :chat-opts         | An optional map of OpenAI chat request options<br>E.g. {:temperature 0.2} <br>See: [OpenAI Request Options](https://platform.openai.com/docs/api-reference/chat/create) |
| :openai-api-key    | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :debug             | An optional debug flag (true/false). Defaults to false. <br>In debug mode prints the HTTP request and response data |
 
 
#### Response value

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


### Examples

#### Example: Counting numbers (full model response)

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
  "created": 1721762861,
  "usage": {
    "completion_tokens": 28,
    "prompt_tokens": 36,
    "total_tokens": 64
  },
  "model": "gpt-4o-2024-05-13",
  "id": "chatcmpl-9oFSfmqWUF9neR5bZXrGtGxErTlbu",
  "choices": [{
    "finish_reason": "stop",
    "index": 0,
    "message": {
      "role": "assistant",
      "content": "1, 2, 3, 4, 5, 6, 7, 8, 9, 10"
    },
    "logprobs": null
  }],
  "system_fingerprint": "fp_400f27fa1f",
  "object": "chat.completion"
}
```

#### Example: Counting numbers (model response content)

```clojure
;; print only the OpenAI response message content
(do
  (load-module :openai)

  (let [prompt    (str "Count to 10, with a comma between each number "
                       "and no newlines. E.g., 1, 2, 3, ...")
        response  (openai/chat-completion prompt)]
    (openai/assert-response-http-ok response)
    (println "Message:" (-> (:data response)
                            (openai/chat-extract-response-message-content)
                            (pr-str)))))
```

```
Message: "1, 2, 3, 4, 5, 6, 7, 8, 9, 10"
```

#### Example: Text correction

```clojure
;; Dealing with prompt options
(do
  (load-module :openai)

  (let [prompt      [ { :role     "system"
                        :content  "You will be provided with statements, and your task is to convert them to standard English." }
                      { :role     "user"
                        :content  "She no went to the market." } ]
        response    (openai/chat-completion prompt 
                                            :model "gpt-4o" 
                                            :chat-opts { :temperature 0.7
                                                         :max_tokens 64
                                                         :top_p 1 })]
    (openai/assert-response-http-ok response)
    (println "Message:" (-> (:data response)
                            (openai/chat-extract-response-message-content)
                            (openai/pretty-print-json)))))
```

```
Message: She did not go to the market.
```


#### Example: Text data extraction

```clojure
;; Dealing with prompt options
(do
  (load-module :openai)

  (let [prompt      [ { :role     "user"
                        :content  """
                                  Please extract the following information from the given text and 
                                  return it as a JSON object:

                                  name
                                  major
                                  school
                                  grades
                                  club

                                  This is the body of text to extract the information from:

                                  Peter Kilmore is a sophomore majoring in computer science at Stanford 
                                  University. He is Irish and has a 3.8 GPA. Peter is known 
                                  for his programming skills and is an active member of the 
                                  university's Robotics Club. He hopes to pursue a career in 
                                  artificial intelligence after graduating.
                                  """ } ]
        response    (openai/chat-completion prompt 
                                            :model "gpt-4-turbo" 
                                            :chat-opts { :temperature 0.1 })]
    (openai/assert-response-http-ok response)
    (println "Message:" (-> (:data response)
                            (openai/chat-extract-response-message-content)
                            (openai/pretty-print-json)))))
```

Message:

```json
{
  "name": "Peter Kilmore",
  "major": "computer science",
  "school": "Stanford University",
  "grades": "3.8 GPA",
  "club": "Robotics Club"
}
```


#### Example: Generating SQL

The database schema used in this SQL example is taken from the Chinook database. 
See [Venice Chinook](database.md#chinook-dataset-overview)

Note: The Venice *Functions Cookbook* provides an example using OpenAI functions to query a database.

The database schema required for the prompt is generated by

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
        response    (openai/chat-completion prompt 
                                            :model "gpt-4o" 
                                            :chat-opts { :temperature 0.1 })]
    (openai/assert-response-http-ok response)
    (println "Message:" (-> (:data response)
                            (openai/chat-extract-response-message-content)
                            (openai/pretty-print-json)))))
```

```
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


#### Example: Chain of Thought Prompting

Chain of thought (CoT) is a method that encourages Large Language Models (LLMs) to explain their reasoning. It advices the model to not only seeking an answer but also requiring it to explain its steps to arrive at that answer. CoT can improve the quality of the answer in case the models fails otherwise. 


##### Prompt 1a

```clojure
(do
  (load-module :openai)

  (let [prompt      [ { :role     "user"
                        :content  """
                                  I am looking for a name for my new pet, a cat. The cat's fur 
                                  is reddish and light tabby. Suggest me 5 names that I could 
                                  give my cat.
                                  """ } ]
        response    (openai/chat-completion prompt 
                                            :model "gpt-4o" 
                                            :chat-opts { :temperature 0.1 })]
    (openai/assert-response-http-ok response)
    (println (-> (:data response)
                 (openai/chat-extract-response-message-content)
                 (openai/pretty-print-json)))))
```

**Response:**

Sure, here are five names that might suit your reddish and light tabby cat:

1. **Ginger**
2. **Rusty**
3. **Amber**
4. **Maple**
5. **Sunny**

I hope one of these names feels just right for your new feline friend!


##### Prompt 1b (with explanation)

```clojure
(do
  (load-module :openai)

  (let [prompt      [ { :role     "user"
                        :content  """
                                  I am looking for a name for my new pet, a cat. The cat's fur 
                                  is reddish and light tabby. Suggest me 5 names that I could 
                                  give my cat. 
                                  
                                  Explain why you have chosen these names.
                                  """ } ]
        response    (openai/chat-completion prompt 
                                            :model "gpt-4o" 
                                            :chat-opts { :temperature 0.1 })]
    (openai/assert-response-http-ok response)
    (println (-> (:data response)
                 (openai/chat-extract-response-message-content)
                 (openai/pretty-print-json)))))
```

**Response:**

Sure, I'd be happy to help you name your new cat! Here are five names that might suit your reddish and light tabby cat, along with explanations for each:

1. **Ginger**: This name is a classic choice for cats with reddish fur. It directly references the color of your cat's coat and has a warm, friendly feel to it.

2. **Rusty**: This name evokes the reddish-brown color of rust, which matches your cat's fur. It's a cute and slightly rugged name that can suit a playful or adventurous cat.

3. **Maple**: Inspired by the warm, reddish hues of autumn leaves, Maple is a sweet and unique name that reflects the color of your cat's fur. It also has a cozy, natural vibe.

4. **Sunny**: This name captures the light, warm tones of your cat's fur and suggests a bright, cheerful personality. It's a happy, uplifting name that can suit a cat with a sunny disposition.

5. **Tawny**: Tawny is a term used to describe a warm, sandy color, which can be a perfect match for a light tabby cat. It's a sophisticated and elegant name that highlights your cat's beautiful coat.

I hope one of these names feels right for your new feline friend!



##### Prompt 2 (solving puzzle / reasoning)

```clojure
(do
  (load-module :openai)

  (let [prompt      [ { :role     "user"
                        :content  """
                                  A farmer with a wolf, a goat, and a cabbage must cross a river 
                                  with a boat. The boat can carry only the farmer and a single item.
                                  If left unattended together, the wolf would eat the goat or the
                                  goat would eat the cabbage. How can they cross the river without
                                  anything being eaten? 
                                  
                                  Describe your reasoning step by step.
                                  """ } ]
        response    (openai/chat-completion prompt 
                                            :model "gpt-4o" 
                                            :chat-opts { :temperature 0.1 })]
    (openai/assert-response-http-ok response)
    (println (-> (:data response)
                 (openai/chat-extract-response-message-content)
                 (openai/pretty-print-json)))))
```

**Response:**

To solve this classic river-crossing puzzle, we need to ensure that at no point are the wolf and the goat left alone together, nor the goat and the cabbage. Here's a step-by-step solution:

1. **First Trip: Farmer takes the Goat across the river.**
    - On the left bank: Wolf, Cabbage
    - On the right bank: Farmer, Goat

2. **Farmer returns alone to the left bank.**
    - On the left bank: Farmer, Wolf, Cabbage
    - On the right bank: Goat

3. **Second Trip: Farmer takes the Wolf across the river.**
    - On the left bank: Cabbage
    - On the right bank: Farmer, Wolf, Goat

4. **Farmer returns with the Goat to the left bank.**
    - On the left bank: Farmer, Goat, Cabbage
    - On the right bank: Wolf

5. **Third Trip: Farmer takes the Cabbage across the river.**
    - On the left bank: Goat
    - On the right bank: Farmer, Wolf, Cabbage

6. **Farmer returns alone to the left bank.**
    - On the left bank: Farmer, Goat
    - On the right bank: Wolf, Cabbage

7. **Fourth Trip: Farmer takes the Goat across the river.**
    - On the left bank: (empty)
    - On the right bank: Farmer, Goat, Wolf, Cabbage

By following these steps, the farmer successfully gets all three items across the river without any of them being eaten.



## Streaming

Runs a chat completion in streaming mode. Upon initiating the request the OpenAI 
server send asynchronously *Server-Side-Events* back to the client. These events 
are then handled by a handler function.


### Sending Requests

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
| :model             | An OpenAI model. E.g.: "gpt-4o-mini". Defaults to "gpt-4o" |
| :sync              | if *true* runs the request syncronously and waits until the full message response is available.<br>if *false* runs the request asyncronously and returns immediately with the response :data field holding a `future` that can be deref'd (with an optional timeout) to get the full message.<br>Defaults to *true* |
| :chat-opts         | An optional map of OpenAI chat request options<br>E.g. {:temperature 0.2} <br>See: [OpenAI Request Options](https://platform.openai.com/docs/api-reference/chat/create) |
| :openai-api-key    | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :debug             | An optional debug flag (true/false). Defaults to false. <br>In debug mode prints the HTTP request and response data |
 
##### Response value

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


### Examples Streaming

#### Example sync

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
    (openai/assert-response-http-ok response)
    (let [data (:data response)]
      (println "Message:" (pr-str (:message data))))))
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
Completed.
Message: "1, 2, 3, 4, 5"
```

To get the usage statistics with the final message add the chat option `{ :stream_options { :include_usage true } }`:

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
        response  (openai/chat-completion-streaming 
                     prompt 
                     handler 
                     :sync true
                     :chat-opts { :stream_options { :include_usage true } })]                  
    (openai/assert-response-http-ok response)
    (let [data (:data response)]
      (println "Usage:  " (pr-str (:usage data)))
      (println "Message:" (pr-str (:message data))))))
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
Completed.
Usage:   {:prompt_tokens 36 :total_tokens 49 :completion_tokens 13}
Message: "1, 2, 3, 4, 5"
```


#### Example async

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
    (openai/assert-response-http-ok response)
    (let [data @(:data response)]
      (println "Message:" (pr-str (:message data))))))
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
Completed.
Message: "1, 2, 3, 4, 5"
```

To get the usage statistics with the final message add the chat option `{ :stream_options { :include_usage true } }`:

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
        response  (openai/chat-completion-streaming 
                     prompt 
                     handler 
                     :sync false
                     :chat-opts { :stream_options { :include_usage true } })]
    (openai/assert-response-http-ok response)
    (let [data @(:data response)]
      (println "Message:" (pr-str (:message data))))))
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
Completed.
Message: "1, 2, 3, 4, 5"
```
       
