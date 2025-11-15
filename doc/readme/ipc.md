# Inter-Process-Communication


Venice Inter-Process-Communication (IPC), is a Venice API that allows application components to communicate with each other by sending and receiving messages. It enables asynchronous and loosely coupled communication, making it ideal for distributed applications. Venice IPC works by either exchanging messages between a client and a server or using a messaging provider, such as a server, that routes messages from producers to consumers through queues with either a single consumer (point-to-point) or multiple consumers (publish-subscribe).


*in work*


## IPC Communication Modes

### Send / Receive

*todo*


### Offer / Poll

*todo*


### Publish / Subscribe

*todo*



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
