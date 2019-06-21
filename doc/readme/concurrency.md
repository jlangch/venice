# Concurrency


## Atoms

Atoms provide uncoordinated, synchronous access to a single identity. 
The primary use of an atom is to hold Venice’s immutable data structures. 
The value held by an atom is changed with the `swap!` method.

```clojure
(do
   (def counter (atom 2))
   (swap! counter + 2))
   (deref counter))
```

## Futures & Promises

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

A promise is a thread-safe object that encapsulates immutable value. This value 
might not be available yet and can be delivered exactly once, from any thread, 
later. If another thread tries to dereference a promise before it's delivered, 
it'll block calling thread. If promise is already resolved (delivered), no 
blocking occurs at all. Promise can only be delivered once and can never change 
its value once set

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



## Scheduler

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
  (binding [x 100]
     (println x)       ; x level 1 => 100
     (binding [x 200]
        (println x))   ; x level 2 => 200
     (println x)))     ; x level 1 => 100
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
