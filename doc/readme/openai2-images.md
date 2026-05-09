# Images

 

* [Create Image](#create-image)
* [Edit Image](#edit-image)

 
 

## Create Image

```clojure
(do
  (load-module :openai-java)
  
  (let [client   (openai-java/client)
        response (openai-java/generate-image 
                      client 
                      :GPT_IMAGE_1_MINI
                      "A cat on bicycle"
                      :format :PNG
                      :number 1
                      :size "1024x1024")
        usage    (openai-java/usage response)
        image-1  (first (openai-java/image-binaries response))]
    (printf "Tokens:  %n%s%n" (openai-java/format-usage usage "  "))
    (io/spit (io/file "./image-1.png") image-1)
    (println "Image #1 saved to ./image-1.png")))
```

 

## Edit Image

```clojure
(do
  (load-module :openai-java)

  (let [client       (openai-java/client)
        response-gen (openai-java/generate-image 
                        client 
                        :GPT_IMAGE_1_MINI
                        "A cute baby sea otter"
                        :format :PNG
                        :number 1
                        :size "1024x1024")
        usage-gen    (openai-java/usage response-gen)
        image-1-gen  (first (openai-java/image-binaries response-gen))]
    (printf "Tokens:  %n%s%n" (openai-java/format-usage usage-gen "  "))
    (io/spit (io/file "./sea-otter.png") image-1-gen)
    (println "Image #1 saved to ./sea-otter.png")

    (println)
    (println "Editing image...")
    (let [response-edt (openai-java/edit-image 
                          client 
                          (io/bytebuf-in-stream image-1-gen)
                          "image/png"
                          "sea-otter.png"
                          :GPT_IMAGE_1_MINI
                          "A cute baby sea otter wearing a beret"
                          :format :PNG
                          :number 1
                          :quality :HIGH
                          :size "1024x1024")
          usage-edt    (openai-java/usage response-edt)
          image-1-edt  (first (openai-java/image-binaries response-edt))]
      (printf "Tokens:  %n%s%n" (openai-java/format-usage usage-edt  "  "))
      (io/spit (io/file "./sea-otter-edited.png") image-1-edt)
      (println "Image #1 saved to ./sea-otter-edited.png"))))
```
