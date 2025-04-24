# OpenAI Client


The OpenAI client runs out-of-the-box without any dependencies on 3rd party libraries.

* [Configuring OpenAI API keys](#configuring-openai-api-keys)
* [Chat Completion](openai-chat-completion.md)
* [Functions](openai-functions.md)
* [Vision](openai-vision.md)
* [Audio](openai-audio.md)
* [Images](openai-images.md)
* [Files](openai-files.md)
* [Models](openai-models.md)
* [Embeddings](openai-embeddings.md)
* Assistants *(in work)*
    * [x] assistants
    * [x] threads
    * [ ] messages
    * [ ] runs
    * [ ] run steps
    * [ ] vector stores
    * [ ] vector store files
    * [ ] vector store file batches
* Batches *(in work)*
    * [ ] api
* [OpenAI API Reference](https://platform.openai.com/docs/api-reference/introduction)

*The OpenAI client is in incubation status. The API might undergo changes.*


### Configuring OpenAI API keys


**Option 1**

Pass the OpenAI API key as an option `:openai-api-key "sk-123456789"` to the OpenAI 
client requests:

```
(openai/chat-completion ... :openai-api-key "sk-123456789")
```


**Option 2**

Define an environment variable at the OS or Shell level:

```
OPENAI_API_KEY=sk-123456789
```


**Option 3**

Add the key to the 'repl.env' file in the REPL home directory. 

`repl.env` is supported with *Venice* V1.12.18 and newer when the REPL has been setup
with:

```
foo>  java -jar venice-1.12.42.jar -setup -colors
```

*Unix like OSs*

The setup will create a `repl.sh` that *sources* a `repl.env` at REPL startup time.


Example 'repl.env':

```
# ------------------------------------------------------------------------------
# Environment variables for unix / linux operating systems
#
# This file will be 'sourced' by 'repl.sh' at REPL startup time. You can add
# as many environment variables you like.
# ------------------------------------------------------------------------------

# OpenAI api key
export OPENAI_API_KEY=sk-123456789
```

*Windows*

The setup will create a `repl.bat` that *sources* a `repl.env.bat` at REPL startup time.


Example 'repl.env.bat':

```
REM # ------------------------------------------------------------------------------
REM # Environment variables for windows operating systems
REM #
REM # This file will be 'sourced' by 'repl.bat' at REPL startup time. You can add
REM # as many environment variables you like.
REM # ------------------------------------------------------------------------------

REM # OpenAI api key
set OPENAI_API_KEY=sk-123456789
```



