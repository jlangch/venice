# Vision

Demonstrates OpenAI's vision capabilities to understand images.



### Example 1a

```clojure
(do
  (load-module :openai)

  (let [img-url     "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg"
        prompt      [ { :role    "user"
                        :content [ { :type "text"
                                     :text "What’s in this image?" }
                                   { :type "image_url"
                                     :image_url { :url img-url } } ] 
                      } ]
        prompt-opts { :temperature 0.1 
                      :max_tokens 300}
        response    (openai/chat-completion prompt 
                                            :model "gpt-4o" 
                                            :prompt-opts prompt-opts)]
    (openai/assert-response-http-ok response)
    (println "Message:")
    (println (-> (:data response)
                 (openai/chat-extract-response-message-content)
                 (openai/pretty-print-json)))))
```

Message:

```
The image depicts a serene landscape with a wooden boardwalk path leading through a lush, green field. The sky above is mostly clear with some scattered clouds, and the horizon features trees and shrubs. The overall scene is bright and peaceful, suggesting a pleasant day in a natural setting.
```


### Example 1b


```clojure
(do
  (load-module :openai)
  
  (defn download-and-encode-base64 [url]
     (->> (io/download url :binary true)
          (str/encode-base64)))

  (let [img-url     "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg"
        base64-img  (download-and-encode-base64 img-url)
        prompt      [ { :role    "user"
                        :content [ { :type "text"
                                     :text "What’s in this image?" }
                                   { :type "image_url"
                                     :image_url { :url "data:image/jpeg;base64,~{base64-img}" } } ] 
                      } ]
        prompt-opts { :temperature 0.1 
                      :max_tokens 300}
        response    (openai/chat-completion prompt 
                                            :model "gpt-4o" 
                                            :prompt-opts prompt-opts)]
    (openai/assert-response-http-ok response)
    (println "Message:")
    (println (-> (:data response)
                 (openai/chat-extract-response-message-content)
                 (openai/pretty-print-json)))))
```

Message:

```
The image depicts a scenic landscape with a wooden boardwalk path cutting through a lush green field. The sky above is mostly clear with some scattered clouds, and the horizon features trees and shrubs. The overall atmosphere is serene and inviting, suggesting a natural park or a nature reserve.

