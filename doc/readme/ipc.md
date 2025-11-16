# Inter-Process-Communication


Venice Inter-Process-Communication (IPC), is a Venice API that allows application components to communicate with each other by sending and receiving messages. It enables asynchronous and loosely coupled communication, making it ideal for distributed applications. Venice IPC works by either exchanging messages between a client and a server or using a messaging provider, such as a server, that routes messages from producers to consumers through queues with either a single consumer (point-to-point) or multiple consumers (publish-subscribe).


*in work*


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


### Message Types

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

### Create Queue

```clojure
(do
  (defn echo-handler [m] m)
  
  (try-with [server (ipc/server 33333 echo-handler)
             client (ipc/client "localhost" 33333)]
     (ipc/create-queue server "orders" 1_000)))
```

### Remove Queue

```clojure
(do
  (defn echo-handler [m] m)
  
  (try-with [server (ipc/server 33333 echo-handler)
             client (ipc/client "localhost" 33333)]
     (ipc/create-queue server "orders" 1_000)
     ;; ...
     (ipc/remove-queue server "orders")))
```

### Check Queue Exists

```clojure
(do
  (defn echo-handler [m] m)
  
  (try-with [server (ipc/server 33333 echo-handler)
             client (ipc/client "localhost" 33333)]
     (ipc/create-queue server "orders" 1_000)
     
     (ipc/exists-queue? server "orders")))
```



## Message Utils

*todo*

  * `ipc/message-field`

  * `ipc/message->json`

  * `ipc/message->map`

  * `ipc/response-ok?`

  * `ipc/response-err?`




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
