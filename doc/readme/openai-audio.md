# Audio

[OpenAI Audio](https://platform.openai.com/docs/api-reference/audio)


* [Create Speech](#create-speech)
* [Transcribe Speech](#transcribe-speech)
* [Translate Speech](#translate-speech)


## Create Speech

Generates audio from the input text.

### Sending Requests

`(audio-speech-generate text voice response-format & options)`

#### Parameter «text»

The text to generate audio from.

```
"The quick brown fox jumped over the lazy dog."
```

#### Parameter «voice»

The voice to use when generating the audio. 

* `:alloy`
* `:echo`
* `:fable`
* `:onyx`
* `:nova`
* `:shimmer`


#### Parameter «response-format»

The format in which the generated images are returned

* `:mp3`  (mimetype: audio/mpeg)
* `:opus` (mimetype: audio/opus)
* `:aac`  (mimetype: audio/aac)
* `:flac` (mimetype: audio/flac)
* `:wav`  (mimetype: audio/wav)
* `:pcm`  (mimetype: audio/pcm)


#### Parameter «options»

| Option            | Description |
| :---              | :---        |
| :uri              | An OpenAI audio speech URI. E.g.: "https://api.openai.com/v1/audio/speech". <br>Defaults  to "https://api.openai.com/v1/audio/speech" |
| :model            | An OpenAI model. E.g.: "tts-1". Defaults to "tts-1".<br>The model can also be passed as a keyword. E.g.: `:tts-1`, `:tts-1-hd`, ...  |
| :openai-api-key   | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :audio-opts       | An optional map of OpenAI audio options. Map keys can be keywords or strings.<br>E.g. `{ :speed 1.0 }`. <br>See: [OpenAI Request Options](https://platform.openai.com/docs/api-reference/audio/createSpeech) |
| :debug            | An optional debug flag (true/false). Defaults  to false.<br>In debug mode prints the HTTP request and response data |
 
 
#### Response value

*The return value is a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |


### Example

```Clojure
(do
  (load-module :openai)

  (let [text      "The quick brown fox jumped over the lazy dog."
        response  (openai/audio-speech-generate text
                                                :alloy
                                                :mp3
                                                :model :tts-1
                                                :audio-opts { :speed 1.0 })]
    (openai/assert-response-http-ok response)
    (let [audio     (:data response)
          size      (/ (count audio) 1024.0)
          file-ext  (openai/audio-file-ext (:mimetype response))
          file      (str "./audio." file-ext)]
      (printf "Saving audio (%.1fKB) to: %s%n" size file)
      (io/spit file audio))))
```



## Transcribe Speech

Transcribes audio into the input language.

### Sending Requests

`(audio-speech-transcribe data audio-type response-format & options)`

#### Parameter «data»

The audio data (a byte buffer)

#### Parameter «audio-type»

The audio type

* `:flac`   (mimetype: audio/flac)
* `:mp3`    (mimetype: audio/mpeg)
* `:mp4`    (mimetype: audio/mp4)
* `:m4a`    (mimetype: audio/m4a)
* `:mpega`  (mimetype: audio/mpega)
* `:ogg`    (mimetype: audio/ogg)
* `:wav`    (mimetype: audio/wav)
* `:webm`   (mimetype: audio/webm)


#### Parameter «response-format»

The format in which the transcribed text is returned

* `:json`
* `:text`
* `:srt`
* `:verbose_json`
* `:vtt`


#### Parameter «options»

| Option            | Description |
| :---              | :---        |
| :uri              | An OpenAI audio speech URI. E.g.: "https://api.openai.com/v1/audio/transcriptions". <br>Defaults  to "https://api.openai.com/v1/audio/transcriptions" |
| :model            | An OpenAI model. E.g.: "whisper-1". Defaults to "whisper-1".<br>The model can also be passed as a keyword. E.g.: `:whisper-1`,...  |
| :openai-api-key   | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :audio-opts       | An optional map of OpenAI audio options. Map keys can be keywords or strings.<br>E.g. `{ :language "en", :temperature 0, :timestamp_granularities "word"}`.<br>See: [OpenAI Request Options](https://platform.openai.com/docs/api-reference/audio/createTranscription) |
| :debug            | An optional debug flag (true/false). Defaults  to false.<br>In debug mode prints the HTTP request and response data |
 
 
#### Response value

*The return value is a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |


### Example 1

```Clojure
(do
  (load-module :openai)

  (defn generate-mp3-audio [text]
    (let [response  (openai/audio-speech-generate text
                                                  :alloy
                                                  :mp3
                                                  :model :tts-1)]
      (openai/assert-response-http-ok response)
      (:data response)))

  (let [text       "The quick brown fox jumped over the lazy dog."
        audio-data (generate-mp3-audio text)
        response   (openai/audio-speech-transcribe audio-data 
                                                    :mp3 
                                                    :json)]
    (openai/assert-response-http-ok response)
    (println (:text (:data response)))))
```

### Example 2

```Clojure
(do
  (load-module :openai)

  (defn generate-mp3-audio [text]
    (let [response  (openai/audio-speech-generate text
                                                  :alloy
                                                  :mp3
                                                  :model :tts-1)]
      (openai/assert-response-http-ok response)
      (:data response)))

  (let [text       "The quick brown fox jumped over the lazy dog."
        audio-data (generate-mp3-audio text)
        audio-opts { :language "en"    ;; ISO-639-1 
                     :temperature 0
                     :timestamp_granularities "word" }
        response   (openai/audio-speech-transcribe audio-data 
                                                    :mp3 
                                                    :verbose_json 
                                                    :audio-opts audio-opts)]
    (openai/assert-response-http-ok response)
    (prn (:data response))))
```


## Translate Speech

Translates audio into English.

### Sending Requests

`(audio-speech-translate data audio-type response-format & options)`

#### Parameter «data»

The audio data (a byte buffer)

#### Parameter «audio-type»

The audio type

* `:flac`   (mimetype: audio/flac)
* `:mp3`    (mimetype: audio/mpeg)
* `:mp4`    (mimetype: audio/mp4)
* `:m4a`    (mimetype: audio/m4a)
* `:mpega`  (mimetype: audio/mpega)
* `:ogg`    (mimetype: audio/ogg)
* `:wav`    (mimetype: audio/wav)
* `:webm`   (mimetype: audio/webm)

#### Parameter «response-format»

The format in which the transcribed text is returned

* `:json`
* `:text`
* `:srt`
* `:verbose_json`
* `:vtt`


#### Parameter «options»

| Option            | Description |
| :---              | :---        |
| :uri              | An OpenAI audio speech URI. E.g.: "https://api.openai.com/v1/audio/translations". <br>Defaults  to "https://api.openai.com/v1/audio/translations" |
| :model            | An OpenAI model. E.g.: "whisper-1". Defaults to "whisper-1".<br>The model can also be passed as a keyword. E.g.: `:whisper-1`,...  |
| :openai-api-key   | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :audio-opts       | An optional map of OpenAI audio options. Map keys can be keywords or strings.<br>E.g. `{ :temperature 0, :prompt "....."}`.<br>See: [OpenAI Request Options](https://platform.openai.com/docs/api-reference/audio/createTranslation) |
| :debug            | An optional debug flag (true/false). Defaults  to false.<br>In debug mode prints the HTTP request and response data |
 
 
#### Response value

*The return value is a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |


### Example

```Clojure
(do
  (load-module :openai)

  (defn generate-mp3-audio [text]
    (let [response  (openai/audio-speech-generate text
                                                  :alloy
                                                  :mp3
                                                  :model :tts-1)]
      (openai/assert-response-http-ok response)
      (:data response)))

  (let [text       "Der schnelle braune Fuchs sprang über den faulen Hund."
        audio-data (generate-mp3-audio text)
        response   (openai/audio-speech-translate audio-data 
                                                  :mp3 
                                                  :json)]
    (openai/assert-response-http-ok response)
    (println (:text (:data response)))))
```
