# Functions

 

This example demonstrates how to execute functions whose inputs 
are model-generated and deliver the required knowledge to the model 
for answering questions.


A full example. It answers questions like *"What is the weather in Zurich in Celsius?"*:

``` clojure
(do
  (load-module :openai-java)

  (defn celsius-to-fahrenheit [c]
    (-> (double c) (* 9) (/ 5) (+ 32) (long)))

  (defn degrees [t unit]
    (if (str/equals-ignore-case? unit "celsius") t (celsius-to-fahrenheit t)))

  ;; The local implementation of the weather function
  ;; The 1-arity variant ([named-args] ..) is called by OpenAI, unpacks the 
  ;; arguments 'location' and 'unit' and dispatches to the implementation  
  ;; variant ([location unit] ...) 
  (defn get-weather 
    ([named-args] 
      (get-weather (get named-args "location") 
                   (get named-args "unit")))

    ([location unit]
      (cond 
        (str/contains? location "Zurich")
          (json/write-str { :location    location
                            :unit        unit
                            :temperature (degrees 21 unit)
                            :conditions  "Mostly sunny" })
        :else
          (json/write-str { :location location
                            :error    "No weather data available ~{location}!" } ))))

  ;; Maps a function names known to OpenAI to Venice functions
  (defn function-mapper [fn-name]
    (case fn-name
      "GetWeather"   get-weather 
      ;; add more mappings here
      nil))

  (let [client    (openai-java/client)
        prompt    "What is the weather in Zurich in Celsius?"
                  ;; Define the function meta data for OpenAI
        functions [ { :name "GetWeather"
                      :description "Gets the current weather for a city."
                      :properties  { 
                          :location { 
                              :type "string" 
                              :description (str "City and country, for example: "
                                                "Zurich, Switzerland")  
                          }
                          :unit { 
                              :type "string"
                              :description (str "Temperature unit: celsius or fahrenheit. "
                                                "Infer this from the user's location when "
                                                "missing.") 
                          } 
                      }
                      :required [ "location" "unit" ] 
                    } 
                  ]
        result    (openai-java/chat-completion-fn client 
                                                  prompt 
                                                  functions        ;; the function's meta data
                                                  function-mapper  ;; the mapper to function's implementation
                                                  :model :GPT_5_4)
        response  (:response result)
        usage     (:usage result)
        status    (:status result)
                  ;; just the first message without status
        msg       (first (openai-java/response-messages-without-status response))]

    (printf "Elapsed: %dms%n%n" (:elapsed result))
    (printf "Status: %s%n%n" (name status))
    (printf "Tokens: %n%s%n" (openai-java/format-usage usage "  "))
    (printf "Result: %n%s%n" msg)))
```

