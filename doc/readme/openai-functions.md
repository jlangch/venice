# OpenAI Functions Cookbook

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
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Message:" (-> (:data response)
                              (:choices)
                              (openai/pretty-print-json)))
      (println "Error:"   (:data response)))))
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
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Choices:" (-> (:data response) 
                              (:choices)                            
                              (openai/pretty-print-json)))
      (println "Error:"   (:data response)))))
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
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Message:" (-> (:data response)                            
                              (openai/extract-response-message-content)
                              (openai/pretty-print-json)))
      (println "Error:"   (:data response)))))
```


The OpenAI model answers:

```json
[{
  "finish_reason": "stop",
  "index": 0,
  "message": {
    "role": "assistant",
    "content": "I'm sorry, but I need to know the exact number of days you want the forecast for. Could you please specify?"
  },
  "logprobs": null
}]
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
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Choices:" (-> (:data response)  
                              (:choices)                          
                              (openai/pretty-print-json)))
      (println "Error:"   (:data response)))))
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
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Choices:" (-> (:data response)  
                              (:choices)                          
                              (openai/pretty-print-json)))
      (println "Error:"   (:data response)))))
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
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Choices:" (-> (:data response)  
                              (:choices)                          
                              (openai/pretty-print-json)))
      (println "Error:"   (:data response)))))
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
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Choices:" (-> (:data response)  
                              (:choices)                          
                              (openai/pretty-print-json)))
      (println "Error:"   (:data response)))))
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
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Choices:" (-> (:data response) 
                              (:choices)                            
                              (openai/pretty-print-json)))
      (println "Error:"   (:data response)))))
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
     "error"     "No weather data available for London!"
   }
   ```   

5. The client adds an additional prompt messages with the function's JSON response data and asks the model again

6. The model has now all information and returns the final answer



*Note: for simplicity reasons this example just handles the happy path!*

```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)
  
  (println "Phase #1: prompt the model")
  (let [prompt-sys  { :role     "system"
                      :content  """
                                Don't make assumptions about what values to plug into functions.
                                Ask for clarification if a user request is ambiguous.
                                """ }
        prompt-usr  { :role     "user"
                      :content  """
                                What is the current weather in Glasgow? Give the temperature in 
                                Celsius.
                                """ }
                  ;; [1] Ask the model about the weather in Glasgow
        response  (openai/chat-completion [ prompt-sys prompt-usr ] 
                                          :model "gpt-4"
                                          :tools (openai-demo/demo-weather-function-defs)
                                          :prompt-opts { :temperature 0.1 })]
    (assert (= (:status response) 200))
    (let [response (:data response)
          message  (openai/extract-response-message response)]
      (println "\nPhase #2: call the requested functions")
      ;; [2] The model returns a function call request
      (assert (openai/finish-reason-tool-calls?  response))
      (let [fn-map  (openai-demo/demo-weather-function-map)
            results (openai/exec-fn response fn-map)  ;; [3] Call the requested function
            answer  (:ok (first results))]            ;; [4] The function's returned JSON data
        (println "Fn call result:" (pr-str answer))

        (println "\nPhase #3: prompt the model again with additional knowledge")
        ;; [5] Additional prompt message with the function's response
        (let [prompt-fn  { :role     "function"
                           :name     (openai/extract-function-name response)
                           :content  answer }
                        ;; [6] Ask the model again
              response  (openai/chat-completion [ prompt-sys prompt-usr prompt-fn ]  
                                                :model "gpt-4"
                                                :prompt-opts { :temperature 0.1 })]
          (assert (= (:status response) 200))
          (let [response (:data response)
                content  (openai/extract-response-message-content response)] ;; [6] Final answer
            (assert (openai/finish-reason-stop?  response))
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

To ask the model to translate the answer to German, just enhanced the "user" role prompt
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

The OpenAI model shall be enabled to answer questions like: *Who are the top 5 artists by number of tracks?*

Before starting, follow the [Venice Database tutorial](database.md) to:

 1. Install the PostgreSQL JDBC driver
 2. Start a PostgreSQL Docker Instance
 3. Load the Chinook dataset into the PostgreSQL database
 
All these tasks can be run from a Venice REPL.

*... work in progress ...*

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

  ;; create the database schema                        
  (defn db-schema [conn]
    (->> (map (fn [[t c]] (str "Table: " t "\nColumns: " (str/join ", " c)))
              (jdbc/tables-with-columns conn)) 
         (str/join "\n")))
  
  ;; create the OPenAI API 'tools' function definition "ask_database"
  (defn tools [database-schema]
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

  ;; function to query the database with a provided SQL.
  (defn ask-database [conn named-args]
    (println "Calling function 'ask-database')
    (try-with [query (get named-args "query")
               stmt (jdbc/create-statement conn)]
      (println "DB Query:" query)
      (-> (jdbc/execute-query stmt query)
          (jdbc/print-query-result))
      (catch :Exception e
             "Query failed with error: ~(ex-message e)")))


  ;; Ask the model
  ;; Note: for simplicity reasons this example just handles the happy path!
  (try-with [conn (db-connection)]
    (let [prompt      [ { :role     "system"
                          :content  """
                                    Answer user questions by generating SQL queries against the 
                                    Chinook Music Database.
                                    """ }
                        { :role     "user"
                          :content  "Hi, who are the top 5 artists by number of tracks?" } ]
          prompt-opts { :temperature 0.1 }
          schema      (db-schema conn)
          fn-defs     (function-defs schema)
          response    (openai/chat-completion prompt 
                                              :model "gpt-4"
                                              :tools fn-defs
                                              :prompt-opts prompt-opts)] 
      (println "Status:       " (:status response))
      (println "Mimetype:     " (:mimetype response))
      (println)
      (assert (= (:status response) 200))
        (let [response (:data response)
              message  (openai/extract-response-message response)]
          (assert (openai/finish-reason-tool-calls?  response))
          ;; (println "Message:" (openai/pretty-print-json message))
          (let [fn-map { "ask_database" ask-database }
                results (openai/exec-fn response fn-map)]
            (println "\nFn results:" (pr-str results))))))

)
```






## Debugging

To debug requests and responses set enable the debug option at the `openai/chat-completion` call:

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
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Choices:" (-> (:data response) 
                              (:choices)                            
                              (openai/pretty-print-json)))
      (println "Error:"   (:data response)))))
```


