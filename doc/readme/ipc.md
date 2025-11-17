# Inter-Process-Communication


Venice Inter-Process-Communication (IPC), is a Venice API that allows application components to communicate with each other by sending and receiving messages. It enables asynchronous and loosely coupled communication, making it ideal for distributed applications. Venice IPC works by either exchanging messages between a client and a server or using a messaging provider, such as a server, that routes messages from producers to consumers through queues with either a single consumer (point-to-point) or multiple consumers (publish-subscribe).


* [IPC Communication Modes](#ipc-communication-modes)
* [Messages](#messages)
* [Compressing Messages](#compressing-messages)
* [Encrypting Messages](#encrypting-messages)
* [Managing Queues](#managing-queues)
* [Message Utils](#message-utils)
* [Timeouts, Retries, and Idempotency in Distributed Systems](#timeouts-retries-and-idempotency-in-distributed-systems)


## IPC Communication Modes

### Send / Receive

Send a message from a client to a server and receive a response


**synchronous send / receive**

```clojure
(do
  ;; thread-safe printing to console
  (defn println [& msg] (locking println (apply core/println msg)))

  (defn echo-handler [m]
    (println "REQUEST:" (ipc/message->json true m)) 
    m)

  (try-with [server (ipc/server 33333 echo-handler)
             client (ipc/client "localhost" 33333)]
    ;; send a plain text message: requestId="1", topic="test", payload="hello"
    (->> (ipc/plain-text-message "1" "test" "hello")
         (ipc/send client)
         (ipc/message->json true)
         (println "RESPONSE:"))))
```

**asynchronous send / receive**

```clojure
(do
  ;; thread-safe printing to console
  (defn println [& msg] (locking println (apply core/println msg)))

  (defn echo-handler [m]
    (println "REQUEST:" (ipc/message->json true m)) 
    m)

  (try-with [server (ipc/server 33333 echo-handler)
             client (ipc/client "localhost" 33333)]
    ;; send a plain text message: requestId="1", topic="test", payload="hello"
    (let [response (->> (ipc/plain-text-message "1" "test" "hello")
                        (ipc/send-async client))]
      (->> (deref response 300 :timeout)  ;; deref the result future with 300ms timeout
           (ipc/message->json true)
           (println "RESPONSE:")))))
```

**oneway send (no response)**

```clojure
(do
  ;; thread-safe printing to console
  (defn println [& msg] (locking println (apply core/println msg)))

  (defn handler [m]
    (println "REQUEST:" (ipc/message->json true m)) 
    nil)

  (try-with [server (ipc/server 33333 handler)
             client (ipc/client "localhost" 33333)]
    ;; send a plain text messages: requestId="1" and "2", topic="test", payload="hello"
    (ipc/send-oneway client (ipc/plain-text-message "1" "test" "hello"))
    (ipc/send-oneway client (ipc/plain-text-message "2" "test" "hello"))))
```


### Offer / Poll

Offer messages to a queue and poll messages from a queue. A message is delivered to at most one client.

**synchronous offer / poll**

```clojure
(do
  ;; thread-safe printing
  (defn println [& msg] (locking println (apply core/println msg)))

  ;; the server handler is not involved with offer/poll!
  (defn echo-handler [m] m)

  (try-with [server (ipc/server 33333 echo-handler)
             client1 (ipc/client "localhost" 33333)
             client2 (ipc/client "localhost" 33333)]
    (let [order-queue "orders"
          capacity    1_000
          timeout     300]
      ;; create a queue to allow client1 and client2 to exchange messages
      
      (ipc/create-queue server order-queue capacity)

      ;; client1 offers an order Venice data message to the queue
      ;;   requestId="1" and "2", topic="order", payload={:item "espresso", :count 2}
      (let [order (ipc/venice-message "1" "order" {:item "espresso", :count 2})]
        (println "ORDER:" (ipc/message->json true order))
        
        ;; publish the order
        (->> (ipc/offer client1 order-queue timeout order)
             (ipc/message->json true)
             (println "OFFERED:")))

      ;; client2 pulls next order from the queue
      (->> (ipc/poll client2 order-queue timeout)
           (ipc/message->json true)
           (println "POLLED:")))))
```

**asynchronous offer / poll**

```clojure
(do
  ;; thread-safe printing
  (defn println [& msg] (locking println (apply core/println msg)))

  ;; the server handler is not involved with offer/poll!
  (defn echo-handler [m] m)

  (try-with [server (ipc/server 33333 echo-handler)
             client1 (ipc/client "localhost" 33333)
             client2 (ipc/client "localhost" 33333)]
    (let [order-queue "orders"
          capacity    1_000
          timeout     300]
      ;; create a queue to allow client1 and client2 to exchange messages
      (ipc/create-queue server order-queue capacity)

      ;; client1 offers order Venice data message to the queue
      ;;   requestId="1" and "2", topic="order", payload={:item "espresso", :count 2}
      (let [order (ipc/venice-message "1" "order" {:item "espresso", :count 2})]
        (println "ORDER:" (ipc/message->json true order))

        ;; publish the order
        (-<> (ipc/offer-async client1 order-queue order)
             (deref <> 300 :timeout)
             (ipc/message->json true <>)
             (println "OFFERED:" <>)))

      ;; client2 pulls next order from the queue
      (-<> (ipc/poll-async client2 order-queue)
           (deref <> 300 :timeout)
           (ipc/message->json true <>)
           (println "POLLED:" <>)))))
```



### Publish / Subscribe

**synchronous publish**

```clojure
(do
  ;; thread-safe printing
  (defn println [& msg] (locking println (apply core/println msg)))

  ;; the server handler is not involved with publish/subscribe!
  (defn echo-handler [m] m)

  (defn client-subscribe-handler [m]
    (println "SUBSCRIBED:" (ipc/message->json true m)))

  (try-with [server (ipc/server 33333 echo-handler)
             client1 (ipc/client "localhost" 33333)
             client2 (ipc/client "localhost" 33333)]

    ;; client1 subscribes to messages with topic 'test'
    (ipc/subscribe client1 "test" client-subscribe-handler)

    ;; client2 publishes a plain text message: 
    ;;   requestId="1", topic="test", payload="hello"
    (let [m (ipc/plain-text-message "1" "test" "hello")]
      (println "PUBLISHING:" (ipc/message->json true m))
      (->> (ipc/publish client2 m)
           (ipc/message->json true)
           (println "PUBLISHED:")))

    (sleep 300)))
```

**asynchronous publish**

```clojure
(do
  ;; thread-safe printing
  (defn println [& msg] (locking println (apply core/println msg)))

  ;; the server handler is not involved with publish/subscribe!
  (defn echo-handler [m] m)

  (defn client-subscribe-handler [m]
    (println "SUBSCRIBED:" (ipc/message->json true m)))

  (try-with [server (ipc/server 33333 echo-handler)
             client1 (ipc/client "localhost" 33333)
             client2 (ipc/client "localhost" 33333)]

    ;; client1 subscribes to messages with topic 'test'
    (ipc/subscribe client1 "test" client-subscribe-handler)

    ;; client2 publishes a plain text message: 
    ;;   requestId="1", topic="test", payload="hello"
    (let [m (ipc/plain-text-message "1" "test" "hello")]
      (println "PUBLISHING:" (ipc/message->json true m))
      (-<> (ipc/publish-async client2 m)
           (deref <> 300 :timeout)
           (ipc/message->json true <>)
           (println "PUBLISHED:" <>)))

    (sleep 300)))
```


## Messages


### Message Layout

```
  Fields                             Filled by
  
 ┌───────────────────────────────┐
 │ ID                            │   by send, offer/poll, publish/subscribe method
 ├───────────────────────────────┤
 │ Message Type                  │   by send, offer/poll, publish/subscribe method
 ├───────────────────────────────┤
 │ Oneway                        │   by client or framework method
 ├───────────────────────────────┤
 │ Response Status               │   by server response processor
 ├───────────────────────────────┤
 │ Timestamp                     │   by message creator
 ├───────────────────────────────┤
 │ Request ID                    │   by client (may be used for idempotency checks by the receiver)
 ├───────────────────────────────┤
 │ Topic                         │   by client
 ├───────────────────────────────┤
 │ Payload Mimetype              │   by client
 ├───────────────────────────────┤
 │ Payload Charset               │   by client if payload data is a string else null
 ├───────────────────────────────┤
 │ Payload data                  │   by client
 └───────────────────────────────┘
```

**Message Types**

* `:REQUEST`     - a request message
* `:PUBLISH`     - a publish message
* `:SUBSCRIBE`   - a subscribe message
* `:UNSUBSCRIBE` - an unsubscribe message
* `:OFFER`       - an offer message for a queue
* `:POLL`        - a poll message from a queue
* `:RESPONSE`    - a response to a request message
* `:NULL`        - a message with yet undefined type\n


**Response Status**
* `:OK`              - a response message for a successfully processed request
* `:SERVER_ERROR`    - a response indicating a server side error while processing the request 
* `:BAD_REQUEST`     - invalid request
* `:HANDLER_ERROR`   - a server handler error in the server's request processing
* `:QUEUE_NOT_FOUND` - the required queue does not exist
* `:QUEUE_EMPTY`     - the adressed queue in a poll request is empty
* `:QUEUE_FULL`      - the adressed queue in offer request is full
* `:NULL`            - a message with yet undefined status, filled when processing the message



### Message Payload Types

Venice IPC supports messages with various payload types:
  * plain text
  * text (json, xml, ...)
  * binary data
  * Venice data



#### 1. Plain Text Messages

```clojure
(->> (ipc/plain-text-message "1" "test" "hello")
     (ipc/message->json true)
     (println))
```


#### 2. Text Messages

Text message payloads are defined by
  * a mimetype. E.g.:  `text/plain`, `application/json`, ...
  * a charset. E.g.:  `:UTF-8`
  * the textual data

```clojure
(->> (ipc/text-message "1" "test" "text/plain" :UTF-8 "hello")
     (ipc/message->json true)
     (println))
```

```clojure
(->> """{"item": "espresso", "count": 2}"""
     (ipc/text-message "1" "order" "application/json" :UTF-8)
     (ipc/message->json true)
     (println))
```


#### 3. Binary Messages

Text message payloads are defined by
  * a mimetype. E.g.:  `application/octet-stream`, `application/pdf`, ...
  * the binary data

```clojure
(->> (bytebuf [0 1 2 3 4 5 6 7])
     (ipc/binary-message "1" "test" "application/octet-stream")
     (ipc/message->json true)
     (println))
```


```clojure
(->> (io/slurp "test.pdf" :binary true)
     (ipc/binary-message "1" "test" "application/pdf")
     (ipc/message->json true)
     (println))
```


#### 4. Venice Data Messages

```clojure
(->> (ipc/venice-message "1" "order" {:item "espresso", :count 2})
     (ipc/message->json true)
     (println))
```



## Compressing Messages

Messages can be transparently GZIP compressed/decompressed while being transferred.

A compression cutoff size for payload messages defines from which payload data size compression
is taking effect. 

By default compression is turned off (cutoff size = -1)

The cutoff size can be specified as a number like `1000` or a number with a unit like `:1KB` or `:2MB`


```clojure
(do
  ;; thread-safe printing
  (defn println [& msg] (locking println (apply core/println msg)))

  (defn echo-handler [m]
    (println "REQUEST:" (ipc/message->json true m)) 
    m)
  
  ;; transparently compress/decompress messages with a size > 1KB bytes
  (try-with [server (ipc/server 33333 echo-handler :compress-cutoff-size :1KB)
             client (ipc/client "localhost" 33333 :compress-cutoff-size :1KB)]
    ;; send a plain text message: requestId="1", topic="test", payload="hello"
    (->> (ipc/plain-text-message "1" "test" "hello")
         (ipc/send client)
         (ipc/message->json true)
         (println "RESPONSE:"))))
```


## Encrypting Messages

If encryption is enabled the payload data of all messages exchanged
between a client and its associated server is encrypted.

The data is AES-256-GCM encrypted using a secret that is created and 
exchanged using the Diffie-Hellman key exchange algorithm.

> [!NOTE]
> If multiple clients are involved in message passing turn on 
> encryption on all clients!


```clojure
(do
  ;; thread-safe printing
  (defn println [& msg] (locking println (apply core/println msg)))

  (defn echo-handler [m]
    (println "REQUEST:" (ipc/message->json true m)) 
    m)
  
  ;; transparently encrypt messages
  (try-with [server (ipc/server 33333 echo-handler)
             client (ipc/client "localhost" 33333 :encrypt true)]
    ;; send a plain text message: requestId="1", topic="test", payload="hello"
    (->> (ipc/plain-text-message "1" "test" "hello")
         (ipc/send client)
         (ipc/message->json true)
         (println "RESPONSE:"))))
```


## Managing Queues

#### Create Queue

```clojure
(do
  (defn echo-handler [m] m)
  
  (try-with [server (ipc/server 33333 echo-handler)
             client (ipc/client "localhost" 33333)]
     (ipc/create-queue server "orders" 1_000)))
```

#### Remove Queue

```clojure
(do
  (defn echo-handler [m] m)
  
  (try-with [server (ipc/server 33333 echo-handler)
             client (ipc/client "localhost" 33333)]
     (ipc/create-queue server "orders" 1_000)
     ;; ...
     (ipc/remove-queue server "orders")))
```

#### Check Queue Exists

```clojure
(do
  (defn echo-handler [m] m)
  
  (try-with [server (ipc/server 33333 echo-handler)
             client (ipc/client "localhost" 33333)]
     (ipc/create-queue server "orders" 1_000)
     
     (ipc/exists-queue? server "orders")))
```



## Message Utils


#### Accessing Message Fields

**Supported field names:** 

  * `:id`               - the message's technical ID
  * `:type`             - the message type (request, response, ..) 
  * `:oneway?`          - `true` if one-way message else `false`
  * `:response-status`  - the response status (ok, bad request, ...) 
  * `:timestamp`        - the message's creation timestamp in milliseconds since epoch
  * `:topic`            - the topic
  * `:payload-mimetype` - the payload data mimetype
  * `:payload-charset`  - the payload data charset (if payload is a text form)
  * `:payload-text`     - the payload converted to text data if payload is textual data else error
  * `:payload-binary`   - the payload binary data (the raw message binary data)
  * `:payload-venice`   - the payload converted venice data if mimetype is 'application/json' else error


**Text Message**

```clojure
(do
  (try-with [server (ipc/server 33333 (fn [m] m))
             client (ipc/client "localhost" 33333)]
    (let [m (ipc/send client (ipc/text-message "1" "test" "text/plain" :UTF-8"Hello!"))]
      (println (ipc/message-field m :id))
      (println (ipc/message-field m :type))
      (println (ipc/message-field m :oneway?))
      (println (ipc/message-field m :timestamp))
      (println (ipc/message-field m :response-status))
      (println (ipc/message-field m :topic))
      (println (ipc/message-field m :payload-mimetype))
      (println (ipc/message-field m :payload-charset))
      (println (ipc/message-field m :payload-text))
      (println (ipc/message-field m :payload-binary)))))
```

Output:

```
baac8cf8-48fd-4e16-a1cc-b3867bd4e505
:RESPONSE
true
1763313279378
:OK
test
text/plain
:UTF-8
Hello!
[72 101 108 108 111 33]
```


**Binary Message**

```clojure
(do
  (try-with [server (ipc/server 33333 (fn [m] m))
             client (ipc/client "localhost" 33333)]
    (let [m (ipc/send client (ipc/binary-message "1" "test" "application/octet-stream" (bytebuf [0 1 2 3 4 5 6 7])))]
      (println (ipc/message-field m :id))
      (println (ipc/message-field m :type))
      (println (ipc/message-field m :oneway?))
      (println (ipc/message-field m :timestamp))
      (println (ipc/message-field m :response-status))
      (println (ipc/message-field m :topic))
      (println (ipc/message-field m :payload-mimetype))
      (println (ipc/message-field m :payload-charset))
      (println (ipc/message-field m :payload-binary)))))
```

Output:

```
abfaab17-dea7-4a38-85ee-501b6ead0aed
:RESPONSE
true
1763313327205
:OK
test
application/octet-stream
nil
[0 1 2 3 4 5 6 7]
```


**Venice Data Message**

```clojure
(do
  (try-with [server (ipc/server 33333 (fn [m] m))
             client (ipc/client "localhost" 33333)]
    (let [m (ipc/send client (ipc/venice-message "1" "test" {:a 100, :b 200} ))]
      (println (ipc/message-field m :id))
      (println (ipc/message-field m :type))
      (println (ipc/message-field m :oneway?))
      (println (ipc/message-field m :timestamp))
      (println (ipc/message-field m :response-status))
      (println (ipc/message-field m :topic))
      (println (ipc/message-field m :payload-mimetype))
      (println (ipc/message-field m :payload-charset))
      (println (ipc/message-field m :payload-text))
      (println (ipc/message-field m :payload-venice)))))
```

Output:

```
2815b1c6-55cf-417c-9c19-88efe5c30ba0
:RESPONSE
true
1763313337189
:OK
test
application/json
:UTF-8
{"a":100,"b":200}
{:a 100 :b 200}
```


#### Convert Message to JSON

```clojure
(do
  (try-with [server (ipc/server 33333 (fn [m] m))
             client (ipc/client "localhost" 33333)]
    ;; send a plain text message: requestId="1", topic="test", payload="hello"
    (->> (ipc/plain-text-message "1" "test" "hello")
         (ipc/send client)
         (ipc/message->json true)    ;; formatted JSON enabled
         (println "RESPONSE:"))))
```


#### Convert Message to Venice Map

```clojure
(do
  (try-with [server (ipc/server 33333 (fn [m] m))
             client (ipc/client "localhost" 33333)]
    ;; send a plain text message: requestId="1", topic="test", payload="hello"
    (->> (ipc/plain-text-message "1" "test" "hello")
         (ipc/send client)
         (ipc/message->map)
         (println "RESPONSE:"))))
```


#### Check Message Response Status OK

```clojure
(do
  (try-with [server (ipc/server 33333 (fn [m] m))
             client (ipc/client "localhost" 33333)]
    ;; send a plain text message: requestId="1", topic="test", payload="hello"
    (->> (ipc/plain-text-message "1" "test" "hello")
         (ipc/send client)
         (ipc/response-ok?)
         (println "RESPONSE OK:"))))
```


#### Check Message Response Status Error

```clojure
(do
  (try-with [server (ipc/server 33333 (fn [m] m))
             client (ipc/client "localhost" 33333)]
    ;; send a plain text message: requestId="1", topic="test", payload="hello"
    (->> (ipc/plain-text-message "1" "test" "hello")
         (ipc/send client)
         (ipc/response-err?)
         (println "RESPONSE ERR:"))))
```




## Timeouts, Retries, and Idempotency in Distributed Systems

Distributed systems are difficult because of three unavoidable truths:
  1. Information cannot be transmitted instantly
  2. Sometimes remote components are unreachable
  3. Resources are limited

These constraints underpin the core resilience tools that systems must implement: timeouts, retries, and idempotency.



### 1. Timeouts

A timeout is when a client stops waiting for a response.
We need timeouts because:
  * Network communication takes time and is unpredictable
  * A service may be down or slow
  * Waiting ties up resources (threads, memory, connections)
  * Users won’t wait forever anyway—they’ll refresh or abandon

Choosing good timeout values requires understanding:
  * Normal performance (using histograms, not averages)
  * User expectations (no point waiting 30 seconds if users give up at 5)
  * Configuration flexibility (timeouts must be adjustable without redeploys)

Timeouts protect the health of the system at the expense of individual requests.



### 2. Retries

Retries help recover from transient failures (e.g., a node restarting or a bad network hop).

However, retries must be controlled to avoid creating a self-inflicted denial-of-service. Problems include:
  * Too many retries amplifying load
  * Synchronized retry “waves” causing traffic spikes

Good retry strategies include:
  * Retry limits (never retry indefinitely)
  * Jitter (randomized delay to avoid synchronized bursts)
  * Backoff (gradually increasing delay, but not unbounded exponential)
  * Dynamic configuration (same as with timeouts)

Retries improve reliability but also increase pressure on already stressed services.



### 3. Idempotency

Retries are only safe if the operation being retried doesn’t cause duplicate effects.

An idempotent operation can be repeated without changing the result (e.g., setting a value vs. incrementing it).

Why this matters:

If a client sends "Pay John $100" and the response is lost, the client might retry — leading to John receiving $200 unless the server detects the duplicate.

There are two main ways to achieve idempotency:

A) Request IDs (preferred)
  * Each request includes a unique ID
  * Server stores results keyed by this ID
  * If a duplicate arrives, return the original response

This requires protocol changes but is simple and robust.

B) Server-side fingerprinting (fallback for legacy systems)
  * Server hashes the request body to detect duplicates
  * Complications arise if the body changes (timestamps, reordered fields, repeated calls)

Idempotency is easiest when designed in early and painful to retrofit.



### Overall Principles

  * Sometimes you must give up → use timeouts
  * When you give up, you may try again → use controlled retries
  * To safely try again, the operation must be idempotent

These mechanisms don’t eliminate the fundamental limitations of distributed systems—but they help systems behave predictably and resiliently within those constraints.
