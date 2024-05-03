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
            :location {
              :type "string"
              :description "The city and state, e.g. San Francisco, CA"
            }
            :format {
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
            :location {
              :type "string"
              :description "The city and state, e.g. San Francisco, CA"
            }
            :format {
              :type "string"
              :enum ["celsius", "fahrenheit"]
              :description "The temperature unit to use. Infer this from the users location.",
            }
            :num_days {
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

OpenAI answers:

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

Returns the choices:

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


OpenAI answers:

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

Returns the choices:

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


#### Forcing the use of specific functions or no function

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


and the model answers with:

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

### Full weather example with supplied functions (version 1)

The OpenAI model calls the function `get-current-weather` to answer the question about
the current weather in Glasgow.

But the model does not know how to proceed with the supplied results from the called 
functions.

In a first attempt we can return the result from the function call

`{:ok "The current weather in Glasgow is sunny at 16°C"}`

 as 
 
 `The current weather in Glasgow is sunny at 16°C`
 
 to the user.


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
                                  What is the current weather in Glasgow? Give the temperature in 
                                  Celsius.
                                  """ } ]
        prompt-opts { :temperature 0.1 }
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :prompt-opts prompt-opts)] 
    (println "Status:       " (:status response))
    (println "Mimetype:     " (:mimetype response))
    (println)
    (if (= (:status response) 200)
      (let [response (:data response)
            message  (openai/extract-response-message response)]
        (println "Finish Reason:" (openai/finish-reason response))
        (if (openai/finish-reason-tool-calls? response)
            ;; (println "Message:" (openai/pretty-print-json message))
            (let [fn-map  (openai-demo/demo-weather-function-map)
                  results (openai/exec-fn response fn-map)]
              (println "\nFn results:" (pr-str results)))
          (println "Message:" (:content message))))
      (println "Error:" (-> (:data response)
                            (openai/pretty-print-json))))))
```

Response:

```
Finish Reason: tool_calls
Calling "get-current-weather" with location="Glasgow", format="celsius"

Fn results: [{:ok "The current weather in Glasgow is sunny at 16°C"}]
```


### Full weather example with supplied functions (version 2)

The first attempt is not really satisfying. We actually want the model to answer the 
question. It should use the results from the functions as additional knowledge 
helping answering questions.

To achieve this, we need to feed back the results from the questions into the model's 
context and prompt it again with the knowledge enriched context.

The second knowledge enhanced prompt will look like:

```
[ { :role     "system"
    :content  "The current weather in Glasgow is sunny at 16°C." }
  { :role     "user"
    :content  "What is the current weather in Glasgow? Give the temperature in Celsius." } ]
```

In version 1 the function call has to deal itself with Celsius / Fahrenheit conversion,
whereas in version 2 the function can return temperatures in Celsius always and the
OpenAI model can convert to Fahrenheit if asked from the user.

You can easily test this by replacing the prompt in the Phase 2 with

```
[ { :role     "system"
    :content  "The current weather in Glasgow is sunny at 16°C." }
  { :role     "user"
    :content  "What is the current weather in Glasgow? Give the temperature in Fahrenheit." } ]
    
==> "The current weather in Glasgow is sunny at 60.8°F."
```

and the OpenAI model does the conversion. Or you can even ask for a final translation to German.

```
[ { :role     "system"
    :content  "The current weather in Glasgow is sunny at 16°C." }
  { :role     "user"
    :content  """
              What is the current weather in Glasgow? Give the temperature in Fahrenheit.
              Translate the answer to German!
              """ } ]
              
==> "Die aktuelle Wetterlage in Glasgow ist sonnig bei 60,8°F."
```



*Note: for simplicity reasons this example just handles the happy path!*

```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)
  
  ;; Phase #1: prompt the model and call the functions
  (println "Phase #1")
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
        response  (openai/chat-completion [ prompt-sys prompt-usr ] 
                                          :model "gpt-4"
                                          :tools (openai-demo/demo-weather-function-defs)
                                          :prompt-opts { :temperature 0.1 })]
    (assert (= (:status response) 200))
    (let [response (:data response)
          message  (openai/extract-response-message response)]
      (assert (openai/finish-reason-tool-calls?  response))
      (let [fn-map  (openai-demo/demo-weather-function-map)
            results (openai/exec-fn response fn-map)
            answer  (:ok (first results))]
        (println "Fn call result:" (pr-str answer))
        
        ;; Phase #2: prompt the model again with enhanced knowledge
        (println "\nPhase #2")
        (println "Using the knowledge fact \"~{answer}\" in the 2nd prompt")
        (let [response  (openai/chat-completion [ { :role "system", :content answer }
                                                  prompt-usr ] 
                                                :model "gpt-4"
                                                :prompt-opts { :temperature 0.1 })]
          (assert (= (:status response) 200))
          (let [response (:data response)
                content  (openai/extract-response-message-content response)]
            (assert (openai/finish-reason-stop?  response))
            (println "\nFinal answer: ~(pr-str content)")))))))
```

Response:

```
Final answer: "The current weather in Glasgow is sunny and the temperature is 16°C."
```



## How to call functions with model generated arguments

In the following examples we'll use the OpenAI chat completion API to answer questions
about a database. For simplicity the Chinook sample database will be used. See 
[Venice and Chinook Dataset](database.md#chinook-dataset-overview)

The OpenAI model shall be enabled to answer questions like: *Who are the top 5 artists by number of tracks?*

Before starting, follow the [Venice Database tutorial](database.md) to:

 1. Install the PostgreSQL JDBC driver
 2. Start a PostgreSQL Docker Instance
 3. Load the Chinook dataset into the PostgreSQL database
 
All these tasks can be run from a Venice REPL.









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


