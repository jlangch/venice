# Weather Example

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

