# Functions Cookbook

This cookbook is adapted from the [OpenAI API Functions Cookbook](https://cookbook.openai.com/examples/how_to_call_functions_with_chat_models) to demonstrate how OpenAI functions can be used with *Venice*.

This tutorial covers the use of Chat Completions API in combination with external functions 
to extend the capabilities of GPT models.

The Chat Completion API provides an optional `tools` parameter that specifies the functions meta 
data (name, arguments names & types and a function description). The models generate function 
arguments based on the prompt and the function meta data. The OpenAI API will actually not call 
the function but instruct the caller to execute a function with the model outputs of function name 
and arguments.

The model decides based on the prompt data if and which function to call. The OpenAI API can be forced
to use a specific function:  `tools_choice = {:type "function", :function {:name "my_function"}}`. The API
can also be forced to not use any function by setting the parameter `tools_choice = "none"`. 


* [Overview](#overview)
* [How to generate function arguments](#how-to-generate-function-arguments)
    * [Basic concepts](#basic-concepts)
    * [Forcing the use of specific functions or no function](#forcing-the-use-of-specific-functions-or-no-function)
    * [Parallel Function Calling](#parallel-function-calling)
* [How to call functions with model generated arguments](#how-to-call-functions-with-model-generated-arguments)
    * [Weather example](#weather-example)
    * [Database example](#database-example)



## Overview

This cookbook contains the following 2 sections:

* **How to generate function arguments:** Specify a set of functions and use the API to generate 
  function arguments.
* **How to call functions with model generated arguments:** Close the loop by actually executing
  functions with model generated arguments.


## How to generate function arguments

### Basic concepts

Throughout the cookbook we will use a hypothetical weather API, that defines two functions

* `get_current_weather` returns the temperature for a given location in Celsius or Fahrenheit

* `get_n_day_weather_forecast` returns an N-day weather forecast for a location 


The function definitions are defined in the `openai-demo` module. You can have at look at by
running these commands in a Venice REPL:

```
venice> (load-module :openai-demo)
venice> (doc :openai-demo)
```


The function definitions are passed in as `tools` parameter to the OpenAI request:


The weather API functions definitions are defined in the `:openai-demo` module and look like:

```clojure
(defn demo-weather-function-defs [] 
   ;; Returns a Venice data map with the OpenAI demo function definitions, that corresponds 
   ;; to the OpenAI chat completion request 'tools' JSON data.
   ;; For better readability the map keys are Venice keyword. Strings would equally work.
   [ {
      :type "function"
      :function {
        :name "get_current_weather"
        :description "Get the current weather"
        :parameters {
          :type "object"
          :properties {
            "location" {
              :type "string"
              :description "The city and state, e.g. San Francisco, CA"
            }
            "format" {
              :type "string"
              :enum ["celsius", "fahrenheit"]
              :description "The temperature unit to use. Infer this from the users location."
            }
          }
          :required ["location", "format"]
        }
      }
    },
    {
      :type "function"
      :function {
        :name "get_n_day_weather_forecast"
        :description "Get an N-day weather forecast"
        :parameters {
          :type "object"
          :properties {
            "location" {
              :type "string"
              :description "The city and state, e.g. San Francisco, CA"
            }
            "format" {
              :type "string"
              :enum ["celsius", "fahrenheit"]
              :description "The temperature unit to use. Infer this from the users location.",
            }
            "num_days" {
              :type "integer"
              :description "The number of days to forecast"
            }
          }
          :required ["location", "format", "num_days"]
        }
      }
    } ] )
```


If we prompt the model about the current weather, it will respond with some clarifying 
questions due to the missing location.

```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)  
  
  (let [prompt      [ { :role     "system"
                        :content  """
                                  Don't make assumptions about what values to plug into functions.
                                  Ask for clarification if a user request is ambiguous.
                                  """ }
                      { :role     "user"
                        :content  "What's the weather like today?" } ]
        prompt-opts { :temperature 0.1 }
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :prompt-opts prompt-opts)]
    (openai/assert-response-http-ok response)
    (println "Message:" (-> (:data response)
                            (:choices)
                            (openai/pretty-print-json)))))
```

The OpenAI model answers:

```json
[{
  "finish_reason": "stop",
  "index": 0,
  "message": {
    "role": "assistant",
    "content": "Sure, I can help with that. Could you please tell me your location?"
  },
  "logprobs": null
}]
```

Once we provide the missing information, it will generate the appropriate function 
arguments for us.

```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)
  
  (let [prompt      [ { :role     "system"
                        :content  """
                                  Don't make assumptions about what values to plug into functions.
                                  Ask for clarification if a user request is ambiguous.
                                  """ }
                      { :role     "user"
                        :content  "What's the weather like today?" }
                      { :role     "assistant"
                        :content  "Sure, I can help with that. Could you please tell me your location" }
                      { :role     "user"
                        :content  "I'm in Glasgow, Scotland." } ]
        prompt-opts { :temperature 0.1 }
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :prompt-opts prompt-opts)]
    (openai/assert-response-http-ok response)
    (println "Choices:" (-> (:data response) 
                            (:choices)                            
                            (openai/pretty-print-json)))))
```

Returns the message:

```json
[{
  "finish_reason": "tool_calls",
  "index": 0,
  "message": {
    "role": "assistant",
    "tool_calls": [{
      "function": {
        "name": "get_current_weather",
        "arguments": "{\n  \"format\": \"celsius\",\n  \"location\": \"Glasgow, Scotland\"\n}"
      },
      "id": "call_TZcPN71eztpUlO0U5qPLVbQA",
      "type": "function"
    }],
    "content": null
  },
  "logprobs": null
 }]
```

By prompting it differently, we can get it to target the `get_n_day_weather_forecast` function:

```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)
  
  (let [prompt      [ { :role     "system"
                        :content  """
                                  Don't make assumptions about what values to plug into functions.
                                  Ask for clarification if a user request is ambiguous.
                                  """ }
                      { :role     "user"
                        :content  "What is the weather going to be like in Glasgow, Scotland over the next n days" } ]
        prompt-opts { :temperature 0.1 }
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :prompt-opts prompt-opts)]
    (openai/assert-response-http-ok response)
    (println "Message:")
    (println (-> (:data response)                            
                 (openai/chat-extract-response-message-content)
                 (openai/pretty-print-json)))))
```


Answer:

```
You mentioned "n days" for the weather forecast in Glasgow, Scotland. Could you please specify the number of days you are interested in?
```


Once again, the model is asking us for clarification because it doesn't have enough information 
yet. In this case it already knows the location for the forecast, but it needs to know how many 
days are required in the forecast.


```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)
  
  (let [prompt      [ { :role     "system"
                        :content  """
                                  Don't make assumptions about what values to plug into functions.
                                  Ask for clarification if a user request is ambiguous.
                                  """ }
                      { :role     "user"
                        :content  "What is the weather going to be like in Glasgow, Scotland over the next x days?" }
                      { :role     "assistant"
                        :content  """
                                  I'm sorry, but I need to know the exact number of days you want the 
                                  forecast for. Could you please specify?
                                  """ }
                      { :role     "user"
                        :content  "5 days" } ]
        prompt-opts { :temperature 0.1 }
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :prompt-opts prompt-opts)]
    (openai/assert-response-http-ok response)
    (println "Choices:" (-> (:data response)  
                            (:choices)                          
                            (openai/pretty-print-json)))))
```

Returns the message:

```json
[{
  "finish_reason": "tool_calls",
  "index": 0,
  "message": {
    "role": "assistant",
    "tool_calls": [{
      "function": {
        "name": "get_n_day_weather_forecast",
        "arguments": "{\n  \"num_days\": 5,\n  \"format\": \"celsius\",\n  \"location\": \"Glasgow, Scotland\"\n}"
      },
      "id": "call_b5DWxEVDqqvQ26Z0bezIhTmL",
      "type": "function"
    }],
    "content": null
  },
  "logprobs": null
}]
```


### Forcing the use of specific functions or no function

We can force the model to use a specific function, for example `get_n_day_weather_forecast` by 
using the `:tool_choice` option argument. By doing so, we force the model to make assumptions about 
how to use it.

Force the model to use "get_n_day_weather_forecast"

```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)
  
  (let [prompt      [ { :role     "system"
                        :content  """
                                  Don't make assumptions about what values to plug into functions.
                                  Ask for clarification if a user request is ambiguous.
                                  """ }
                      { :role     "user"
                        :content  "Give me a weather report for Toronto, Canada." } ]
        prompt-opts { :temperature 0.1 }
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            ;; force the model to use "get_n_day_weather_forecast"!!
                                            :tool_choice {:type "function", :function {:name "get_n_day_weather_forecast"}}
                                            :prompt-opts prompt-opts)]
    (openai/assert-response-http-ok response)
    (println "Choices:" (-> (:data response)  
                            (:choices)                          
                            (openai/pretty-print-json)))))
```


The OpenAI model answers:

```json
[{
  "finish_reason": "stop",
  "index": 0,
  "message": {
    "role": "assistant",
    "tool_calls": [{
      "function": {
        "name": "get_n_day_weather_forecast",
        "arguments": "{\n  \"num_days\": 1,\n  \"format\": \"celsius\",\n  \"location\": \"Toronto, Canada\"\n}"
      },
      "id": "call_XHddNciVWOFZ3liobUdqpBBl",
      "type": "function"
    }],
    "content": null
  },
  "logprobs": null
}]
```

If we don't force the model to use "get_n_day_weather_forecast" it answers

```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)
  
  (let [prompt      [ { :role     "system"
                        :content  """
                                  Don't make assumptions about what values to plug into functions.
                                  Ask for clarification if a user request is ambiguous.
                                  """ }
                      { :role     "user"
                        :content  "Give me a weather report for Toronto, Canada." } ]
        prompt-opts { :temperature 0.1 }
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :prompt-opts prompt-opts)]
    (openai/assert-response-http-ok response)
    (println "Choices:" (-> (:data response)  
                            (:choices)                          
                            (openai/pretty-print-json)))))
```


with:

```json
[{
  "finish_reason": "tool_calls",
  "index": 0,
  "message": {
    "role": "assistant",
    "tool_calls": [{
      "function": {
        "name": "get_current_weather",
        "arguments": "{\n  \"format\": \"celsius\",\n  \"location\": \"Toronto, Canada\"\n}"
      },
      "id": "call_3zYkGtCs08ABTGVLGCwGXAZG",
      "type": "function"
    }],
    "content": null
  },
  "logprobs": null
}]
```

We can also force the model to not use a function at all. By doing so we prevent it from 
producing a proper function call.

```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)
  
  (let [prompt      [ { :role     "system"
                        :content  """
                                  Don't make assumptions about what values to plug into functions.
                                  Ask for clarification if a user request is ambiguous.
                                  """ }
                      { :role     "user"
                        :content  "Give me the current weather (use Celcius) for Toronto, Canada." } ]
        prompt-opts { :temperature 0.1 }
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :tool_choice "none"
                                            :prompt-opts prompt-opts)]
    (openai/assert-response-http-ok response)
    (println "Choices:" (-> (:data response)  
                            (:choices)
                            (openai/pretty-print-json)))))
```

Response:

```josn
[{
  "finish_reason": "stop",
  "index": 0,
  "message": {
    "role": "assistant",
    "content": "Sure, let me get that information for you.\n\nAssistant to=functions.get_current_weather:\n{\n  \"format\": \"celsius\",\n  \"location\": \"Toronto, Canada\"\n}"
  },
  "logprobs": null
}]
```

### Parallel Function Calling

Actual GPT-3.5-Turbo models can call multiple functions in one turn.

```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)
  
  (let [prompt      [ { :role     "system"
                        :content  """
                                  Don't make assumptions about what values to plug into functions.
                                  Ask for clarification if a user request is ambiguous.
                                  """ }
                      { :role     "user"
                        :content  """
                                  What is the weather going to be like in San Francisco 
                                  and Glasgow over the next 4 day? Give the temperature in Celsius.
                                  """ } ]
        prompt-opts { :temperature 0.1 }
        response    (openai/chat-completion prompt 
                                            :model "gpt-3.5-turbo"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :prompt-opts prompt-opts)] 
    (openai/assert-response-http-ok response)
    (println "Choices:" (-> (:data response) 
                            (:choices)                            
                            (openai/pretty-print-json)))))
```

Response:

```json
[{
  "finish_reason": "tool_calls",
  "index": 0,
  "message": {
    "role": "assistant",
    "tool_calls": [{
      "function": {
        "name": "get_n_day_weather_forecast",
        "arguments": "{\"num_days\": 4, \"format\": \"celsius\", \"location\": \"San Francisco\"}"
      },
      "id": "call_gIdR2g4mieRcClQEDestGO1x",
      "type": "function"
    },{
      "function": {
        "name": "get_n_day_weather_forecast",
        "arguments": "{\"num_days\": 4, \"format\": \"celsius\", \"location\": \"Glasgow\"}"
      },
      "id": "call_9A9YPcNDPpZ5G1zqumPNeq6R",
      "type": "function"
    }],
    "content": null
  },
  "logprobs": null
}]
```




## How to call functions with model generated arguments

The next examples demonstrate how to execute functions whose inputs are model-generated and deliver 
the required knowledge to model for answering questions



### Weather example

The OpenAI shall answer questions about the current weather at a given location.


**Workflow**

1. Ask the model about the current weather in Glasgow

2. The model does not have enough information about the current weather in Glasgow and returns a function call request to retrieve that information from an external source. It provides function parameters in the `tool_calls` part of the response:
   
   ```
   { "tool_calls": [{
       "function": {
         "name": "get_current_weather",
         "arguments": "{\n  \"format\": \"celsius\",\n  \"location\": \"Glasgow\"\n}"
       },
       "id": "call_XHddNciVWOFZ3liobUdqpBBl",
       "type": "function"
      }]
   }
   ```

3. The client calls the requested function to get the information

      `(openai/exec-fn response fn-map)` is calling the requested function from the `fn-map` based on the model's response.
   

4. The function returns a JSON object with the current Glasgow weather data

   Function arguments:
   
   ```json
   { 
     "location": "Glasgow",
     "format": "celsius"
   }
   ```
   
   Function return value:
   
   ```json
   { 
     "location": "Glasgow",
     "format": "celsius",
     "general": "sunny",
     "temperature": "16.0"
   }
   ```
   
   Note: The function responds with an error if there is no data for a location:
   
   ```json
   { 
     "location": "London",
     "error":    "No weather data available for London!"
   }
   ```   

5. The client adds an additional prompt message with the function's JSON response data and asks the model again

6. The model has now all information and returns the final answer


**Weather function implementation**

The weather function details are defined in "openai-demo.venice". To see its source code type in a REPL:

```clojure
(do
  (load-module :openai-demo)
  (doc :openai-demo))
```


The weather function map, maps the OpenAI function name to the Venice function name:

```clojure
(defn demo-weather-function-map []
  { "get_current_weather"   get-current-weather } )
```

The weather data function is defined as:

```clojure
(defn get-current-weather 
  ([named-args] 
    (assert map? named-args)
    (get-current-weather (get named-args "location")   ;; argument dispatching
                         (get named-args "format")))

  ([location format]
    (println """
             Calling fn "get-current-weather" with \
             location="~{location}", format="~{format}"
             """)
    (case location
      "Glasgow"             (json/write-str
                              { :location    location
                                :format      format
                                :temperature (temperature 16 format)
                                :general     "sunny" })

      "San Francisco, CA"   (json/write-str
                              { :location    location
                                :format      format
                                :temperature (temperature 12 format)
                                :general     "rainy" })
      (json/write-str { :location location
                        :error    "No weather data available for ~{location}!" }))))

(defn- temperature [t format]
  (if (str/equals-ignore-case? format "fahrenheit")
    (str/format "%#.1f" (celsius-to-fahrenheit t))
    (str/format "%#.1f" (double t))))

(defn- celsius-to-fahrenheit [c]
  (-> (* (double c) 9)
      (/ 5)
      (+ 32)))
```


**Running the weather example**

*Note: for simplicity this example just handles the happy path!*

Run this code in a REPL:

```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)
  
  (println "Phase #1: prompt the model")
  (let [model     "gpt-4"
        prompt    [ { :role     "system"
                      :content  """
                                Don't make assumptions about what values to plug into functions.
                                Ask for clarification if a user request is ambiguous.
                                """ }
                    { :role     "user"
                      :content  """
                                What is the current weather in Glasgow? Give the temperature in 
                                Celsius.
                                """ } ]
                  ;; [1] Ask the model about the weather in Glasgow
        response  (openai/chat-completion prompt
                                          :model model
                                          :tools (openai-demo/demo-weather-function-defs)
                                          :prompt-opts { :temperature 0.1 })]
    (openai/assert-response-http-ok response)
    (let [response (:data response)]     
      ;;(println "Message:" (->> (openai/chat-extract-response-message response)
      ;;                         (openai/pretty-print-json)))

      (assert (openai/chat-finish-reason-tool-calls?  response))
      
      ;; [2] The model returns a function call request
      (println "\nPhase #2: call the requested functions")
      
      ;; [3] Call the requested function (openai/exec-fn ...)
      (let [fn-map  (openai-demo/demo-weather-function-map)
            results (openai/exec-fn response fn-map)
            answer  (:ok (first results))]            ;; [4] The function's returned JSON data
        (println "Fn call result:" (pr-str answer))

        (println "\nPhase #3: prompt the model again with additional knowledge")
        
        ;; [5] Additional prompt message with the function's response
        (let [prompt-fn { :role     "function"
                          :name     (openai/chat-extract-function-name response)
                          :content  answer }
                        ;; [6] Ask the model again
              response  (openai/chat-completion (conj prompt prompt-fn)
                                                :model model
                                                :prompt-opts { :temperature 0.1 })]
          (openai/assert-response-http-ok response)
          (let [response (:data response)
                content  (openai/chat-extract-response-message-content response)] ;; [6] Final answer
            (assert (openai/chat-finish-reason-stop?  response))
            (println "\nFinal answer: ~(pr-str content)")))))))
```

Response:

```
Final answer: "The current weather in Glasgow is sunny with a temperature of 16.0 degrees Celsius."
```

**Asking the weather for a location without weather data**

When asking for the current weather for London, where there is no weather data 
available

```
prompt-usr  { :role     "user"
              :content  """
                        What is the current weather in London? Give the temperature in 
                        Celsius.
                        """ }
```

the model responds without halluscinating:

```
Final answer: "I'm sorry, but I currently don't have access to the weather data for London."
```


**Translating the answer**

To ask the model to translate the answer to German, just enhance the "user" role prompt
with the instruction "Translate to German.":

```
prompt-usr  { :role     "user"
              :content  """
                        What is the current weather in Glasgow? Give the temperature in 
                        Celsius. Translate to German.
                        """ }
```

Translated response:

```
Final answer: "Das aktuelle Wetter in Glasgow ist sonnig und die Temperatur beträgt 16,0 Grad Celsius."
```



### Database example

In the following examples we'll use the OpenAI chat completion API to answer questions
about a database. For simplicity the Chinook sample database will be used. See 
[Venice and Chinook Dataset](database.md#chinook-dataset-overview)

The OpenAI model shall be enabled to answer questions on the dataset like: *Who are the top 5 artists by number of tracks?*

Before starting, follow the [Venice Database Tutorial](database.md) to:

 1. Install the PostgreSQL JDBC driver
 2. Start a PostgreSQL Docker Instance
 3. Load the Chinook dataset into the PostgreSQL database
 
All these preliminary tasks can be run from a Venice REPL.


Then run the full example:

```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])


  ;; create a database connection
  (defn db-connection []
    (jdbp/create-connection "localhost" 5432 
                            "chinook_auto_increment" 
                            "postgres" "postgres"))


  ;; get the database schema (formatted text for OpenAI) in the format
  ;;   Table: table1
  ;;   Columns: col1, col2, col3, ...
  ;;   Table: table2
  ;;   Columns: col1, col2, col3, ...
  ;;   ...
  (defn db-schema [conn]
    (->> (jdbc/tables-with-columns conn)
         (map (fn [[t c]] 
                (str "Table: " t "\nColumns: " (str/join ", " c)))) 
         (str/join "\n")))
  
  
  ;; create the OpenAI API 'tools' function definition for "ask_database"
  (defn function-defs [database-schema]
    [ { :type "function"
        :function {
          :name "ask_database"
          :description """
                       Use this function to answer user questions about music. 
                       Input should be a fully formed SQL query.
                       """
          :parameters {
            :type "object"
            :properties {
              "query" {
                :type "string"
                :description  """
                              SQL query extracting info to answer the user's question.
                              
                              SQL should be written using this database schema:
                              ~{database-schema}
                              
                              The query should be returned in plain text, not in JSON.
                              """
              }
            }
            :required ["query"]
          }
        }
      } ] )


  ;; query the database with a provided SQL.
  ;;   conn:       a JDBC database connection
  ;;   named-args: a map e.g.: {"query" "SELECT * FROM Foo" }
  (defn ask-database [conn named-args]
    (println "Calling function 'ask-database'")
    (try-with [query (get named-args "query")
               stmt  (jdbc/create-statement conn)]
      (println "DB Query:" query)
      (let [result (jdbc/execute-query stmt query)
            rows   (:rows result)]       
        (json/write-str rows))  ;; return the rows as a JSON string
      (catch :Exception e
             ;; return the error as a JSON string
             (json/write-str { "query" query
                               "error" "Query failed with error: ~(ex-message e)" }))))


  ;; Ask the model
  ;; Note: for simplicity this example just handles the happy path!
  
  ;; Phase 1: Initial question to the model
  (try-with [conn (db-connection)]
    (let [model       "gpt-4"
          prompt      [ { :role     "system"
                          :content  """
                                    Answer user questions by generating SQL queries against 
                                    the Chinook Music Database.
                                    """ }
                        { :role     "user"
                          :content  "Hi, who are the top 5 artists by number of tracks?" } ]
          fn-defs     (function-defs (db-schema conn))
          response    (openai/chat-completion prompt 
                                              :model model
                                              :tools fn-defs
                                              :prompt-opts { :temperature 0.1 })] 
      (openai/assert-response-http-ok response)
      
      ;; Phase 2: model requests to call the function "ask_database"
      (let [response (:data response)]
        ;;(println "Message:" (->> (openai/chat-extract-response-message response)
        ;;                         (openai/pretty-print-json message)))

        (assert (openai/chat-finish-reason-tool-calls?  response))
        
        ;; call the function "ask_database"
        (let [fn-map     { "ask_database" (partial ask-database conn) }
              fn-result  (first (openai/exec-fn response fn-map))
              answer     (:ok fn-result)
              err        (:err fn-result)]
          (when err (throw err))  ;; "ask_database" failed
          (println "Fn call result:" (pr-str answer))
          
          ;; Phase 3: Ask the model again with the queried music data obtained
          ;;            from the function "ask_database"
          (let [prompt-fn  { :role     "function"
                             :name     (openai/chat-extract-function-name response)
                             :content  answer }
                response   (openai/chat-completion (conj prompt prompt-fn) 
                                                   :model model
                                                   :prompt-opts { :temperature 0.1 })]
            (openai/assert-response-http-ok response)
            (let [response (:data response)
                  content  (openai/chat-extract-response-message-content response)]
              (assert (openai/chat-finish-reason-stop?  response))
              (println)
              (println "Question: \"Hi, who are the top 5 artists by number of tracks?\"")
              (println)
              (println content))))))))
```

The model answers the question "Hi, who are the top 5 artists by number of tracks?" 
with:

```
The top 5 artists by number of tracks are:

1. Iron Maiden with 213 tracks
2. U2 with 135 tracks
3. Led Zeppelin with 114 tracks
4. Metallica with 112 tracks
5. Deep Purple with 92 tracks
```



## Debugging

To debug requests and responses enable the debug option at the `openai/chat-completion` call:

```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)
  
  (let [prompt      [ { :role     "system"
                        :content  """
                                  Don't make assumptions about what values to plug into functions.
                                  Ask for clarification if a user request is ambiguous.
                                  """ }
                      { :role     "user"
                        :content  "What's the weather like today in Glasgow, Scotland?" } ]
        prompt-opts { :temperature 0.1 }
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :prompt-opts prompt-opts
                                            :debug true)]         ;; <======= DEBUGGING ON
    (openai/assert-response-http-ok response)
    (println "Response:" (-> (:data response) 
                             (openai/pretty-print-json)))))
```


