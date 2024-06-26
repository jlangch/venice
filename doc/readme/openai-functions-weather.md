# Weather Example


This example demonstrates how to execute functions whose inputs are model-generated and deliver 
the required knowledge to the model for answering questions



## Oveview

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

   Venice provides the `(openai/exec-fn response fn-map)` function to call 
   the requested function based on the model's *response* and a function
   mapping dictionary *fn-map*.
   

4. The function returns a JSON object with the current Glasgow weather data

   Function arguments (a Venice map):
   
   ```clojure
   { 
     "location" "Glasgow",
     "format"   "celsius"
   }
   ```
   
   Function return value (JSON):
   
   ```json
   { 
     "location":    "Glasgow",
     "format":      "celsius",
     "general":     "sunny",
     "temperature": "16.0"
   }
   ```
   
   Note: The function responds with an error in JSON if there is no data for a location:
   
   ```json
   { 
     "location": "London",
     "error":    "No weather data available for London!"
   }
   ```   

5. The client adds an additional prompt message with the function's JSON response data and asks the model again

6. The model has now all information and returns the final answer


**Weather function implementation**


The weather function map maps the OpenAI function names to the Venice functions:

```clojure
(defn weather-function-map []
  { "get_current_weather"   get-current-weather } )
```

The simplified weather data function is defined as:

```clojure
(defn get-current-weather 
  ([dictionary] 
    ;; dispatches a dictionary of arguments to a two-arg function call
    (get-current-weather (get dictionary "location") 
                         (get dictionary "format")))

  ([location format]
    (case location
      "Glasgow"    (json/write-str
                     { :location    location
                       :format      format
                       :temperature 16.0
                       :general     "sunny" })
                       
      ;; else (unknown location)
      (json/write-str { :location location
                        :error    "No weather data available for ~{location}!" }))))
```


## Full Example

*Note: for simplicity this example just handles the happy path!*

Run this code in a REPL:

```clojure
(do
  (load-module :openai)
  
  
  ;; Returns a Venice data map with the OpenAI demo function definitions, that corresponds 
  ;; to the OpenAI chat completion request 'tools' JSON data.
  ;; For better readability the map keys are Venice keywords. Strings would work equally well.
  (defn weather-function-defs [] 
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


  ;; Maps the OpenAI function names to Venice functions
  (defn weather-function-map []
    { "get_current_weather"        get-current-weather
      "get_n_day_weather_forecast" get-n-day-weather-forecast } )


  (defn get-current-weather 
    ([dictionary] 
      (assert map? dictionary)
      ;; argument dispatching
      (get-current-weather (get dictionary "location") 
                           (get dictionary "format")))

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

        ;; else (unknown location)                         
        (json/write-str { :location location
                          :error    "No weather data available for ~{location}!" }))))


  (defn get-n-day-weather-forecast 
    ([dictionary]
      (assert map? dictionary)
      ;; argument dispatching
      (get-n-day-weather-forecast (get dictionary "location")
                                  (get dictionary "format")
                                  (get dictionary "n_days")))

    ([location format n-days]
      (println """
              Calling fn "get-current-weather" with \
              location="~{location}", format="~{format}", n-days="~{n-days}"
              """)
      (case location
        "Glasgow"             (json/write-str
                                { :location    location
                                  :format      format
                                  :n_days      n-days
                                  :temperature (temperature 16 format)
                                  :general     "mostly sunny" })

        "San Francisco, CA"   (json/write-str
                                { :location    location
                                  :format      format
                                  :n_days      n-days
                                  :temperature (temperature 12 format)
                                  :general     "mostly rainy" })
        
        ;; else (uknown location)
        (json/write-str { :location location
                          :error    "No weather data available for ~{location}!" }))))


  (defn- temperature [t format]
    (str/format "%#.1f" (if (fahrenheit? format) (celsius-to-fahrenheit t) (double t))))

  (defn- fahrenheit? [format]
    (str/equals-ignore-case? format "fahrenheit"))

  (defn- celsius-to-fahrenheit [c]
    (-> (double c) (* 9) (/ 5) (+ 32)))
  
  
  ;; Running the example
  
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
                                          :tools (weather-function-defs)
                                          :chat-opts { :temperature 0.1 })]
    (openai/assert-response-http-ok response)
    (let [response (:data response)]  
      ;; [2] The model returns a function call request   
      (assert (openai/chat-finish-reason-tool-calls?  response))
     
      ;;(println "Message:" (->> (openai/chat-extract-response-message response)
      ;;                         (openai/pretty-print-json)))
     
      ;; [3] Call the requested function (openai/exec-fn ...)
      (println "\nPhase #2: call the requested function")
      (let [fn-map  (weather-function-map)
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
                                                :chat-opts { :temperature 0.1 })]
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

the model responds without hallucinating:

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
        response    (openai/chat-completion prompt 
                                            :model "gpt-4"
                                            :tools (openai-demo/demo-weather-function-defs)
                                            :chat-opts { :temperature 0.1 }
                                            :debug true)]         ;; <======= DEBUGGING ON
    (openai/assert-response-http-ok response)
    (println "Response:" (-> (:data response) 
                             (openai/pretty-print-json)))))
```

