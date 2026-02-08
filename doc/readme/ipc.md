# Inter-Process-Communication (Messaging)


Venice Inter-Process-Communication (IPC) is a Venice API that allows applications to send and receive messages. It enables asynchronous and loosely coupled communication, making it ideal for distributed applications. 

 
 

* [IPC Communication Modes](#ipc-communication-modes)
    * [Functions Send/Receive](#functions-send-receive)
    * [Queues Offer/Poll](#queues-offer-poll)
    * [Topics Publish/Subscribe](#topics-publish-subscribe)
* [Messages](#messages)
    * [Layout](#layout)
    * [Payload Types](#payload-types)
    * [Limit-Size](#limit-size)
    * [Message Utils](#message-utils)
* [Managing Destinations](#managing-destinations)
    * [Queues](#functions)
    * [Topics](#queues)
    * [Functions](#topics)
* [Authentication](#authentication)
* [ACL (Access Control Lists)](#acl-access-control-lists)
* [Compression](#compression)
* [Encryption](#encryption)
* [Benchmark](#benchmark)
* [Timeouts, Retries, and Idempotency in Distributed Systems](#timeouts-retries-and-idempotency-in-distributed-systems)

 
 

> [!NOTE]
> All examples require Venice 1.12.78+
>
> For API details please see the [cheatsheet](https://cdn.rawgit.com/jlangch/venice/6caed37/cheatsheet.pdf) under *Overview* -> *I/O* -> *Inter Process Communication*
>

 
 
 


## IPC Communication Modes

### Functions Send-Receive

Send a message from a client to a server and receive a response. The server's 
pluggable handler function computes the response from the request.


**Synchronous send / receive**

```clojure
(do
  ;; thread-safe printing to console
  (defn println [& msg] (locking println (apply core/println msg)))

  ;; a simple echo handler that just returns the request
  (defn echo-handler [m]
    (println "REQUEST:" (ipc/message->json true m)) 
    m)

  (try-with [server (ipc/server 33333)
             client (ipc/client "localhost" 33333)]

    (ipc/create-function server :echo echo-handler)

    ;; send a plain text message: requestId="1", subject=:test, payload="hello"
    (->> (ipc/plain-text-message "1" :test "hello")
         (ipc/send client :echo)  ;; send to echo-handler
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
          _          (assert (= :add topic))
          request-id (ipc/message-field m :request-id)
          request    (ipc/message-field m :payload-venice)
          result     {:z (+ (:x request) (:y request))}]
      (ipc/venice-message request-id topic result)))

  (try-with [server (ipc/server 33333)
             client (ipc/client "localhost" 33333)]

    (ipc/create-function server :handler handler)

    (->> (ipc/venice-message "1" :add {:x 100 :y 200})
         (ipc/send client :handler)
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

  (try-with [server (ipc/server 33333)
             client (ipc/client "localhost" 33333)]

    (ipc/create-function server :echo echo-handler)

    ;; send a plain text message: requestId="1", subject=:test, payload="hello"
    (let [msg       (ipc/plain-text-message "1" :test "hello")
          response  (ipc/send-async client :echo msg)]  ;; returns a future
      (->> (deref response 1_000 :timeout)  ;; deref the response future with 1s timeout
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

  (try-with [server (ipc/server 33333)
             client (ipc/client "localhost" 33333)]

    (ipc/create-function server :handler handler)

    ;; send a plain text messages: requestId="1" and "2", subject=:test, payload="hello"
    (ipc/send-oneway client :handler (ipc/plain-text-message "1" :test "hello"))
    (ipc/send-oneway client :handler (ipc/plain-text-message "2" :test "hello"))))
```



### Queues Offer-Poll

Offer messages to a queue and poll messages from a queue. More than one client can offer/poll
messages to/from queues but a message is delivered to one client only.


#### Offer and Poll with transient Queues

Transient queues and its messages live only as long as the servers lives.
 

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
    ;;   requestId="1" and "2", subject=:order, payload={:item "espresso", :count 2}
    (let [order (ipc/venice-message "1" :order {:item "espresso", :count 2})]
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
    ;;   requestId="1" and "2", subject=:order, payload={:item "espresso", :count 2}
    (let [order (ipc/venice-message "1" :order {:item "espresso", :count 2})]
      (println "ORDER:" (ipc/message->json true order))

      ;; client1 offers the order
      (-<> (ipc/offer-async client1 :orders 300 order)  ;; returns a future
           (deref <> 1_000 :timeout)
           (ipc/message->json true <>)
           (println "OFFERED:" <>)))

    ;; client2 polls next order from the queue
    (-<> (ipc/poll-async client2 :orders 300)  ;; returns a future
         (deref <> 1_000 :timeout)
         (ipc/message->json true <>)
         (println "POLLED:" <>))))
```


**Temporary queues**

Temporary queues can be created dynamically for use as a dedicated reply queue for a client. You can use these queues to ensure that a reply message can be sent to the appropriate client.

Temporary queues live only as long as the client, that created it, lives.

Coffee order example:

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

      ;; start the barista workers (1 worker currently)
      ;; reads orders from the :orders queue and replies to the order's reply-to queue
      (futures-fork 1 (fn worker-factory [n] #(barista-worker barista :orders)))

      ;; place orders and process the barista's order confirmation
      (deref (place-order 1 client1 :orders client1-reply-queue client1-request-counter))
      (deref (place-order 2 client2 :orders client2-reply-queue client2-request-counter))
      (deref (place-order 1 client1 :orders client1-reply-queue client1-request-counter)))))
```



#### Offer and Poll with durable Queues

Venice supports durable queues if the Write-Ahead-Log option is activated on 
the server. 
 

```clojure
(let [wal-dir (io/file (io/temp-dir "wal-"))]
  (try
    ;; start client/server with Write-Ahead-Log and offer a few messages
    (println "Starting server/client ...")
    (try-with [server (ipc/server 33333
                                  :write-ahead-log-dir wal-dir    ;; enable WAL
                                  :write-ahead-log-compress true  ;; compress WAL entries
                                  :write-ahead-log-compact true)  ;; compact WAL at startup
               client (ipc/client 33333)]

      (sleep 100)

      ;; create the durable queue :testq
      (ipc/create-queue server :testq 100 :bounded :durable)

      ;; offer 3 durable and 1 nondurable messages
      (ipc/offer client :testq 300 (ipc/plain-text-message "1" :test "hello 1" true))
      (ipc/offer client :testq 300 (ipc/plain-text-message "2" :test "hello 2" true))
      (ipc/offer client :testq 300 (ipc/plain-text-message "3" :test "hello 3" false))
      (ipc/offer client :testq 300 (ipc/plain-text-message "4" :test "hello 4" true))

      ;; poll message #1
      (let [m (ipc/poll client :testq 300)]
        (assert (ipc/response-ok? m))
        (assert (== "hello 1" (ipc/message-field m :payload-text)))))
    (println "Shutdown server/client")
    
    (sleep 100)

    ;; restart client/server to test Write-Ahead-Logs 
    ;; the new server will read the Write-Ahead-Logs and populate the queue :testq
    (println "Restarting server/client ...")
    (try-with [server (ipc/server 33333
                                  :write-ahead-log-dir wal-dir
                                  :write-ahead-log-compress true
                                  :write-ahead-log-compact true)
               client (ipc/client 33333)]

      (sleep 100)

      ;; create the durable queue :testq
      ;; if the queue already exists due to the WAL recovery process, this
      ;; queue create request will just be skipped!
      (ipc/create-queue server :testq 100 :bounded :durable)

      ;; poll message #2
      (let [m (ipc/poll client :testq 300)]
        (assert (ipc/response-ok? m))
        (assert (== "hello 2" (ipc/message-field m :payload-text))))

      ;; message #3 is nondurable and therefore lost at server shutdown

      ;; poll message #4
      (let [m (ipc/poll client :testq 300)]
        (assert (ipc/response-ok? m))
        (assert (== "hello 4" (ipc/message-field m :payload-text)))))
    (println "Shutdown server/client")
    
    (sleep 100)

    (finally 
      (io/delete-file-tree wal-dir)
      (println "WAL dir cleaned and removed")))
    
  (println "Done."))
```



### Topics Publish-Subscribe

Publish a message to a topic. All clients that have subscribed to a topic will receive
the messages on the topic. After subscribing to a topic the client turns into subscription
mode and listens for messages. To unsubscribe call `(ipc/unsubscribe client topic)` or just 
close the IPC client.


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

    ;; create topic ':test'
    (ipc/create-topic server :test)

    ;; client1 subscribes to messages with topic ':test'
    (ipc/subscribe client1 :test client-subscribe-handler)

    ;; client2 publishes a plain text message: 
    ;;   requestId="1", subject=:testing, payload="hello"
    (let [m (ipc/plain-text-message "1" :testing "hello")]
      (println "PUBLISHING:" (ipc/message->json true m))
      (->> (ipc/publish client2 :test m)
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

    ;; create topic ':test'
    (ipc/create-topic server :test)
    
    ;; client1 subscribes to messages with topic 'test'
    (ipc/subscribe client1 :test client-subscribe-handler)

    ;; client2 publishes a plain text message: 
    ;;   requestId="1", subject=:testing, payload="hello"
    (let [m (ipc/plain-text-message "1" :testing "hello")]
      (println "PUBLISHING:" (ipc/message->json true m))
      (-<> (ipc/publish-async client2 :test m)   ;; returns a future
           (deref <> 1_000 :timeout)
           (ipc/message->json true <>)
           (println "PUBLISHED:" <>)))

    (sleep 300)))
```

 
 

## Messages


### Layout

```
  Fields                             Originator
  
 ┌───────────────────────────────┐
 │ ID                            │   send, offer/poll, publish/subscribe method
 ├───────────────────────────────┤
 │ Message Type                  │   send, offer/poll, publish/subscribe method
 ├───────────────────────────────┤
 │ Oneway                        │   client or framework method
 ├───────────────────────────────┤
 │ Durable                       │   client or framework method
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
 │ Subject                       │   client
 ├───────────────────────────────┤
 │ Destination Name              │   client  
 ├───────────────────────────────┤
 │ ReplyTo Queue Name            │   client  (offer, may be null)
 ├───────────────────────────────┤
 │ Payload Mimetype              │   client
 ├───────────────────────────────┤
 │ Payload Charset               │   client if payload data is a string else null
 ├───────────────────────────────┤
 │ Payload data                  │   client
 └───────────────────────────────┘
```

**Types**

  * `:REQUEST`            - a request message
  * `:PUBLISH`            - a publish message
  * `:SUBSCRIBE`          - a subscribe message
  * `:UNSUBSCRIBE`        - an unsubscribe message
  * `:OFFER`              - an offer message for a queue
  * `:POLL`               - a poll message from a queue
  * `:CREATE_QUEUE`       - a queue create request message
  * `:CREATE_TEMP_QUEUE`  - a temporary queue create request message
  * `:REMOVE_QUEUE`       - a queue remove request message
  * `:STATUS_QUEUE`       - a queue status request message
  * `:CREATE_TOPIC`       - a topic create request message
  * `:REMOVE_TOPIC`       - a topic remove request message
  * `:CREATE_FUNCTION`    - a function create request message
  * `:REMOVE_FUNCTION`    - a function remove request message
  * `:RESPONSE`           - a response to a request message
  * `:NULL`               - a message with yet undefined type


**Response Status**

  * `:OK`               - a response message for a successfully processed request
  * `:SERVER_ERROR`      - a response indicating a server side error while processing the request 
  * `:BAD_REQUEST`       - invalid request
  * `:HANDLER_ERROR`     - a server handler error in the server's request processing
  * `:QUEUE_NOT_FOUND`   - the required queue does not exist
  * `:QUEUE_EMPTY`       - the adressed queue in a poll request is empty
  * `:QUEUE_FULL`        - the adressed queue in offer request is full
  * `:TOPIC_NOT_FOUND`   - the required topic does not exist
  * `:FUNCTION_NOT_FOUND` - the required function does not exist
  * `:NULL`              - a message with yet undefined status, filled when processing the message

 

### Payload Types

Venice IPC supports messages with various payload types:

  * plain text
  * text (json, xml, ...)
  * binary data
  * Venice data



#### 1. Plain Text Messages

```clojure
;; plain-text-message: request-id="1" subject="test" data="hello"
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
;; message: request-id="1" subject=:test mimetype="text/plain" charset=:UTF-8 data="hello"
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
;; message: request-id="1" subject=:test mimetype="application/octet-stream" data=(bytebuf [0 1 2 3 4 5 6 7])
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
;; message: request-id="1" subject=:order data={:item "espresso", :count 2}
(->> (ipc/venice-message "1" :order {:item "espresso", :count 2})
     (ipc/message->json true)
     (println))
```

 

### Limit Size

By default messages are limited to 20 MB size (not encrypted, not compressed).

The message size limit can be configured on the server in the range of 2KB ... 250MB.

```clojure
(try-with [server (ipc/server 33333 :max-message-size :100MB)]
  ;;
  )
```

```clojure
(try-with [server (ipc/server 33333 :max-message-size :200KB)]
  ;;
  )
```

 

### Message Utils


#### Accessing Message Fields

**Supported field names:** 

  * `:id`               - the message's technical ID
  * `:type`             - the message type (request, response, ..) 
  * `:oneway?`          - `true` if one-way message else `false`
  * `:durable?`         - `true` if durable message else `false`
  * `:response-status`  - the response status (ok, bad request, ...) 
  * `:timestamp`        - the message's creation timestamp in milliseconds since epoch
  * `:expires-at`       - the message's expiration time in milliseconds since epoch
  * `:request-id`       - the request ID (may be `nil`)
  * `:subject`          - the subject
  * `:destination-name`  - the destination name (may be `nil`)
  * `:reply-to-queue-name`   - the reply-to queue name 
  * `:payload-mimetype` - the payload data mimetype
  * `:payload-charset`  - the payload data charset if payload is a text form else `nil`
  * `:payload-text`     - the payload converted to text data if payload is textual data else `nil`
  * `:payload-binary`   - the payload binary data (the raw message binary data)
  * `:payload-venice`   - the payload converted venice data if mimetype is 'application/json' else error


**Text Message**

```clojure
(do
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]

    (ipc/create-function server :echo (fn [m] m))

    (let [m (ipc/send client :echo (ipc/text-message "1" "test" "text/plain" :UTF-8"Hello!"))]
      (println (ipc/message-field m :id))
      (println (ipc/message-field m :type))
      (println (ipc/message-field m :oneway?))
      (println (ipc/message-field m :timestamp))
      (println (ipc/message-field m :expires-at))
      (println (ipc/message-field m :response-status))
      (println (ipc/message-field m :request-id))
      (println (ipc/message-field m :subject))
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
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]

    (ipc/create-function server :echo (fn [m] m))

    (let [m (ipc/send client :echo (ipc/binary-message "1" "test" 
                                                 "application/octet-stream" 
                                                 (bytebuf [0 1 2 3 4 5 6 7])))]
      (println (ipc/message-field m :id))
      (println (ipc/message-field m :type))
      (println (ipc/message-field m :oneway?))
      (println (ipc/message-field m :timestamp))
      (println (ipc/message-field m :response-status))
      (println (ipc/message-field m :request-id))
      (println (ipc/message-field m :subject))
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
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]

    (ipc/create-function server :echo (fn [m] m))

    (let [m (ipc/send client :echo (ipc/venice-message "1" "test" {:a 100, :b 200} ))]
      (println (ipc/message-field m :id))
      (println (ipc/message-field m :type))
      (println (ipc/message-field m :oneway?))
      (println (ipc/message-field m :timestamp))
      (println (ipc/message-field m :response-status))
      (println (ipc/message-field m :request-id))
      (println (ipc/message-field m :subject))
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
  (try-with [server (ipc/server 33333)
             client (ipc/client "localhost" 33333)]

    (ipc/create-function server :echo (fn [m] m))

    ;; send a plain text message: requestId="1", subject=:test, payload="hello"
    (->> (ipc/plain-text-message "1" :test "hello")
         (ipc/send client :echo)
         (ipc/message->json true)    ;; formatted JSON enabled
         (println "RESPONSE:"))))
```


#### Convert Message to Venice Map

```clojure
(do
  (try-with [server (ipc/server 33333)
             client (ipc/client "localhost" 33333)]

    (ipc/create-function server :echo (fn [m] m))

    ;; send a plain text message: requestId="1", subject=:test, payload="hello"
    (->> (ipc/plain-text-message "1" :test "hello")
         (ipc/send client :echo)
         (ipc/message->map)
         (println "RESPONSE:"))))
```


#### Check Message Response Status OK

```clojure
(do
  (try-with [server (ipc/server 33333)
             client (ipc/client "localhost" 33333)]

    (ipc/create-function server :echo (fn [m] m))

    ;; send a plain text message: requestId="1", subject=:test, payload="hello"
    (->> (ipc/plain-text-message "1" :test "hello")
         (ipc/send client :echo)
         (ipc/response-ok?)
         (println "RESPONSE OK:"))))
```


#### Check Message Response Status Error

```clojure
(do
  (try-with [server (ipc/server 33333)
             client (ipc/client "localhost" 33333)]

    (ipc/create-function server :echo (fn [m] m))

    ;; send a plain text message: requestId="1", subject=:test, payload="hello"
    (->> (ipc/plain-text-message "1" :test "hello")
         (ipc/send client :echo)
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
    (ipc/create-queue server :orders 1_000)

    ;; client1 offers an new order to the queue (expires within 5 minutes)
    (let [order (ipc/venice-message "1" :order 
                                    {:item "espresso", :count 2} 
                                    false  ;; nondurable
                                    5 :minutes)]
      (ipc/offer client1 :orders 300 order))

    ;; client2 polls next order from the queue
    (let [m (ipc/poll client2 :orders 300)]
      ;; Response message expired?
      (println "RESPONSE EXPIRED:" (ipc/message-expired? m)))))
```

 
 

## Managing Destinations

> [!NOTE]
> All destination management functions require an 'admin' user when called
> from a client node! 

### Queues

#### Create Bounded and Circular Queues

Create through 'server'

```clojure
(do
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]

    (ipc/create-queue server :queue/1 100 :bounded)
    (ipc/create-queue server :queue/2 100 :circular)

    (ipc/offer client :queue/1 300 
               (ipc/plain-text-message "1" :test "hello"))

    (ipc/offer client :queue/2 300 
               (ipc/plain-text-message "2" :test "hello"))))
```


Create through 'client' (requires 'admin' user)

```clojure
(do
  (let [auth (ipc/authenticator)]
    (ipc/add-credentials auth "max" "756")         ;; normal user
    (ipc/add-credentials auth "tom" "123" :admin)  ;; admin user

    (try-with [server (ipc/server 33333 :encrypt true :authenticator auth)
               client (ipc/client 33333 :user-name "tom" :password "123")]

      (ipc/create-queue client :queue/1 100 :bounded)
      (ipc/create-queue client :queue/2 100 :circular)

      (ipc/offer client :queue/1 300 
                 (ipc/plain-text-message "1" :test "hello"))

      (ipc/offer client :queue/2 300 
                 (ipc/plain-text-message "2" :test "hello")))))
```


#### Create Bounded Durable Queues

> [!NOTE]
> To use durable queues the server must be started with Write-Ahead-Log enabled!
>
> Only bounded queues can be made durable!
> 
> Servers with Write-Ahead-Lopg enabled support all types of queues: bounded/durable, 
> bounded, circular, and temporary


```clojure
(let [wal-dir (io/file (io/temp-dir "wal-"))]
  (try-with [server (ipc/server 33333
                                :write-ahead-log-dir wal-dir    ;; enable WAL
                                :write-ahead-log-compress true  ;; compress WAL entries
                                :write-ahead-log-compact true)  ;; compact WAL at startup
             client (ipc/client 33333)]
    (ipc/create-queue server :queue/1 100 :bounded :durable)
    (ipc/offer client :queue/1 300 
               (ipc/plain-text-message "1" :test "hello"))
    (finally (io/delete-file-tree wal-dir))))
```


#### Create Temporary Queues

Temporary queues can only be created on behalf of a client. They only live 
as long as the client lives!

```clojure
(do
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]
    (let [queue-name (ipc/create-temporary-queue client 100)]
      (ipc/offer client queue-name 300 
                 (ipc/plain-text-message "1" :test "hello")))))
```


#### Remove Queues

> [!NOTE]
> If the queue is durable, its Write-Ahead-Log will be removed as well!

```clojure
(do
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]
    
    (ipc/create-queue server :queue/1 100)
    ;; ...
    (ipc/remove-queue server :queue/1)))
```


#### Check if a Queue exists

```clojure
(do
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]
    
    (ipc/create-queue server :queue/1 100)
    ;; ...
    (ipc/exists-queue? server :queue/1)))
```


#### Check Queue Status

for bounded or circular queues

```clojure
(do
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]
    
    (ipc/create-queue server :queue/1 100)
     
    (ipc/offer client :queue/1 300 
               (ipc/plain-text-message "1" :test "hello"))
    ;; ...
    (ipc/queue-status server :queue/1)))
```

 

### Topics

#### Create Topics

Create through 'server'

```clojure
(do
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]

    (ipc/create-topic server :topic/1)
    (ipc/create-topic server :topic/2)

    (ipc/publish client :topic/1 (ipc/plain-text-message "1" :test "hello"))))
```


Create through 'client' (requires 'admin' user)

```clojure
(do
  (let [auth (ipc/authenticator)]
    (ipc/add-credentials auth "max" "756")         ;; normal user
    (ipc/add-credentials auth "tom" "123" :admin)  ;; admin user

    (try-with [server (ipc/server 33333 :encrypt true :authenticator auth)
               client (ipc/client 33333 :user-name "tom" :password "123")]

      (ipc/create-topic client :topic/1)
      (ipc/create-topic client :topic/2)

      (ipc/publish client :topic/1 (ipc/plain-text-message "1" :test "hello")))))
```

#### Remove Topics

```clojure
(do
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]
    
    (ipc/create-topic server :topic/1)
    ;; ...
    (ipc/remove-topic server :topic/1)))
```


### Functions

> [!NOTE]
> Functions can be created from a server only!


#### Create Functions

```clojure
(do
  (defn echo-handler [m] m)
  
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]

    (ipc/create-function server :echo echo-handler)

    ;; send a plain text message: requestId="1", subject=:test, payload="hello"
    (->> (ipc/plain-text-message "1" :test "hello")
         (ipc/send client :echo)  ;; send to echo-handler
         (ipc/message->json true)
         (println "RESPONSE:"))))
```


#### Remove Functions

```clojure
(do
  (defn echo-handler [m] m)
  
  (try-with [server (ipc/server 33333)
             client (ipc/client 33333)]

    (ipc/create-function server :echo echo-handler)
    ;; ...
    (ipc/remove-function server :echo)))
```

 
 

## Authentication

Authentication secures IPC connections by requiring a username and password when a 
client creates a connection. This process verifies the client's identity, ensuring only 
authorized users/applications can access the messaging infrastructure.

> [!NOTE]
> For security reasons encryption needs to be enabled on the server to securely send the
> user credentials from a client to the server!
>
> Passwords are stored as salted PBKDF2 hashes on the server!

```clojure
(do
  (defn echo-handler [m]
    (println "REQUEST:  " (ipc/message->map m))
    m)

  (let [auth (ipc/authenticator)]                ;; create an authenticator
    (ipc/add-credentials auth "tom" "3,kio")     ;; add test credentials
    
    (try-with [server (ipc/server 33333
                                  :encrypt true         ;; enable encryption
                                  :authenticator auth)  ;; pass the authenticator to the server
               client (ipc/client "localhost" 33333
                                  :user-name "tom"
                                  :password "3,kio")]   ;; client connection with the credentials

      (ipc/create-function server :echo echo-handler)

      (->> (ipc/plain-text-message "1" :test "hello")
           (ipc/send client :echo)
           (ipc/message->map)
           (println "RESPONSE: ")))))
```

**Server authenticators can be stored/loaded from a file**

```clojure
(do
  (def auth-file (io/file "./ipc.cred"));
  
  ;; Create an authenticator and store it to "./ipc.cred"
  (let [auth (ipc/authenticator)]
    (ipc/add-credentials auth "tom" "3-kio")
    (ipc/add-credentials auth "max" "zu*67" :admin)  ;; user 'max' has 'admin' authorization
    (ipc/store-authenticator auth auth-file))

  ;; Load authenticator from "./ipc.cred"
  (let [auth (ipc/load-authenticator auth-file)]
    (try-with [server (ipc/server 33333
                                  :encrypt true
                                  :authenticator auth)
               client (ipc/client "localhost" 33333
                                  :user-name "tom"
                                  :password "3-kio")]

      (ipc/create-function server :echo (fn [m] m))

      (->> (ipc/plain-text-message "1" :test "hello")
           (ipc/send client :echo)
           (ipc/message->map)
           (println "RESPONSE: ")))))
```

 
 

## ACL (Access Control Lists)

ACLs (Access Control Lists) regulate access to destinations such as queues, topics, or functions 
in order to secure message flows. They validate user permissions when authentication mode is enabled 
by defining who is allowed to produce or consume messages. These mechanisms are based on the 
principal (authenticated user) and access control items.

> [!NOTE]
> ACLs are enabled implicitly if authentication is activated on the server.
>
> Users with `:admin` role can access all operations on all queues, topics, and functions. No ACL is
required!
>


 

ACLs define a list of access permissions for principals on a destination. Five access modes can be 
used to to define permissions:


| Destination Type | Supported Access Modes | Description                                |
| :--              | :--                    | :--                                        |
| `:queue`          | `:poll`                | allow to poll messages from queues         |
|                  | `:offer`                | allow to offer messages to queues          |
|                  | `:offer-poll`           | allow to offer/poll messages on queues     |
|                  | `:deny`                 | prevent accessing queues                   |
| `:topic`          | `:subscribe`            | allow to subscribe to topics               |
|                  | `:publish`              | allow to publish to topics                 |
|                  | `:publish-subscribe`     | allow to publish/subscribe to topics       |
|                  | `:deny`                 | prevent accessing topics                   |
| `:function`       | `:exec`                 | allow to execute functions                 |
|                  | `:deny`                 | prevent executing functions                |


### Default ACLs

An IPC authenticator defines system default ACLs for all three destination types (queues, topics, 
and functions). 

| Destination Type | Default Access Mode   | Description                                     |
| :--              | :--                   | :--                                             |
| `:queue`          | `:offer-poll`          | all users can offer/poll messages on any *queue* |
| `:topic`          | `:publish-subscribe`    | all users can subscribe/publish to any *topic*  |
| `:function`       | `:exec`                | all users can execute any *function*            |

This allows users to access all queues, topics, and functions as long as there are no explicit
access control rules for a user on a destination to override the defaults. 


### Custom default ACLs

The default destination ACLs can be customized to e.g. require explicit user access control
for each destination:

| Destination Type | Default Access Mode   | Description                                |
| :--              | :--                   | :--                                        |
| `:queue`          | `:deny`                | prevent all users from accessing queues    |
| `:topic`          | `:deny`                | prevent all users from accessing topics    |
| `:function`       | `:deny`                | prevent all users from accessing functions |

This custom default ACL setup can be achieved with:

```clojure
(let [auth (ipc/authenticator)]
  (ipc/add-credentials auth "max" "zu*67")          ;; user 'max'
  (ipc/add-credentials auth "tom" "3-kio")          ;; user 'tom'
  
  ;; custom default ACLs
  (ipc/default-acl auth :queue    :deny)            ;; prevent all users from accessing queues
  (ipc/default-acl auth :topic    :deny)            ;; prevent all users from accessing topics
  (ipc/default-acl auth :function :deny)            ;; prevent all users from accessing functions
  
  ;; Overrides for specific users
  (ipc/add-acl auth :queue :queue/1 :poll  "tom")   ;; allow user 'tom' to poll messages from :queue/1
  (ipc/add-acl auth :queue :queue/1 :offer "max")   ;; allow user 'max' to offer messages to :queue/1
)
```


 

### Principal based access control for Queues

Grant specific principals (users) to:
  - poll messages from a queue
  - offer messages to a queue
  - deny accessing a queue

| Authorization          | Access Mode  | Example                                         |
| :--                    | :--          | :--                                             |
| poll from a queue      | `:poll`       | `(ipc/add-acl auth :queue :queue/1 :poll "user1")`  |
| offer to a queue       | `:offer`      | `(ipc/add-acl auth :queue :queue/1 :offer "user2")` |
| deny accessing a queue | `:deny`       | `(ipc/add-acl auth :queue :queue/1 :deny "user3")`  |

*Any number of access control items can be assigned to a principal (user)*

```clojure
(do
  ;; Create an authenticator with ACLs
  (let [auth (ipc/authenticator)]
    (ipc/add-credentials auth "jak" "io-96")              ;; user 'jak'
    (ipc/add-credentials auth "pax" "ph$54")              ;; user 'pax'
    (ipc/add-credentials auth "tom" "3-kio")              ;; user 'tom'
    (ipc/add-credentials auth "jon" "ph$54")              ;; user 'jon'
    (ipc/add-credentials auth "max" "zu*67" :admin)       ;; user 'max' (admin)
 
    (ipc/add-acl auth :queue :queue/1 :poll "jak")        ;; :queue/1 allow poll only
    (ipc/add-acl auth :queue :queue/1 :poll "jak")        ;; :queue/2 allow poll only
    (ipc/add-acl auth :queue :queue/1 :offer "pax")       ;; :queue/1 allow offer only
    (ipc/add-acl auth :queue :queue/1 :deny "jon")        ;; :queue/1 deny offer/poll

    (try-with [server    (ipc/server 33333 :encrypt true :authenticator auth)
               clientPax (ipc/client "localhost" 33333 :user-name "pax" :password "ph$54")
               clientJak (ipc/client "localhost" 33333 :user-name "jak" :password "io-96")]

      (ipc/create-queue server :queue/1 100)

      ;; 'pax' offers a message ('pax' has :write access to :queue/1)
      (ipc/offer clientPax :queue/1 300 (ipc/plain-text-message "1" :test "hello"))

      ;; 'jak' polls a message ('jak' has :read access to :queue/1)
      (->> (ipc/poll clientJak :queue/1 300)
           (ipc/message->map)
           (println "POLLED: ")))))
```


### Principal based access control for Topics

Grant specific principals (users) to:
  - subscribe to a topic
  - publish messages to a topic
  - deny accessing a topic

| Authorization          | Access Mode  | Example                                           |
| :--                    | :--          | :--                                               |
| subscribe to a topic   | `:subscribe`  | `(ipc/add-acl auth :topic :topic/1 :subscribe "user1")`|
| publish to a topic     | `:publish`    | `(ipc/add-acl auth :topic :topic/1 :publish "user2")`  |
| deny accessing a topic | `:deny`       | `(ipc/add-acl auth :topic :topic/1 :deny "user3")`    |

*Any number of access control items can be assigned to a principal (user)*

```clojure
(do
  ;; thread-safe printing
  (defn println [& msg] (locking println (apply core/println msg)))

  (defn jaks-subscription-handler [m]
    (println "MESSAGE (jak):" (ipc/message->json true m)))

  ;; Create an authenticator with ACLs
  (let [auth (ipc/authenticator)]
    (ipc/add-credentials auth "jak" "io-96")              ;; user 'jak'
    (ipc/add-credentials auth "pax" "ph$54")              ;; user 'pax'
    (ipc/add-credentials auth "tom" "3-kio")              ;; user 'tom'
    (ipc/add-credentials auth "jon" "ph$54")              ;; user 'jon'
    (ipc/add-credentials auth "max" "zu*67" :admin)       ;; user 'max' (admin)

    (ipc/add-acl auth :topic :topic/1 :subscribe "jak")         ;; :topic/1 allow subscribe only
    (ipc/add-acl auth :topic :topic/2 :subscribe "jak")         ;; :topic/2 allow subscribe only
    (ipc/add-acl auth :topic :topic/1 :publish "pax")           ;; :topic/1 allow publish only
    (ipc/add-acl auth :topic :topic/1 :publish-subscribe "tom") ;; :topic/1 allow publish/subscribe
    (ipc/add-acl auth :topic :topic/1 :deny "jon")               ;; :topic/1 deny publish/subscribe

    (try-with [server    (ipc/server 33333 :encrypt true :authenticator auth)
               clientPax (ipc/client "localhost" 33333 :user-name "pax" :password "ph$54")
               clientJak (ipc/client "localhost" 33333 :user-name "jak" :password "io-96")]

      (ipc/create-topic server :topic/1)

      ;; 'jak' subscribes for :topic/1 message ('jak' has :read access to :topic/1)
      (ipc/subscribe clientJak :topic/1 jaks-subscription-handler)

      ;; 'pax' publishes a message ('pax' has :write access to :topic/1)
      (ipc/publish clientPax :topic/1 (ipc/plain-text-message "1" :test "hello"))

      (sleep 500))))
```


### Principal based access control for Functions

Grant specific principals (users) to:
  - execute a function
  - deny accessing a function

| Authorization             | Access Mode  | Example                                          |
| :--                       | :--          | :--                                              |
| execute a function        | `:execute`    | `(ipc/add-acl auth :function :echo :execute "user1")` |
| deny accessing a function | `:deny`       | `(ipc/add-acl auth :function :echo :deny "user2")`   |

*Any number of access control items can be assigned to a principal (user)*

```clojure
(do
  ;; Create an authenticator with ACLs
  (let [auth (ipc/authenticator)]
    (ipc/add-credentials auth "jak" "io-96")            ;; user 'jak'
    (ipc/add-credentials auth "jon" "ph$54")            ;; user 'jon'
    (ipc/add-credentials auth "max" "zu*67" :admin)     ;; user 'max' (admin)

    (ipc/add-acl auth :function :echo :execute "jak")   ;; function :echo allow execute
    (ipc/add-acl auth :function :order :execute "jak")  ;; function :order allow execute
    (ipc/add-acl auth :function :echo :deny "jon")      ;; function :echo deny execute

    (try-with [server    (ipc/server 33333 :encrypt true :authenticator auth)
               clientJak (ipc/client "localhost" 33333 :user-name "jak" :password "io-96")]

      (ipc/create-function server :echo (fn [m] m))

      ;; 'jak' sends a message ('jak' has :execute access to function :echo)
      (->> (ipc/plain-text-message "1" :test "hello")
           (ipc/send clientJak :echo)
           (ipc/message->map)
           (println "RECEIVED: ")))))
```

 
 

## Compression

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
  
  ;; transparently compress/decompress messages with a size > 1KB
  (try-with [server (ipc/server 33333 :compress-cutoff-size :1KB)
             client (ipc/client "localhost" 33333)]

    (ipc/create-function server :echo echo-handler)

    ;; send a plain text message: requestId="1", subject=:test, payload="hello"
    (->> (ipc/plain-text-message "1" :test "hello")
         (ipc/send client :echo)
         (ipc/message->json true)
         (println "RESPONSE:"))))
```

 
 

## Encryption

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
  (try-with [server (ipc/server 33333 :encrypt true)
             client (ipc/client "localhost" 33333)]

    (ipc/create-function server :echo echo-handler)

    ;; send a plain text message: requestId="1", subject=:test, payload="hello"
    (->> (ipc/plain-text-message "1" :test "hello")
         (ipc/send client :echo)
         (ipc/message->json true)
         (println "RESPONSE:"))))
```

 
 

## Benchmark


### MacOS 26

MacBook Air M2, Venice 1.12.74

 

**AF_INET** tcp/ip sockets

*Java 8, single connection, single thread*

| Payload bytes    | 5 KB        | 50 KB       | 500 KB      | 5 MB       | 50 MB     | 200 MB    |
| :--              | :--         | :--         | :--         | :--        | :--       | :--       |
| Throughput msgs  | 30578 msg/s | 23317 msg/s | 9498 msg/s  | 1552 msg/s | 122 msg/s | 30 msg/s  |
| Throughput bytes | 149 MB/s    | 1139 MB/s   | 4638 MB/s   | 7760 MB/s  | 6104 MB/s | 5953 MB/s |

 

*Java 8, multiple connections, 1 thread per connection*

| Payload bytes 5KB | 1 conn      | 2 conn      | 3 conn      | 4 conn      | 10 conn      | 100 conn     |
| :--               | :--         | :--         | :--         | :--         | :--          | :--          |
| Throughput msgs   | 30578 msg/s | 57340 msg/s | 80033 msg/s | 93818 msg/s | 112331 msg/s | 119498 msg/s |
| Throughput bytes  | 149 MB/s    | 280 MB/s    | 391 MB/s    | 458 MB/s    | 548 MB/s     | 583 MB/s     |

 

*Java 21, single connection, single thread*

| Payload bytes    | 5 KB        | 50 KB       | 500 KB      | 5 MB       | 50 MB     | 200 MB    |
| :--              | :--         | :--         | :--         | :--        | :--       | :--       |
| Throughput msgs  | 34782 msg/s | 24099 msg/s | 9548 msg/s  | 1545 msg/s | 114 msg/s | 29 msg/s  |
| Throughput bytes | 170 MB/s    | 1177 MB/s   | 4662 MB/s   | 7724 MB/s  | 5691 MB/s | 5797 MB/s |

 

*Java 21, multiple connections, 1 thread per connection*

| Payload 5KB      | 1 conn      | 2 conn      | 3 conn      | 4 conn      | 10 conn      | 100 conn     |
| :--              | :--         | :--         | :--         | :--         | :--          | :--          |
| Throughput msgs  | 34782 msg/s | 60245 msg/s | 83154 msg/s | 97530 msg/s | 120214 msg/s | 119482 msg/s |
| Throughput bytes | 170 MB/s    | 294 MB/s    | 406 MB/s    | 476 MB/s    | 587 MB/s     | 583 MB/s     |

 

**AF_UNIX** Unix domain sockets

*Java 8, default socket snd/rcv buffer size, single connection, single thread*

| Payload bytes    | 5 KB        | 50 KB       | 500 KB      | 5 MB      | 50 MB     | 200 MB    |
| :--              | :--         | :--         | :--         | :--       | :--       | :--       |
| Throughput msgs  | 49122 msg/s | 20003 msg/s | 3591 msg/s  | 7 msg/s   | - msg/s   | - msg/s   |
| Throughput bytes | 240 MB/s    | 977 MB/s    | 1754 MB/s   | 34 MB/s   | - MB/s    | - MB/s    |

 

**AF_UNIX** Unix domain sockets

*Java 8, 1MB socket snd/rcv buffer size, single connection, single thread*

| Payload bytes    | 5 KB        | 50 KB       | 500 KB      | 5 MB       | 50 MB     | 200 MB    |
| :--              | :--         | :--         | :--         | :--        | :--       | :--       |
| Throughput msgs  | 49013 msg/s | 38756 msg/s | 13616 msg/s | 414 msg/s | 7.3 msg/s | 0.5 msg/s |
| Throughput bytes | 239 MB/s    | 1892 MB/s   | 6649 MB/s   | 2072 MB/s | 365 MB/s  | 96 MB/s   |

 

*Java 8, 1MB socket snd/rcv buffer size, multiple connections, 1 thread per connection (limit 53 connections!)*

| Payload 5KB      | 1 conn      | 2 conn      | 3 conn       | 4 conn       | 10 conn      | 50 conn      |
| :--              | :--         | :--         | :--          | :--          | :--          | :--          |
| Throughput msgs  | 49013 msg/s | 88937 msg/s | 120589 msg/s | 130190 msg/s | 137519 msg/s | 137386 msg/s |
| Throughput bytes | 239 MB/s    | 434 MB/s    | 589 MB/s     | 636 MB/s     | 671 MB/s     | 871 MB/s     |

 

### AlmaLinux 9

VMWare, Intel(R) Xeon(R) Silver 4214 CPU @ 2.20GHz, 2 cores with 1 thread each, Venice 1.12.74

 

**AF_INET** tcp/ip sockets

*Java 8, single connection, single thread*

| Payload bytes    | 5 KB        | 50 KB       | 500 KB     | 5 MB      | 50 MB     | 200 MB    |
| :--              | :--         | :--         | :--        | :--       | :--       | :--       |
| Throughput msgs  | 26012 msg/s | 16065 msg/s | 3419 msg/s | 345 msg/s | 34 msg/s  | 8.5 msg/s |
| Throughput bytes | 127 MB/s    | 784 MB/s    | 1669 MB/s  | 1727 MB/s | 1681 MB/s | 1708 MB/s |

 

*Java 8, multiple connections, 1 thread per connection*

| Payload 5KB      | 1 conn      | 2 conn      | 3 conn      | 4 conn      | 10 conn     | 100 conn    |
| :--              | :--         | :--         | :--         | :--         | :--         | :--         |
| Throughput msgs  | 26012 msg/s | 47891 msg/s | 50588 msg/s | 48312 msg/s | 48265 msg/s | 39386 msg/s |
| Throughput bytes | 127 MB/s    | 234 MB/s    | 247 MB/s    | 234 MB/s    | 236 MB/s    | 192 MB/s    |

 



**Test scenario:** 

*IPC client and server colocated, compression and encryption turned off*

*The client sends messages with a defined payload size, and the server responds with a simple acknowledge message. Throughput measurements consider only the client-sent messages.*


```clojure
;; tcp/ip socket
(ipc/benchmark "af-inet://localhost:33333"
               :5KB                         ;; 5KB payload size
               5                            ;; 5s duration
               :print true                  ;; print results
               :ramp-up 1)                  ;; ramp-up phase 1s

;; Unix domain socket
(ipc/benchmark "af-unix:///path/to/test.sock"
               :5KB                         ;; 5KB payload size
               5                            ;; 5s duration
               :print true                  ;; print results
               :socket-snd-buf-size :256KB  ;; socket send buffer size
               :socket-rcv-buf-size :256KB  ;; socket receiver buffer size
               :ramp-up 1)                  ;; ramp-up phase 1s
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
