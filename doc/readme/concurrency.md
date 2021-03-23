(map (fn [x] (. :Semaphore :new 1)))))# Concurrency

* [Atoms](#atoms)
* [Futures & Promises](#futures-and-promises)
* [Delays](#delays)
* [Agents](#agents)
* [Worker Threads](#worker-threads)
* [Locking](#locking)
* [Schedulers](#schedulers)
* [Thread local vars](#thread-local-vars)
* [Example Dining Philosophers](#example-dining-philosophers)


## Atoms

Atoms provide uncoordinated, synchronous access to a single identity. 
The primary use of an atom is to hold Venice’s immutable data structures. 
The value held by an atom is changed with the `swap!` method.

```clojure
(do
   (def counter (atom 2))
   (swap! counter + 2)
   (deref counter))
```

## Futures and Promises

A future takes a function and yields a future object that will invoke the function 
in another thread, and will cache the result and return it on all subsequent calls 
to deref. If the computation has not yet finished, calls to deref will block, 
unless the variant of deref with timeout is used. 
A future can be cancelled `(future-cancel f)` as long its computation has not yet 
finished. A future can be checked if it has been cancelled `(future-cancelled? f)` or
if its computation has finished `(realized? f)`.

```clojure
(do
   (defn task [] (sleep 1000) 200)
   (deref (future task)))
```

A promise is a thread-safe object that encapsulates an immutable value. This value 
might not be available yet and can be delivered exactly once, from any thread, 
later. If another thread tries to dereference a promise before it's delivered, 
it will block the calling thread. If the promise is already resolved (delivered), 
no blocking occurs at all. Promise can only be delivered once and can never change 
its value once set.

```clojure
(do
   (def p (promise))
   (defn task [] (sleep 500) (deliver p 123))

   ; deliver a value to the promise
   (future task)
   
   ; deref the promise
   (deref p))
```


## Delays

The delay function takes a body of expressions and yields a Delay object that will
invoke the body only the first time it is forced (with `force` or `deref`/`@`), and
will cache the result and return it on all subsequent `force` calls.

```clojure
(do  
   (def x (delay (println "realizing...") 100))
   (sleep 1000)
   (deref x))
```


## Agents

Agents provide uncoordinated, asynchronous access to a single identity. Actions are 
functions that are asynchronously applied to an Agent's state and whose 
return value becomes the Agent's new state.

while agents accept functions to process the agent's state...

```clojure
(do
   (def x (agent 100))
   (send x + 5)
   (await-for 100 x)
   @x)
```

actors accept data to be processed by the actor's function

```clojure
;; simple actors implemented on top of agents
(do
   (def actors (atom {}))

   (defn wait [timeout] (apply await-for timeout (vals @actors)))

   (defn make! [name state handler]
         (let [actor (agent {:state state :handler handler})]
            (swap! actors assoc name actor)))

   (defn invoke-handler [context msg]
         (let [{:keys [state handler]} context
               new-state (handler state msg)]
            (assoc context :state new-state)))

   (defn send! [name & args]
         (let [actor (get @actors name)]
            (send actor invoke-handler args)
            nil))

   (make! :printer nil (fn [_ msg] (apply println msg)))
       
   (send! :printer "hello world")
 
   (wait 200)
   
   nil)
```


## Worker Threads

`futures` are the means of choice when worker threads are required. `futures` 
are basically threads served from a _Java_ _ThreadPoolExecutor_.

_Venice_ provides the functions `futures-fork` and `futures-wait` to rig the
workers and wait for its termination.

```clojure
(do
  ;; define a factory function that creates the workers
  (defn worker-factory [n]
    (fn [] (sleep 3 :seconds)))

  ;; create 5 worker threads and wait for its termination
  (let [threads (futures-fork 5 #(worker-factory %))]
     (apply futures-wait threads)))
```
 

## Locking

The `locking` special form executes expressions in an implicit do, while 
holding a monitor allowing only one thread to execute at any time. 
The monitor can be any _Venice_ value and will be released in all 
circumstances.

Locking operates like the _synchronized_ keyword in _Java_.


Example: coordinating multiple threads printing to stdout

```clojure
(do
  (def monitor 0)

  (defn log [& xs]
    (locking monitor (println (apply str xs))))
  
  (defn worker [n end]
    (fn []
      (log "Worker " n " started")
      (while (< (current-time-millis) end)
        (log "Worker " n " message")
        (sleep (rand-long 3000) :milliseconds))
      (log "Worker " n " stopped")))
 
   ;; launch 5 worker threads, run 20s
   (println "Starting")
   (let [end (+ (current-time-millis) 20000)]
      (apply futures-wait (futures-fork 5 #(worker % end))))
)
```



## Schedulers

### one-shot

Executes a one-shot action that becomes enabled after the specified 
time. 3 seconds in the example:
						
```clojure
(schedule-delay #(println 100) 3 :seconds)
```

`schedule-delay` returns a future that can be dereferenced to get the 
scheduled function’s value or to cancel the waiting execution.
`(deref s)` blocks the current thread until the result gets available. 
						
```clojure
(let [s (schedule-delay (fn [] 100) 3 :seconds)]
  (println "result: " (deref s)))
```


### periodic

Executes a periodic action that becomes enabled first after the initial delay and
then subsequently with the given period:

```clojure
(schedule-at-fixed-rate #(println "test") 1 3 :seconds)
```

`schedule-at-fixed-rate` returns a future that can be used
to cancel the scheduled task. 

Execute a periodic task with a 1s initial delay, a period 
of 3s, and cancel it after 16s:

```clojure
(let [s (schedule-at-fixed-rate (fn [] (println "test")) 1 3 :seconds)]
  (sleep 16000)
  (future-cancel s)
  (println "done."))
```



## Thread local vars

Dynamic variables start off as a global variable and can be bound with 'binding' 
to a new value on the local thread. 

```clojure
(do
  (def-dynamic x 100)
  (println x)         ; x level 1 => 100
  
  (binding [x 200]      
      (println x))    ; x level 2 => 200
      
  (println x))        ; x level 1 => 100
```

Thread local var bindings can be nested

```clojure
(do
  (binding [y 100]
     (println y)       ; x level 1 => 100
     (binding [y 200]
        (println y))   ; x level 2 => 200
     (println y)))     ; x level 1 => 100
```

Thread local vars get inherited by child threads

```clojure
(do
  ;; parent thread locals
  (binding [a 10 b 20]
      ;; future with child thread locals
      (let [f (future (fn [] (binding [b 90] {:a a :b b})))]
         {:child @f :parent {:a a :b b}})))
         
;; => {:parent {:a 10 :b 20} :child {:a 10 :b 90}}
```


## Example: Dining Philosophers

```clojure
(do
  (import :java.util.concurrent.Semaphore)

  (def n-philosophers 5)
  (def max-eating-time 5000)
  (def max-thinking-time 3000)
  (def retry-time 5)
  (def forks (repeatedly n-philosophers #(. :Semaphore :new 1)))
  (def log-mutex 0)

  (defn log [& xs]
    (locking log-mutex (println (apply str xs))))

  (defn left-fork [n]
    (nth forks (mod (dec n) n-philosophers)))

  (defn right-fork [n]
    (nth forks n))

  (defn aquire-fork [fork]
    (. fork :tryAcquire))

  (defn release-fork [fork]
    (. fork :release))

  (defn take-forks [n]
    (if (aquire-fork (left-fork n))
      (if (aquire-fork (right-fork n))
        true
        (do (release-fork (left-fork n))
            false))
      false))

  (defn put-down-forks [n]
    (release-fork (left-fork n))
    (release-fork (right-fork n)))

  (defn eat [n]
    (log "Philosopher " n " is dining")
    (sleep (rand-long max-eating-time))
    (put-down-forks n)
    (log "Philosopher " n " put down forks"))

  (defn think [n]
    (log "Philosopher " n " is thinking")
    (sleep (rand-long max-thinking-time)))

  (defn philosopher [n]
    (fn []
      (try
        (log "Philosopher " n " just sat down")
        (while true
          (if (take-forks n)
            (do (log "Philosopher " n " picked up forks")
                (eat n)
                (think n))
            (sleep retry-time)))
      (catch :RuntimeException ex
        (log "Philosopher " n " died! " (:message ex))))))

   ;; launch
   (println "Starting (stop with <ctrl-c>)")
   (apply futures-wait (futures-fork n-philosophers #(philosopher %)))
)
```

