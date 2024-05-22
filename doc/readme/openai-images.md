# Images

[OpenAI Images](https://platform.openai.com/docs/api-reference/images)

* [Image Create](#image-create)
* [Image Variations](#image-variations)
* [Image Edits](#image-edits)


## Image Create

### Sending Requests

`(image-create prompt response-format & options)`

Create images.


#### Parameter «prompt»

```
"A portrait of a dog in a library, Sigma 85mm f/1.4"
```

#### Parameter «response-format»

The format in which the generated images are returned

  * `:url`
  * `:b64_json`

Note: URLs are only valid for 60 minutes after the image has been generated.


#### Parameter «options»

| Option            | Description |
| :---              | :---        |
| :uri              | An OpenAI chat completion URI. E.g.: "https://api.openai.com/v1/images/generations". Defaults  to "https://api.openai.com/v1/images/generations" |
| :model            | An OpenAI model. E.g.: "dall-e-3". Defaults to "dall-e-3".<br>The model can also be passed as a keyword. E.g.: `:dall-e-2`, `:dall-e-3`, ...  |
| :openai-api-key   | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :prompt-opts      | An optional map of OpenAI chat request prompt options Map keys can be keywords or strings.<br>E.g. `{ :style "vivid" :size "1024x1024", :quality "hd" :n 1 }`. <br>See: [OpenAI Request Options](https://platform.openai.com/docs/api-reference/images/create) |
| :debug            | An optional debug flag (true/false). Defaults  to false.<br>In debug mode prints the HTTP request and response data |
 
 
#### Response value

*The return value is a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |

          
See:
 * [OpenAI Image Guide](https://platform.openai.com/docs/guides/images/usage)
 * [OpenAI Image API](https://platform.openai.com/docs/api-reference/images)


#### Example 1: 

Return image as URL (response format `:url`) and print the full OpenAI response message

```clojure
(do
  (load-module :openai)

  (let [prompt    "A portrait of a dog in a library, Sigma 85mm f/1.4"
        response  (openai/image-create prompt 
                                       :url   ;; alternatively use :b64_json
                                       :model :dall-e-3
                                       :prompt-opts {:quality "hd"})]
    (openai/assert-response-http-ok response)
    (println "Response:" (openai/pretty-print-json (:data response)))))
```

#### Example 2: 

Return image as URL (response format `:url`) JSON and save it to a file

```clojure
(do
  (load-module :openai)
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])

  (let [prompt    "A portrait of a dog in a library, Sigma 85mm f/1.4"
        response  (openai/image-create prompt                 ;; generate image
                                      :url
                                      :model :dall-e-3
                                      :prompt-opts {:quality "hd"})]
    (openai/assert-response-http-ok response)
    (let [data       (:data (:data response))
          img-data   (first data) ;; 1st image data
          url        (:url img-data)
          _          (println "Downloading image...")
          img        (openai/image-download url "image-1")    ;; download image URL
          file       (str "./" (:name img))]
      (io/spit file (:data img))
      (println "Saved image to:" file))))                     ;; save image to file
```


#### Example 3: 

Return image as Base64 (response format `:b64_json`) JSON and save it to a file

```clojure
(do
  (load-module :openai)
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])

  (let [prompt    "A portrait of a dog in a library, Sigma 85mm f/1.4"
        response  (openai/image-create prompt          ;; generate image
                                       :b64_json
                                       :model :dall-e-3
                                       :prompt-opts {:quality "hd"})]
    (openai/assert-response-http-ok response)
    (let [data       (:data (:data response))
          img-data   (first data) ;; 1st image data   
          img        (->> (get img-data :b64_json)
                          (str/decode-base64))         ;; base64 image decoding
          file       "./image-2.png"]
      (io/spit file img)                               ;; save image to file
      (println "Saved image to:" file))))
```



## Image Variations

### Sending Requests

`(image-variants image response-format & options)`

Create image variations.


#### Parameter «image»

The image to create variants from.

#### Parameter «response-format»

The format in which the generated images are returned

  * `:url`
  * `:b64_json`

Note: URLs are only valid for 60 minutes after the image has been generated.


#### Parameter «options»

| Option            | Description |
| :---              | :---        |
| :uri              | An OpenAI chat completion URI. E.g.: "https://api.openai.com/v1/images/variations". Defaults  to "https://api.openai.com/v1/images/variations" |
| :model            | n OpenAI model. E.g.: "dall-e-2". Defaults to "dall-e-2".<br>The model can also be passed as a keyword. E.g.: `:dall-e-2`, `:dall-e-3`, ...  |
| :openai-api-key   | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :prompt-opts      | An optional map of OpenAI chat request prompt options Map keys can be keywords or strings.<br>E.g. `{ :style "vivid" :size "1024x1024", :n 1 }`. <br>See: [OpenAI Request Options](https://platform.openai.com/docs/api-reference/images/createVariation) |
| :debug            | An optional debug flag (true/false). Defaults  to false.<br>In debug mode prints the HTTP request and response data |
 
 
#### Response value

*The return value is a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |

          
See:
 * [OpenAI Image Guide](https://platform.openai.com/docs/guides/images/usage)
 * [OpenAI Image API](https://platform.openai.com/docs/api-reference/images)


#### Example: 

```clojure
(do
  (load-module :openai)
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])

  (defn create-image [img-file]
    (println "Requesting image...")
    (let [prompt    "A portrait of a dog in a library, Sigma 85mm f/1.4"
          response  (openai/image-create prompt 
                                         :b64_json            ;; generate image
                                         :model :dall-e-3
                                         :prompt-opts {:size "1024x1024", :quality "hd"})]
      (openai/assert-response-http-ok response)
      (let [data       (:data (:data response))
            img-data   (first data) ;; 1st image data   
            img        (->> (get img-data :b64_json)
                            (str/decode-base64))]
        (io/spit img-file img)
        (println "Saved image to:" img-file))))

  (defn create-image-variant [img-file img-variant-file]
    (println "Requesting image variant...")
    (let [img       (io/slurp img-file :binary true)
          response  (openai/image-variants img  ;; generate image variant
                                           :b64_json
                                           :model :dall-e-3
                                           :prompt-opts {:size "1024x1024", :n 1})]
      (openai/assert-response-http-ok response)
      (let [data       (:data (:data response))
            img-data   (first data) ;; 1st image data   
            img        (->> (get img-data :b64_json)
                            (str/decode-base64))]
        (io/spit "./image-variant-2.png" img)
        (println "Saved variant to:" img-variant-file))))

  (create-image "./image-variant-1.png")            ;; create an image
  (create-image-variant "./image-variant-1.png"
                        "./image-variant-2.png"))   ;; create a variant of the image
```



## Image Edits


### Sending Requests

`(image-edits image mask prompt response-format & options`

Edit an image.


#### Parameter «image»

The image to edit.

#### Parameter «mask»

The mask image.

#### Parameter «prompt»

A text description of the desired image.

#### Parameter «response-format»

The format in which the generated images are returned

  * `:url`
  * `:b64_json`

Note: URLs are only valid for 60 minutes after the image has been generated.


#### Parameter «options»

| Option            | Description |
| :---              | :---        |
| :uri              | An OpenAI chat completion URI. E.g.: "https://api.openai.com/v1/images/edits". Defaults  to "https://api.openai.com/v1/images/edits" |
| :model            | n OpenAI model. E.g.: "dall-e-2". Defaults to "dall-e-2".<br>The model can also be passed as a keyword. E.g.: `:dall-e-2`, `:dall-e-3`, ...  |
| :openai-api-key   | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :prompt-opts      | An optional map of OpenAI chat request prompt options Map keys can be keywords or strings.<br>E.g. `{ :style "vivid" :size "1024x1024", :n 1 }`. <br>See: [OpenAI Request Options](https://platform.openai.com/docs/api-reference/images/createEdit) |
| :debug            | An optional debug flag (true/false). Defaults  to false.<br>In debug mode prints the HTTP request and response data |
 
 
#### Response value

*The return value is a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |

          
See:
 * [OpenAI Image Guide](https://platform.openai.com/docs/guides/images/usage)
 * [OpenAI Image API](https://platform.openai.com/docs/api-reference/images)


#### Example: 

```clojure
(do
  (load-module :openai)
  (load-module :http-client-j8 ['http-client-j8 :as 'hc])
  (load-module :images)

  (defn create-image [prompt img-file]
    (println "Requesting image...")
    (let [response  (openai/image-create prompt               ;; generate image
                                         :b64_json
                                         :model :dall-e-3
                                         :prompt-opts {:size "1024x1024", :quality "hd"})]
      (openai/assert-response-http-ok response)
      (let [data       (:data (:data response))
            img-data   (first data) ;; 1st image data   
            img        (->> (get img-data :b64_json)
                            (str/decode-base64))]
        (io/spit img-file img)
        (println "Saved image to:" img-file))))

  (defn create-image-mask [img-file mask-file]
    (println "Creating mask...")
    (let [img    (->> (images/load (io/file img-file))
                      (images/convert-to-rgba))
          [w h]  (images/dimension img)
          g2d    (images/g2d img)]
      (. g2d :setComposite (. :java.awt.AlphaComposite :Clear))
      (images/fg-color g2d images/white)
      (images/fill-circle g2d (/ w 2) (/ h 2) (/ w 4))
      (images/dispose g2d)
      (images/save img :png (io/file mask-file))
      (println "Saved mask to:" mask-file)))

  (defn create-image-edit [prompt img-file mask-file result-file]
    (println "Requesting image edit...")
    (let [response  (openai/image-edits (io/slurp img-file :binary true)  ;; image
                                        (io/slurp mask-file :binary true) ;; mask
                                        prompt 
                                        :b64_json
                                        :model :dall-e-2
                                        :prompt-opts {:size "1024x1024", :n 1})]
      (openai/assert-response-http-ok response)
      (let [data       (:data (:data response))
            img-data   (first data) ;; 1st image data   
            img        (->> (get img-data :b64_json)
                              (str/decode-base64))]
        (io/spit result-file img)
        (println "Saved edited image to:" result-file))))

  ;; create the initial image
  (create-image "A sunlit indoor lounge area with a large pool at the center of the image"
                "./image-edit-source.png") 

  ;; derive an image with a mask at the center for placing the flamingo 
  (create-image-mask "./image-edit-source.png"
                     "./image-edit-mask.png") 

  ;; place the flamingo in the mask area at the center
  (create-image-edit "A sunlit indoor lounge area with a pool containing a flamingo"
                     "./image-edit-source.png"
                     "./image-edit-mask.png"
                     "./image-edit-result.png"))
```
