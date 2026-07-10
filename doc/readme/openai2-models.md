# Audio

 

* [Create Speech](#create-speech)

 
 

## Create Speech

Example:

```clojure
(do
  (load-module :openai-java)
  
  (let [client   (openai-java/client)
        is       (openai-java/create-speech
                      client 
                      :GPT_4O_MINI_TTS
                      "Today is a wonderful day to build something people love!"
                      :format :MP3
                      :voice "cedar")]
    (->> (io/slurp-stream is :binary true)
          (io/spit (io/file "./audio.mp3")))
    (println "Audio saved to ./audio.mp3")))
```


Example with playing the created audio (MacOS only):

```clojure
(do
  (load-module :openai-java)
  
  (let [client   (openai-java/client)
        is       (openai-java/create-speech
                      client 
                      :GPT_4O_MINI_TTS
                      "Today is a wonderful day to build something people love!"
                      :format :MP3
                      :voice "cedar")]
    (let [audio-file (io/file "./audio.mp3")]
      (println "Downloading audio ...")
      (->> (io/slurp-stream is :binary true)
            (io/spit audio-file))
      (println "Saved audio to" audio-file)
      (if (os-type? :mac-osx)
        (do
          (println "Playing audio ...")
          (sh "afplay" (io/file-path audio-file))
          nil)
        (println "Playing audio supported on MacOS only!")))))
```
