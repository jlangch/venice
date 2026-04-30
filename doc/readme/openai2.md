# OpenAI Client


The OpenAI client runs out-of-the-box without any dependencies on 3rd party libraries.

* [Configuring OpenAI API keys](#configuring-openai-api-keys)
* [Chat Completion](openai2-chat-completion.md)


### Configuring OpenAI API keys


**Option 1**

Pass the OpenAI API key as an option `:openai-api-key "sk-123456789"` for creating
the OpenAI client:

```
(do
  (load-module :openai-java)
  (let [client (openai-java/client :openai-api-key "sk-123456789")]
    ;; ...
    (openai-java/close client)))
```


**Option 2**

Define an environment variable at the OS or Shell level:

```
OPENAI_API_KEY=sk-123456789
```

Create a client that reads the OpenAI API key from the env var:

```
(do
  (load-module :openai-java)
  (let [client (openai-java/client)]
    ;; ...
    (openai-java/close client)))
```


**Option 3**

Add the key to the `repl.env` file in the REPL home directory. 

Linux/Mac OS:

```
# OpenAI api key
export OPENAI_API_KEY=sk-123456789
```


Windows:

```
# OpenAI api key
set OPENAI_API_KEY=sk-123456789
```

Create a client that reads the OpenAI API key from the env var:

```
(do
  (load-module :openai-java)
  (let [client (openai-java/client)]
    ;; ...
    (openai-java/close client)))
```
