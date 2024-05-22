# Vision

Demonstrates OpenAI's vision capabilities to understand images.

The Vision functionality is based on OpenAI's Chat Completion API. Images can be passed
along with the prompt as an additional source of information.


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

*The image depicts a serene landscape with a wooden boardwalk path leading through a lush, green field. The sky above is mostly clear with some scattered clouds, and the horizon features trees and shrubs. The overall scene is bright and peaceful, suggesting a pleasant day in a natural setting.*


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

*The image depicts a scenic landscape with a wooden boardwalk path cutting through a lush green field. The sky above is mostly clear with some scattered clouds, and the horizon features trees and shrubs. The overall atmosphere is serene and inviting, suggesting a natural park or a nature reserve.*


### Example 2

```clojure
(do
  (load-module :openai)

  (let [img-url     "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg"
        prompt      [ { :role    "user"
                        :content [ { :type "text"
                                     :text "What are in these images? Is there any difference between them?" }
                                   { :type "image_url"
                                     :image_url { :url img-url } }
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

*Both images depict a scenic landscape featuring a wooden boardwalk path cutting through a lush green field under a blue sky with scattered clouds. The images appear to be identical, with no noticeable differences between them.*

### Example 3

Controlling the *detail* parameter ("low", "high", "auto")

```clojure
(do
  (load-module :openai)

  (let [img-url     "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg"
        prompt      [ { :role    "user"
                        :content [ { :type "text"
                                     :text "What’s in this image?" }
                                   { :type "image_url"
                                     :image_url { 
                                          :url img-url 
                                          :detail "high" } } ] 
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

*The image depicts a scenic landscape with a wooden boardwalk or pathway leading through a lush, green field. The sky above is mostly clear with some scattered clouds, and the overall atmosphere appears to be calm and serene. The pathway seems to invite viewers to walk through the natural setting, suggesting a peaceful and inviting environment.*


### Example 4

```clojure
(do
  (load-module :openai)

  (let [img-url     "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Wolves_hunting_elk.jpg/1280px-Wolves_hunting_elk.jpg"
        prompt      [ { :role    "user"
                        :content [ { :type "text"
                                     :text """
                                           This is a picture that I want to upload. 
                                           Generate a compelling description in the style of David Attenborough 
                                           that I can upload along with the picture. Only include the narration.
                                           """ }
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

*In the vast, untamed wilderness, a dramatic scene unfolds. Here, on the open plains, a pack of wolves engages in a high-stakes pursuit of two elk. The wolves, masters of strategy and endurance, work in unison, their eyes locked on their quarry. The elk, powerful and agile, rely on their speed and strength to evade the relentless predators. This is a timeless dance of survival, where every move can mean the difference between life and death. In this raw and unforgiving landscape, nature's eternal struggle for survival is laid bare, a testament to the resilience and tenacity of these magnificent creatures.*

