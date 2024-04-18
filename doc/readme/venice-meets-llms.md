# Venice meets LLMs

* [OpenAI Client](#openai-client)
* [LangChain4J](#langchain4j)
* [Qdrant Vector DB](#qdrant-vector-db)



## OpenAI Client

*The OpenAI client is incubation status. The API might undergo changes.*

The OpenAI client runs out-of-the-box without any dependencies on 3rd party libraries.

* [Chat Completion](#chat-completion)
* [Chat Completion Streaming](#chat-completion-streaming)


### Chat Completion

Runs a chat completion.

To run the request asynchronously just wrap it in a `future` and
deref it, when the result is required.



#### Sending Requests

`(chat-completion prompt & options)`

Send a chat completion request given a prompt and options.


#### Parameter «options»

| Option             | Description |
| :---               | :---        |
| :uri               | An OpenAI chat completion uri. E.g.: <br>"https://api.openai.com/v1/chat/completions".<br>Defaults to "https://api.openai.com/v1/chat/completions" |
| :model             | An OpenAI model. E.g.: "gpt-3.5-turbo". Defaults to "gpt-3.5-turbo" |
| :prompt-opts       | An optional map of OpenAI chat request prompt options<br>E.g. {:temperature 0.2} <br>See: [OpenAI Request Options](https://platform.openai.com/docs/api-reference/chat/create) |
| :openai-api-key    | An optional OpenAI API Key. As default the key is read from the environment variable "OPENAI_API_KEY". |
| :debug             | An optional debug flag (true/false). Defaults to false. <br>In debug mode prints the HTTP request and response data |
 
#### Return value

*Returns a map with the response data:*

| Field      | Description |
| :---       | :---        |
| :status    | The HTTP status (a long)         |
| :mimetype  | The content type's mimetype      |
| :headers   | A map of headers. key: header name, value: list of header values |
| :message   | The final chat completion message if the OpenAI  server returned the HTTP status OK, else `nil` |
| :data      | If the response' HTTP status is `HTTP_OK` the data fields contains the chat completion message.<br> If the response' HTTP status is not `HTTP_OK` the data fields contains an error message formatted as plain or JSON string. |

See:
 * [OpenAI Chat Completions API](https://platform.openai.com/docs/guides/text-generation/chat-completions-api)
 * [OpenAI API Reference](https://platform.openai.com/docs/api-reference/chat/create)
 * [OpenAI API Examples](https://platform.openai.com/examples)
 * [OpenAI API Examples Prompts](https://platform.openai.com/examples?category=code)


### Chat Completion Streaming




## LangChain4J]


## Qdrant Vector DB

