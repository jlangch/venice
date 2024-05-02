# OpenAI Functions Cookbook

The example is adapted from the [OpenAI API Functions Cookbook](https://cookbook.openai.com/examples/how_to_call_functions_with_chat_models) to demonstrate how OpenAI functions can be used with *Venice*.

This tutorial covers how to use the Chat Completions API in combination with external functions to extend the capabilities of GPT models.

`tools` is an optional parameter in the Chat Completion API which can be used to provide function specifications. The purpose of this is to enable models to generate function arguments which adhere to the provided specifications. Note that the API will not actually execute any function calls. It is up to developers to execute function calls using model outputs.

Within the tools parameter, if the functions parameter is provided then by default the model will decide when it is appropriate to use one of the functions. The API can be forced to use a specific function by setting the tool_choice parameter to `{"type": "function", "function": {"name": "my_function"}}`. The API can also be forced to not use any function by setting the tool_choice parameter to `"none"`. If a function is used, the output will contain `"finish_reason": "tool_calls"` in the response, as well as a tool_calls object that has the name of the function and the generated function arguments.

## Overview

This notebook contains the following 2 sections:

* **How to generate function arguments:** Specify a set of functions and use the API to generate function arguments.
* **How to call functions with model generated arguments:** Close the loop by actually executing functions with model generated arguments.

## How to generate function arguments

### Basic concepts

Let's create some function specifications to interface with a hypothetical weather API. We'll pass these function specification to the Chat Completions API in order to generate function arguments that adhere to the specification.

```json
tools = [
    {
        "type": "function",
        "function": {
            "name": "get_current_weather",
            "description": "Get the current weather",
            "parameters": {
                "type": "object",
                "properties": {
                    "location": {
                        "type": "string",
                        "description": "The city and state, e.g. San Francisco, CA",
                    },
                    "format": {
                        "type": "string",
                        "enum": ["celsius", "fahrenheit"],
                        "description": "The temperature unit to use. Infer this from the users location.",
                    },
                },
                "required": ["location", "format"],
            },
        }
    },
    {
        "type": "function",
        "function": {
            "name": "get_n_day_weather_forecast",
            "description": "Get an N-day weather forecast",
            "parameters": {
                "type": "object",
                "properties": {
                    "location": {
                        "type": "string",
                        "description": "The city and state, e.g. San Francisco, CA",
                    },
                    "format": {
                        "type": "string",
                        "enum": ["celsius", "fahrenheit"],
                        "description": "The temperature unit to use. Infer this from the users location.",
                    },
                    "num_days": {
                        "type": "integer",
                        "description": "The number of days to forecast",
                    }
                },
                "required": ["location", "format", "num_days"]
            },
        }
    },
]
```


*work in progress...*

