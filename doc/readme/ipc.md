# Inter-Process-Communication


Venice Inter-Process-Communication (IPC), is a Venice API that allows application components to communicate with each other by sending and receiving messages. It enables asynchronous and loosely coupled communication, making it ideal for distributed applications. Venice IPC works by either exchanging messages between a client and a server or using a messaging provider, such as a server, that routes messages from producers to consumers through queues with either a single consumer (point-to-point) or multiple consumers (publish-subscribe).


* [IPC Communication Modes](#ipc-communication-modes)
    * [Send and Receive](#send-and-receive)
    * [Offer and Poll](#offer-and-poll)
    * [Publish and Subscribe](#publish-and-subscribe)
* [Messages](#messages)
* [Compressing Messages](#compressing-messages)
* [Encrypting Messages](#encrypting-messages)
* [Managing Queues](#managing-queues)
* [Message Utils](#message-utils)
* [Timeouts, Retries, and Idempotency in Distributed Systems](#timeouts-retries-and-idempotency-in-distributed-systems)


## IPC Communication Modes

### Send and Receive

Send a message from a client to a server and receive a response. The server's 
pluggable handler function computes the response from the request.

**Note:** If the server's handler function takes more than a couple of 10 milliseconds 
to process the request, consider to use *offer/poll* with a reply queue to improve the system 
throughput!


**Synchronous send / receive**

```clojure
(do
  ;; thread-safe printing to console
  (defn println [& msg] (locking println (apply core/println msg)))

  ;; a simple echo handler that just returns the request
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

```clojure
;; processing Venice message data with a handler that adds two numbers 
;; request:  {:x 100, :y 200}
;; response: {:z 300}
(do
  (defn handler [m]
    (let [topic      (ipc/message-field m :topic)
          _          (assert (= "add" topic))
          request-id (ipc/message-field m :request-id)
          request    (ipc/message-field m :payload-venice)
          result     {:z (+ (:x request) (:y request))}]
      (ipc/venice-message request-id topic result)))

  (try-with [server (ipc/server 33333 handler)
             client (ipc/client "localhost" 33333)]
    (->> (ipc/venice-message "1" "add" {:x 100 :y 200})
         (ipc/send client)
         (ipc/message->json true)
         (println))))
```


**Asynchronous send / receive**

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
      (->> (deref response 1_000 :timeout)  ;; deref the result future with 1s timeout
           (ipc/message->json true)
           (println "RESPONSE:")))))
```


**Oneway send (no response)**

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



### Offer and Poll

Offer messages to a queue and poll messages from a queue. More than one client can offer/poll
messages to/from queues but a message is delivered to one client only.


**Synchronous offer / poll**

```clojure
(do
  ;; thread-safe printing
  (defn println [& msg] (locking println (apply core/println msg)))

  (try-with [server (ipc/server 33333)
             client1 (ipc/client 33333)
             client2 (ipc/client 33333)]
    ;; create an orders queue (capacity=100) to allow client1 and client2 to exchange messages
    (ipc/create-queue server :orders 100)

    ;; client1 offers an order Venice data message to the queue
    ;;   requestId="1" and "2", topic="order", payload={:item "espresso", :count 2}
    (let [order (ipc/venice-message "1" "order" {:item "espresso", :count 2})]
      (println "ORDER:" (ipc/message->json true order))

      ;; client1 offers the order
      (->> (ipc/offer client1 :orders 300 order)
           (ipc/message->json true)
           (println "OFFERED:")))

    ;; client2 polls next order from the queue
    (->> (ipc/poll client2 :orders 300)
         (ipc/message->json true)
         (println "POLLED:"))))
```


**Asynchronous offer / poll**

```clojure
(do
  ;; thread-safe printing
  (defn println [& msg] (locking println (apply core/println msg)))

  (try-with [server (ipc/server 33333)
             client1 (ipc/client 33333)
             client2 (ipc/client 33333)]
    ;; create an orders queue (capacity=100) to allow client1 and client2 to exchange messages
    (ipc/create-queue server :orders 100)

    ;; client1 offers order Venice data message to the queue
    ;;   requestId="1" and "2", topic="order", payload={:item "espresso", :count 2}
    (let [order (ipc/venice-message "1" "order" {:item "espresso", :count 2})]
      (println "ORDER:" (ipc/message->json true order))

      ;; client1 offers the order
      (-<> (ipc/offer-async client1 :orders 300 order)
           (deref <> 1_000 :timeout)
           (ipc/message->json true <>)
           (println "OFFERED:" <>)))

    ;; client2 polls next order from the queue
    (-<> (ipc/poll-async client2 :orders 300)
         (deref <> 1_000 :timeout)
         (ipc/message->json true <>)
         (println "POLLED:" <>))))
```


**Temporary queues**

Temporary queues can be created dynamically for use as a dedicated reply queue for a client. You can use these queues to ensure that a reply message can be sent to the appropriate client.

Temporary queues live only as long as the client, that created it, lives.


```clojure
(do
  ;; thread-safe printing
  (defn println [& msg] (locking println (apply core/println msg)))

  ;; the barista processes the order and sends the order confirmation back  
  ;; via the message's reply queue
  (defn barista-worker [client queue]
    (println "(barista) STARTED")
    (while true
      (let [order (ipc/poll client queue 1_000)]
        (when (ipc/response-ok? order)
          (let [request-id     (ipc/message-field order :request-id)
                reply-to-queue (ipc/message-field order :reply-to-queue-name)
                order-data     (ipc/message-field order :payload-venice)
                confirmation   (ipc/venice-message request-id "order-confirmed" order-data)]
            (println "(barista) ORDER PROCESSED:" (ipc/message->json true order))
            (ipc/offer client reply-to-queue 1_000 confirmation)))))
    (println "(barista) TERMINATED"))

  ;; places an order on behalf of the client - returns a future
  (defn place-order [client-id client queue reply-queue request-counter]
    (let [name  (str "(client-" client-id ")")]
      (future (fn []
                (let [req-id   (str "client-" client-id "--#" (swap! request-counter inc))
                      order    (ipc/venice-message req-id "order" {:item "espresso", :count 2})
                      response (ipc/offer client :orders reply-queue 500 order)]
                  (if (ipc/response-ok? response)
                    (do
                      (println name " ORDER:" (ipc/message->json true order))
                      ;; client waits for the order confirmation
                      (let [confirmation (ipc/poll client reply-queue 500)]
                        (if (ipc/response-ok? confirmation)
                         (println name " ORDER CONFIRMED:"     (ipc/message->json true confirmation))
                         (println name " ORDER NOT CONFIRMED:" (ipc/message->json true confirmation)))))
                     (println name " FAILED TO PLACE ORDER")))))))

  (try-with [server   (ipc/server 33333)
             client1  (ipc/client 33333)
             client2  (ipc/client 33333)
             barista  (ipc/client 33333)]
    (let [client1-reply-queue     (ipc/create-temporary-queue client1 100)
          client1-request-counter (atom 0)
          client2-reply-queue     (ipc/create-temporary-queue client2 100)
          client2-request-counter (atom 0)]
      ;; create the orders queue
      (ipc/create-queue server :orders 100)

      ;; start the barista workers
      (futures-fork 1 (fn worker-factory [n] #(barista-worker barista :orders)))

      ;; place orders
      (deref (place-order 1 client1 :orders client1-reply-queue client1-request-counter))
      (deref (place-order 2 client2 :orders client2-reply-queue client2-request-counter))
      (deref (place-order 1 client1 :orders client1-reply-queue client1-request-counter)))))
```



### Publish and Subscribe

Publish a message to a topic. All clients that have subscribed to a topic will receive
the messages on the topic. After subscribing to a topic the client turns into subscription
mode and listens for messages. To unsubscribe just close the IPC client.


**Synchronous publish**

```clojure
(do
  ;; thread-safe printing
  (defn println [& msg] (locking println (apply core/println msg)))

  (defn client-subscribe-handler [m]
    (println "SUBSCRIBED:" (ipc/message->json true m)))

  (try-with [server  (ipc/server 33333)
             client1 (ipc/client 33333)
             client2 (ipc/client 33333)]

    ;; client1 subscribes to messages with topic 'test'
    (ipc/subscribe client1 [:test] client-subscribe-handler)

    ;; client2 publishes a plain text message: 
    ;;   requestId="1", topic="test", payload="hello"
    (let [m (ipc/plain-text-message "1" "test" "hello")]
      (println "PUBLISHING:" (ipc/message->json true m))
      (->> (ipc/publish client2 m)
           (ipc/message->json true)
           (println "PUBLISHED:")))

    (sleep 300)))
```

**Asynchronous publish**

```clojure
(do
  ;; thread-safe printing
  (defn println [& msg] (locking println (apply core/println msg)))

  (defn client-subscribe-handler [m]
    (println "SUBSCRIBED:" (ipc/message->json true m)))

  (try-with [server  (ipc/server 33333)
             client1 (ipc/client 33333)
             client2 (ipc/client 33333)]

    ;; client1 subscribes to messages with topic 'test'
    (ipc/subscribe client1 [:test] client-subscribe-handler)

    ;; client2 publishes a plain text message: 
    ;;   requestId="1", topic="test", payload="hello"
    (let [m (ipc/plain-text-message "1" "test" "hello")]
      (println "PUBLISHING:" (ipc/message->json true m))
      (-<> (ipc/publish-async client2 m)
           (deref <> 1_000 :timeout)
           (ipc/message->json true <>)
           (println "PUBLISHED:" <>)))

    (sleep 300)))
```


## Messages


### Message Layout

```
  Fields                             Originator
  
 ┌───────────────────────────────┐
 │ ID                            │   send, offer/poll, publish/subscribe method
 ├───────────────────────────────┤
 │ Message Type                  │   send, offer/poll, publish/subscribe method
 ├───────────────────────────────┤
 │ Oneway                        │   client or framework method
 ├───────────────────────────────┤
 │ Response Status               │   server response processor
 ├───────────────────────────────┤
 │ Timestamp                     │   message creator
 ├───────────────────────────────┤
 │ ExpiresAt                     │   client (may be null)
 ├───────────────────────────────┤
 │ Timeout                       │   client (used as server-side queue offer/poll timeout)
 ├───────────────────────────────┤
 │ Request ID                    │   client (may be used for idempotency checks by the receiver)
 ├───────────────────────────────┤
 │ Topic                         │   client
 ├───────────────────────────────┤
 │ Queue Name                    │   client  (offer/poll, else null)
 ├───────────────────────────────┤
 │ ReplyTo Queue Name            │   client  (offer/poll, may be null)
 ├───────────────────────────────┤
 │ Payload Mimetype              │   client
 ├───────────────────────────────┤
 │ Payload Charset               │   client if payload data is a string else null
 ├───────────────────────────────┤
 │ Payload data                  │   client
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
  * `:NULL`        - a message with yet undefined type


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
;; message: request-id="1" topic=:test mimetype="text/plain" charset=:UTF-8 data="hello"
(->> (ipc/text-message "1" :test "text/plain" :UTF-8 "hello")
     (ipc/message->json true)
     (println))
```

```clojure
(->> """{"item": "espresso", "count": 2}"""
     (ipc/text-message "1" :order "application/json" :UTF-8)
     (ipc/message->json true)
     (println))
```


#### 3. Binary Messages

Text message payloads are defined by

  * a mimetype. E.g.:  `application/octet-stream`, `application/pdf`, ...
  * the binary data

```clojure
;; message: request-id="1" topic=:test mimetype="application/octet-stream" data=(bytebuf [0 1 2 3 4 5 6 7])
(->> (bytebuf [0 1 2 3 4 5 6 7])
     (ipc/binary-message "1" :test "application/octet-stream")
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
;; message: request-id="1" topic=:order data={:item "espresso", :count 2}
(->> (ipc/venice-message "1" :order {:item "espresso", :count 2})
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
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]
     (ipc/create-queue server "orders" 1_000)))
```

#### Remove Queue

```clojure
(do
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]
     (ipc/create-queue server "orders" 1_000)
     ;; ...
     (ipc/remove-queue server "orders")))
```

#### Check if Queue exists

```clojure
(do
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]
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
  * `:expires-at`       - the message's expiration time in milliseconds since epoch
  * `:request-id`       - the request ID (may be `nil`)
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
             client (ipc/client 33333)]
    (let [m (ipc/send client (ipc/text-message "1" "test" "text/plain" :UTF-8"Hello!"))]
      (println (ipc/message-field m :id))
      (println (ipc/message-field m :type))
      (println (ipc/message-field m :oneway?))
      (println (ipc/message-field m :timestamp))
      (println (ipc/message-field m :expires-at))
      (println (ipc/message-field m :response-status))
      (println (ipc/message-field m :request-id))
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
nil
:OK
1
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
             client (ipc/client 33333)]
    (let [m (ipc/send client (ipc/binary-message "1" "test" 
                                                 "application/octet-stream" 
                                                 (bytebuf [0 1 2 3 4 5 6 7])))]
      (println (ipc/message-field m :id))
      (println (ipc/message-field m :type))
      (println (ipc/message-field m :oneway?))
      (println (ipc/message-field m :timestamp))
      (println (ipc/message-field m :response-status))
      (println (ipc/message-field m :request-id))
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
1
test
application/octet-stream
nil
[0 1 2 3 4 5 6 7]
```


**Venice Data Message**

```clojure
(do
  (try-with [server (ipc/server 33333 (fn [m] m))
             client (ipc/client 33333)]
    (let [m (ipc/send client (ipc/venice-message "1" "test" {:a 100, :b 200} ))]
      (println (ipc/message-field m :id))
      (println (ipc/message-field m :type))
      (println (ipc/message-field m :oneway?))
      (println (ipc/message-field m :timestamp))
      (println (ipc/message-field m :response-status))
      (println (ipc/message-field m :request-id))
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
1
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



#### Check Message Expiration

```clojure
(do
  ;; thread-safe printing
  (defn println [& msg] (locking println (apply core/println msg)))

  (try-with [server (ipc/server 33333)
             client1 (ipc/client "localhost" 33333)
             client2 (ipc/client "localhost" 33333)]
    (ipc/create-queue server "orders" 1_000)

    ;; client1 offers an new order to the queue
    (let [order (ipc/venice-message "1" "order" {:item "espresso", :count 2})]
      (ipc/offer client1 "orders" 300 order))

    ;; client2 polls next order from the queue
    (let [m (ipc/poll client2 "orders" 300)]
      ;; Response message expired?
      (println "RESPONSE EXPIRED:" (ipc/message-expired? m)))))
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
