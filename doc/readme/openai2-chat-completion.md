# Chat Completion

 

* [Completions](#completions)
* [Conversations](#conversations)
* [Functions](#functions)
* [Structured Output](#structured-output)
* [File Completions](#file-completions)

 
 
 

## Completions

 

### Examples

#### Example: Counting numbers (full model response)

``` clojure
"""
(do
  (load-module :openai-java)
  (let [client   (openai-java/client)
        chat     (-> (openai-java/chat-completion client :GPT_5_4)
                     (openai-java/max-completion-tokens 2048)
                     (openai-java/add-user-message 
                         """
                         Count to 10, with a comma between each number "
                         and no newlines. E.g., 1, 2, 3, ...
                         """ ))
        response (openai-java/execute chat)
        elapsed  (openai-java/elapsed chat)
        usage    (openai-java/usage response)
        msg      (first (openai-java/messages response))]
    (printf "Elapsed: %dms%n%n" elapsed)
    (printf "Tokens:  %n%s%n" (openai-java/format-usage usage "  "))
    (printf "Result:  %n%s%n" msg)))
```

Answer:

```
Elapsed: 2968ms

Tokens:  
  Input:     36
  Output:    31 (Reasoning: 0)
  Total:     67

Result:  
1, 2, 3, 4, 5, 6, 7, 8, 9, 10
```

 

#### Example: Text correction

``` clojure
(do
  (load-module :openai-java)
  (let [client   (openai-java/client)
        chat     (-> (openai-java/chat-completion client :GPT_5_4)
                     (openai-java/max-completion-tokens 2048)
                     (openai-java/add-user-message 
                         """
                         Convert the following text to standard english:
               
                         She no went to the market.
                         """ ))
        response (openai-java/execute chat)
        msg      (first (openai-java/messages response))]
    (println msg)))
```

Answer:

```
She did not go to the market.
```

 

#### Example: Text data extraction

``` clojure
(do
  (load-module :openai-java)
  (let [client   (openai-java/client)
        chat     (-> (openai-java/chat-completion client :GPT_5_4)
                     (openai-java/max-completion-tokens 2048)
                     (openai-java/add-user-message 
                         """
                         Please extract the following information from the given text and 
                         return it as a JSON:

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
                         """ ))
        response (openai-java/execute chat)
        msg      (first (openai-java/messages response))]
    (println msg)))
```

Answer:

```
{
  "name": "Peter Kilmore",
  "major": "computer science",
  "school": "Stanford University",
  "grades": "3.8 GPA",
  "club": "Robotics Club"
}
```

 

#### Example: Chain of Thought Prompting

Chain of thought (CoT) is a method that encourages Large Language Models (LLMs) to explain their reasoning. It advices the model to not only seeking an answer but also requiring it to explain its steps to arrive at that answer. CoT can improve the quality of the answer in case the models fails otherwise. 


##### Prompt 1a


``` clojure
(do
  (load-module :openai-java)
  (let [client   (openai-java/client)
        chat     (-> (openai-java/chat-completion client :GPT_5_4)
                     (openai-java/max-completion-tokens 2048)
                     (openai-java/add-user-message 
                         """
                         I am looking for a name for my new pet, a cat. The cat's fur 
                         is reddish and light tabby. Suggest me 5 names that I could 
                         give my cat.
                         """ ))
        response (openai-java/execute chat)
        msg      (first (openai-java/messages response))]
    (println msg)))
```

Answer:


Here are 5 name ideas for your reddish, light tabby cat:

1. **Rusty** – great for a warm reddish coat.  
2. **Amber** – soft and pretty, inspired by golden-red tones.  
3. **Maple** – cozy and autumn-like.  
4. **Sunny** – bright and cheerful for a light-colored cat.  
5. **Tiger** – a fun choice for a tabby cat with stripes.

If you want, I can also suggest names that are **cute**, **elegant**, or **gender-specific**.



##### Prompt 1b (with explanation)

``` clojure
(do
  (load-module :openai-java)
  (let [client   (openai-java/client)
        chat     (-> (openai-java/chat-completion client :GPT_5_4)
                     (openai-java/max-completion-tokens 2048)
                     (openai-java/add-user-message 
                         """
                         I am looking for a name for my new pet, a cat. The cat's fur 
                         is reddish and light tabby. Suggest me 5 names that I could 
                         give my cat.
                
                         Explain why you have chosen these names.
                         """ ))
        response (openai-java/execute chat)
        msg      (first (openai-java/messages response))]
    (println msg)))
```

Answer:


Here are 5 name ideas for your reddish, light-tabby cat, with why each one fits:

1. **Amber**  
   This name suits a cat with warm reddish-golden tones. Amber is the color of golden resin, so it matches a soft, glowing coat very well.

2. **Rusty**  
   A classic and friendly name for a cat with reddish fur. It highlights the rusty-orange shade in a playful and easy-to-remember way.

3. **Maple**  
   Maple brings to mind warm autumn colors like red, orange, and gold. It feels gentle and cozy, which can be perfect for a light tabby cat.

4. **Sunny**  
   Since your cat has light, warm coloring, Sunny gives a cheerful and bright impression. It’s a cute name for a cat with a lively or sweet personality.

5. **Tiger**  
   Because tabby cats often have striped patterns, Tiger is a fun choice that draws attention to those markings. It works especially well if your cat has a bold or energetic look.

If you want, I can also suggest:
- more cute names,
- more elegant names,
- or male/female-specific cat names.



##### Prompt 2 (solving puzzle / reasoning)

``` clojure
(do
  (load-module :openai-java)
  (let [client   (openai-java/client)
        chat     (-> (openai-java/chat-completion client :GPT_5_4)
                     (openai-java/max-completion-tokens 2048)
                     (openai-java/add-user-message 
                         """
                         A farmer with a wolf, a goat, and a cabbage must cross a river 
                         with a boat. The boat can carry only the farmer and a single item.
                         If left unattended together, the wolf would eat the goat or the
                         goat would eat the cabbage. How can they cross the river without
                         anything being eaten? 
                  
                         Describe your reasoning step by step.
                         """ ))
        response (openai-java/execute chat)
        msg      (first (openai-java/messages response))]
    (println msg)))
```

Answer:


Here’s the step-by-step solution:

1. **Take the goat across**  
   - Farmer takes the **goat** to the other side.  
   - Left behind: **wolf** and **cabbage**  
   - This is safe, because the wolf does not eat the cabbage.

2. **Farmer returns alone**  
   - Farmer goes back by himself.  

3. **Take the wolf across**  
   - Farmer takes the **wolf** to the other side.  
   - Now the **wolf** and **goat** would be together, which is not safe, so:

4. **Bring the goat back**  
   - Farmer brings the **goat** back to the starting side.  
   - Left on the far side: **wolf** alone.

5. **Take the cabbage across**  
   - Farmer takes the **cabbage** to the other side.  
   - Left behind on the starting side: **goat** alone.  
   - On the far side: **wolf** and **cabbage**, which is safe.

6. **Farmer returns alone**  
   - Farmer goes back by himself.

7. **Take the goat across again**  
   - Farmer takes the **goat** across.  

Now all three—the **wolf, goat, and cabbage**—are safely across the river.

**Final crossing order:**

*Goat over → Farmer back → Wolf over → Goat back → Cabbage over → Farmer back → Goat over*

 
 

## Conversations


#### Example: Conversation

Chat conversation with multiple questions and answers.

``` clojure
;; conversation
(do
  (load-module :openai-java)
  (let [client   (openai-java/client)
        chat     (-> (openai-java/chat-completion client :GPT_5_4)
                     (openai-java/max-completion-tokens 2048)
                     (openai-java/add-user-message "Say Hello!"))
        response (openai-java/execute chat)]
    (println (first (openai-java/messages response)))

    ;; 1st follow up question
    (openai-java/add-assistant-message chat (openai-java/messages response))      
    (openai-java/add-user-message chat "Can you say it more informal?")
    (let [response (openai-java/execute chat)]
      (println (first (openai-java/messages response))))

      ;; 2nd follow up question
      (openai-java/add-assistant-message chat (openai-java/messages response))      
      (openai-java/add-user-message chat "Can you say it very formal?")
      (let [response (openai-java/execute chat)]
        (println (first (openai-java/messages response))))))
```

Answer:

```
Hello!
Hey
Greetings
```

 
 

## Functions


This example demonstrates how to execute functions whose inputs 
are model-generated and deliver the required knowledge to the model 
for answering questions.


A full weather example. It answers questions like *"What is the weather in Zurich in Celsius?"*:

#### Example 1

``` clojure
(do
  (load-module :openai-java)

  (defn celsius-to-fahrenheit [c]
    (-> (double c) (* 9) (/ 5) (+ 32) (long)))  ;; (c * 9) / 5 + 32

  (defn degrees [t unit]
    (if (str/equals-ignore-case? unit "celsius") t (celsius-to-fahrenheit t)))

  (defn get-weather [fnArgsJson]
    (let [args     (json/read-str fnArgsJson)
          location (get args "location")
          unit     (get args "unit")]
      (cond                 ;; we just support one hard coded location
        (str/contains? location "Zurich")
          (json/write-str { :location    location
                            :unit        unit
                            :temperature (degrees 21 unit)
                            :conditions  "Mostly sunny" })
        :else
          (json/write-str { :location location
                            :error    (str "No weather data available for " location "!")}))))

  (let [client   (openai-java/client)
        registry (-> (openai-java/create-function-registry)
                     (openai-java/register-function "GetWeather"
                                                    get-weather))
        chat     (-> (openai-java/chat-completion client :GPT_5_4 registry)
                     (openai-java/max-completion-tokens 2048)
                     (openai-java/add-function 
                          "GetWeather"
                          "Gets the current weather for a city."
                          { :location { 
                              :type "string" 
                              :description "A city, e.g.: Zurich" }
                            :unit { 
                              :type "string"
                              :description "Temperature unit: celsius or fahrenheit" } } 
                          '("location" "unit"))
                     (openai-java/add-user-message "What is the weather in Zurich in Celsius?"))
        response (openai-java/execute chat)]
    (println (coalesce (first (openai-java/messages response)) "<no message>\n"))

    ;; required follow up question with the function output on the weather in Zurich in the context
    (openai-java/add-assistant-message chat (openai-java/messages response))      
    (openai-java/add-user-message chat "What is the weather in Zurich in Celsius?")
    (let [response (openai-java/execute chat)]
      (println (first (openai-java/messages response))))))
```

Response

```
Zurich: 21°C, mostly sunny.
```


#### Example 2

``` clojure
(do
  (load-module :openai-java)

  (defn celsius-to-fahrenheit [c]
    (-> (double c) (* 9) (/ 5) (+ 32) (long)))  ;; (c * 9) / 5 + 32

  (defn degrees [t unit]
    (if (str/equals-ignore-case? unit "celsius") t (celsius-to-fahrenheit t)))

  (defn get-weather [fnArgsJson]
    (let [args     (json/read-str fnArgsJson)
          location (get args "location")
          unit     (get args "unit")]
      (cond                 ;; we just support one hard coded location
        (str/contains? location "Zurich")
          (json/write-str { :location    location
                            :unit        unit
                            :temperature (degrees 21 unit)
                            :conditions  "Mostly sunny" })
        :else
          (json/write-str { :location location
                            :error    (str "No weather data available for " location "!")}))))

  (let [client   (openai-java/client)
        registry (-> (openai-java/create-function-registry)
                     (openai-java/register-function "GetWeather"
                                                    get-weather))
        chat     (-> (openai-java/chat-completion client :GPT_5_4 registry)
                     (openai-java/max-completion-tokens 2048)
                     (openai-java/add-function 
                          "GetWeather"
                          "Gets the current weather for a city."
                          { :location { 
                              :type "string" 
                              :description "A city, e.g.: Zurich" }
                            :unit { 
                              :type "string"
                              :description "Temperature unit: celsius or fahrenheit" } } 
                          '("location" "unit"))
                     (openai-java/add-user-message "What is the weather in Zurich in Celsius?"))
        response (openai-java/execute chat)]
    (println (coalesce (first (openai-java/messages response)) "<no message>\n"))

    ;; follow up question with the function output in the context
    (openai-java/add-assistant-message chat (openai-java/messages response))      
    (openai-java/add-user-message chat 
        "Please give the current weather in Zurich and some ideas what to do in Zurich on day like this!")
    (let [response (openai-java/execute chat)]
      (println (first (openai-java/messages response))))))
```

Answer:

```
Current weather in Zurich: 21°C and mostly sunny.

Ideas for a day like this in Zurich:
- Walk along Lake Zurich or the Limmat River
 - Explore the Old Town (Altstadt)
- Sit at an outdoor café and enjoy the sunshine
- Visit Lindenhof for a nice city view
- Take a boat cruise on Lake Zurich
- Go up to Uetliberg for an easy hike and panoramic views
- Have a picnic in a park like Zürichhorn
- Visit the Bahnhofstrasse area for shopping and strolling

If you want, I can also suggest a full half-day or full-day Zurich itinerary based on this weather.
```

 
 

## Structured Output

``` clojure
(do
  (load-module :openai-java)
  (let [client   (openai-java/client)
        chat     (-> (openai-java/chat-completion client :GPT_5_4)
                      (openai-java/max-completion-tokens 2048)
                      (openai-java/json-response-format 
                          "employee-list"
                          "A list of employees"
                          "object"
                          { :employees { :items  { "type" "string" }}} )
                      (openai-java/add-user-message "Who works at OpenAI?"))
        response (openai-java/execute chat)
        msg      (first (openai-java/messages response))]
    (println (first (openai-java/messages response)))))
```

Answer:

```
{"employees":["Sam Altman","Mira Murati","Greg Brockman","Ilya Sutskever","Wojciech Zaremba","Jakub Pachocki","Kevin Weil","Brad Lightcap","Sarah Friar"]}
```

 
 

## File Completions


``` clojure
          (do
            (load-module :openai-java)
            
            (let [client   (openai-java/client)
                  file-obj (openai-java/create-file-object 
                                          client 
                                          (io/file "/Users/foo/Desktop/Tour_Eiffel.pdf")
                                          :USER_DATA
                                          3600)
                  chat     (-> (openai-java/chat-completion client :GPT_5_4)
                               (openai-java/max-completion-tokens 2048)
                               (openai-java/add-user-message-with-files 
                                    "Describe this image"
                                    file-obj))
                  response (openai-java/execute chat)
                  elapsed  (openai-java/elapsed chat)
                  usage    (openai-java/usage response)
                  msg      (first (openai-java/messages response))]
              (printf "Elapsed: %dms%n%n" elapsed)
              (printf "Tokens:  %n%s%n" (openai-java/format-usage usage "  "))
              (printf "Result:  %n%s%n" msg)))
```

Answer:

```
Elapsed: 4909ms

Tokens:  
  Input:    499
  Output:    79 (Reasoning: 0)
  Total:    578

Result:  
The image shows the Eiffel Tower centered in the frame, viewed from the front 
on a clear day. It rises above a wide green lawn, with rows of trees on both 
sides leading toward the tower. The sky is bright and cloudless blue, and 
buildings can be seen in the distance beneath the tower’s arch. The composition 
is symmetrical, emphasizing the tower’s height and structure.
```