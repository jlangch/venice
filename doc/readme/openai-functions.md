# OpenAI Functions Cookbook

The example is adapted from the [OpenAI API Functions Cookbook](https://cookbook.openai.com/examples/how_to_call_functions_with_chat_models) to demonstrate how OpenAI functions can be used with *Venice*.

This tutorial covers how to use the Chat Completions API in combination with external functions to extend the capabilities of GPT models.

`tools` is an optional parameter in the Chat Completion API which can be used to provide function specifications. The purpose of this is to enable models to generate function arguments which adhere to the provided specifications. Note that the API will not actually execute any function calls. It is up to developers to execute function calls using model outputs.

Within the tools parameter, if the functions parameter is provided then by default the model will decide when it is appropriate to use one of the functions. The API can be forced to use a specific function by setting the tool_choice parameter to `{:type "function", :function {:name "my_function"}}`. The API can also be forced to not use any function by setting the tool_choice parameter to `"none"`. If a function is used, the output will contain `"finish_reason": "tool_calls"` in the response, as well as a tool_calls object that has the name of the function and the generated function arguments.

## Overview

This notebook contains the following 2 sections:

* **How to generate function arguments:** Specify a set of functions and use the API to generate function arguments.
* **How to call functions with model generated arguments:** Close the loop by actually executing functions with model generated arguments.

## How to generate function arguments

### Basic concepts

Let's create some function specifications to interface with a hypothetical weather API. We'll pass these function specification to the Chat Completions API in order to generate function arguments that adhere to the specification.

The function definitions are passed in as `tools parameter to the OpenAI request:


If we prompt the model about the current weather, it will respond with some clarifying questions.

```clojure
(do
  (load-module :openai)
  
  (defn weather-tools [] 
    ;; A Venice collection that can be converted 1:1 to JSON
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
      }
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
                                            :tools (weather-tools)
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

```
Status:   200
Mimetype: application/json
Message: Sure, I can help with that. Could you please tell me your location?
```

Once we provide the missing information, it will generate the appropriate function 
arguments for us.

```clojure
(do
  (load-module :openai)
  
  (defn weather-tools [] 
    ;; A Venice collection that can be converted 1:1 to JSON
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
      }
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
                                            :tools (weather-tools)
                                            :prompt-opts prompt-opts)]
    (println "Status:  " (:status response))
    (println "Mimetype:" (:mimetype response))
    (if (=  (:status response) 200)
      (println "Message:" (-> (:data response)                            
                              (openai/pretty-print-json)))
      (println "Error:"   (:data response)))))
```

```json
Message: {
  "created": 1714674616,
  "usage": {
    "completion_tokens": 28,
    "prompt_tokens": 222,
    "total_tokens": 250
  },
  "model": "gpt-4-0613",
  "id": "chatcmpl-9KVU85aY4qIxgYvkVUdWVvQWngT2f",
  "choices": [{
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
  }],
  "system_fingerprint": null,
  "object": "chat.completion"
}
```


