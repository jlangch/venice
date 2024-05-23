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
* How to call functions with model generated arguments
    * [Weather Example](openai-functions-weather.md)
    * [Database Example](openai-functions-database.md)



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
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :chat-opts { :temperature 0.1 })]
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
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :chat-opts { :temperature 0.1 })]
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
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :chat-opts { :temperature 0.1 })]
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
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :chat-opts { :temperature 0.1 })]
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
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            ;; force the model to use "get_n_day_weather_forecast"!!
                                            :tool_choice {:type "function", :function {:name "get_n_day_weather_forecast"}}
                                            :chat-opts { :temperature 0.1 })]
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
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :chat-opts { :temperature 0.1 })]
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
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :tool_choice "none"
                                            :chat-opts { :temperature 0.1 })]
    (openai/assert-response-http-ok response)
    (println "Choices:" (-> (:data response)  
                            (:choices)
                            (openai/pretty-print-json)))))
```

Response:

```json
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
        response    (openai/chat-completion prompt 
                                            :model "gpt-3.5-turbo"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :chat-opts { :temperature 0.1 })] 
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

