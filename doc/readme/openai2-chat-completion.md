# Chat Completion


* [Completion](#completion)
    * [Sending Requests](#sending-requests)
    * [Examples](#examples)
        * [Example: Counting numbers (full model response)](#example-counting-numbers-full-model-response))

        * [Example: Text correction](#example-text-correction)
        * [Example: Text data extraction](#example-text-data-extraction)
        * [Example: Chain of Thought Prompting](#example-chain-of-thought-prompting)
   
        
## Completion


### Sending Requests

`(chat-completion client prompt & options)`

Send a chat completion request given an OpenAI client, a prompt and options.

The OpenAI api key can be provided in an environment variable "OPENAI_API_KEY" or
explicitly passed as an option `:openai-api-key "sk-xxxxxxxxxxxxx"`.

To run the request asynchronously just wrap it in a `future` and
deref it, when the result is required.


#### Parameter «prompt»

A prompt

```
"Who won the world series in 2020?"
```



#### Parameter «options»

| Option              | Description |
| :---                | :---        |
| :model m            | An OpenAI model. E.g.: :GPT_5_4, :GPT_5_4_MINI, :GPT_5_4_NANO.<br>Defaults to :GPT_5_4|
| :reasoning-effort e | The reasoning.effort parameter guides the model on how much to think when performing a task.<br>Values: :NONE, :MINIMAL, :LOW, :MEDIUM, :HIGH, :XHIGH<br>Default is model dependent|


 
#### Return value

*Returns a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The response status { :completed, :failed, :in_progress, :cancelled, :queued, :incomplete, :unknown }  |
| :elapsed   | The elapsed milliseconds |
| :response  | The original OpenAI response, a Java object of type `:com.openai.models.responses.Response`<br>Response helpers:<br>* `response-messages-without-status`<br>* `response-messages-with-status` |
| :usage     | The usage data. E.g.:<br>{<br>\u00A0\u00A0:input-tokens 11<br>  :output-tokens 8<br>  :output-tokens-detail {<br>    :reasoning-tokens 0 }<br>  :total-tokens 19<br>} |


### Examples

#### Example: Counting numbers (full model response)

``` clojure
(do
  (load-module :openai-java)
  (let [client   (openai-java/client)
        prompt   (str "Count to 10, with a comma between each number "
                      "and no newlines. E.g., 1, 2, 3, ...")
        result   (openai-java/chat-completion client prompt :model :GPT_5_4)
        response (:response result)
        usage    (:usage result)
        status   (:status result)
                 ;; just the first message without status
        msg      (first (openai-java/response-messages-without-status response))]
    (printf "Elapsed: %dms%n%n" (:elapsed result))
    (printf "Status:  %s%n%n" (name status))
    (printf "Tokens:  %n%s%n" (openai-java/format-usage usage "  "))
    (printf "Result:  %n%s%n" msg)))
```

Answer:

```
Elapsed: 4800ms

Status:  completed

Tokens:  
  Input:     35
  Output:    32 (Reasoning: 0)
  Total:     67

Result:  
1, 2, 3, 4, 5, 6, 7, 8, 9, 10
```

#### Example: Text correction

``` clojure
(do
  (load-module :openai-java)
  (let [client   (openai-java/client)
        prompt   """
                 Convert the following text to standard english:
               
                 She no went to the market.
                 """
        result   (openai-java/chat-completion client prompt :model :GPT_5_4)
        response (:response result)
        msg      (first (openai-java/response-messages-without-status response))]
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
        prompt   """
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
                 """
        result   (openai-java/chat-completion client prompt :model :GPT_5_4)
        response (:response result)
        msg      (first (openai-java/response-messages-without-status response))]
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
        prompt   """
                 I am looking for a name for my new pet, a cat. The cat's fur 
                 is reddish and light tabby. Suggest me 5 names that I could 
                 give my cat.
                 """
        result   (openai-java/chat-completion client prompt :model :GPT_5_4)
        response (:response result)
        msg      (first (openai-java/response-messages-without-status response))]
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
        prompt   """
                 I am looking for a name for my new pet, a cat. The cat's fur 
                 is reddish and light tabby. Suggest me 5 names that I could 
                 give my cat.
                
                 Explain why you have chosen these names.
                 """
        result   (openai-java/chat-completion client prompt :model :GPT_5_4)
        response (:response result)
        msg      (first (openai-java/response-messages-without-status response))]
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
        prompt   """
                 A farmer with a wolf, a goat, and a cabbage must cross a river 
                 with a boat. The boat can carry only the farmer and a single item.
                 If left unattended together, the wolf would eat the goat or the
                 goat would eat the cabbage. How can they cross the river without
                 anything being eaten? 
                  
                 Describe your reasoning step by step.
                 """
        result   (openai-java/chat-completion client prompt :model :GPT_5_4)
        response (:response result)
        msg      (first (openai-java/response-messages-without-status response))]
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

### Final crossing order
**Goat over → Farmer back → Wolf over → Goat back → Cabbage over → Farmer back → Goat over**


