# Concurrency

* [Atoms](#atoms)
* [Futures & Promises](#futures-and-promises)
* [Delays](#delays)
* [Agents](#agents)
* [Worker Threads](#worker-threads)
* [Locking](#locking)
* [Schedulers](#schedulers)
* [Bare Threads](#bare-threads)
* [Thread local vars](#thread-local-vars)
* [Examples](#examples)


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

`@foo` is the dereference short form for `(deref foo)`

```clojure
(do
   (def counter (atom 2))
   (swap! counter + 2)
   @counter)
```



## Futures and Promises

### Futures

A future takes a function and yields a future object that will invoke the function 
in another thread, and will cache the result and return it on all subsequent calls 
to deref. If the computation has not yet finished, calls to deref will block, 
unless the variant of deref with timeout is used. 
A future can be cancelled `(cancel f)` as long its computation has not yet 
finished. A future can be checked if it has been cancelled `(cancelled? f)` or
if its computation has finished `(realized? f)`.

```clojure
(do
   (defn task [] (sleep 1000) 200)
   (deref (future task)))
```

Dereference with a timeout, and a default value. The default value that is returned 
if the computation is not finished within the timeout time.

```clojure
(do
   (defn task [] (sleep 1000) 200)
   (deref (future task) 100 :timeout))
```


### Promises

A promise is a thread-safe object that encapsulates an immutable value. This value 
might not be available yet and can be delivered exactly once, from any thread, 
later. If another thread tries to dereference a promise before it's delivered, 
it will block the calling thread. If the promise is already resolved (delivered), 
no blocking occurs at all. A promise can only be delivered once and can never change 
its value once set. 

Promises are futures too. They come with additional features for chaining and 
combining asynchronous tasks.

```clojure
(do
   (def p (promise))
   (defn task [] (sleep 500) (deliver p 123))

   ; deliver a value to the promise
   (future task)
   
   ; deref the promise
   (deref p))  ; => 123
```

Using a promise like a future:

```clojure
(do
   (defn task [] (sleep 500) 123)
   (def p (promise task))
   
   ; deref the promise
   (deref p))  ; => 123
```

Chaining asynchronous tasks:

```clojure
(-> (promise (fn [] (sleep 50) 5))
    (then-apply (fn [x] (sleep 30) (+ x 2)))
    (then-apply (fn [x] (sleep 20) (* x 3)))
    (deref))  ; => 21
```

Combining the result of two asynchronous tasks:

```clojure
(-> (promise #(do (sleep 50) 1000))
    (then-apply #(do (sleep 20) (+ %1 50)))
    (then-combine (-> (promise #(do (sleep 30) "eur"))
                      (then-apply str/upper-case))
                  #(str %1 " " %2))
    (deref))  ; => "1050 EUR"
```

Composing the result of two asynchronous tasks:

```clojure
(-> (promise #(do (sleep 20) 1000))
    (then-apply #(do (sleep 20) (+ %1 50)))
    (then-compose (fn [o] (-> (promise #(do (sleep 20) "eur"))
                              (then-apply str/upper-case)
                              (then-apply #(str o " " %1)))))
    (deref)) ; => "1050 EUR"
```

Suppose we want to make coffee. This involves 4 steps:

* 1a grind coffee beans
* 1b heat water
* 2  mix the hot water with the ground beans
* 3  filter the coffee

All these steps take varying time, so they run asynchronously and have to be 
orchestrated. 

```clojure
(do
  (def trace (let [mutex 0] 
               (fn [& xs] (locking mutex (println (apply str xs))))))

  ;; the domain data
  (deftype :coffee-beans [])
  (deftype :ground-coffee [])
  (deftype :cold-water [])
  (deftype :warm-water [])
  (deftype :unfiltered-coffee [])
  (deftype :filtered-coffee [])

  ;; the domain functions
  (defn grind-beans [coffee-beans] 
    (trace "1a) grinding beans...") 
    (ground-coffee.))
    
  (defn heat-water [cold-water] 
    (trace "1b) heating water...") 
    (warm-water.))
    
  (defn mix [warm-water ground-coffee] 
    (trace "2)  mixing water and coffee...") 
    (unfiltered-coffee.))
    
  (defn filter-coffee [unfiltered-coffee] 
    (trace "3)  filtering coffee...") 
    (filtered-coffee.))

  (defn make-coffee []
	;; the processing, wiring the steps
	(-> (promise #(grind-beans (coffee-beans.)))             ;; 1a
	    (then-combine (promise #(heat-water (cold-water.)))  ;; 1b
	                  #(mix %1 %2))                          ;; 2
	    (then-apply #(filter-coffee %1))                     ;; 3
	    (deref)))

  (defn make-coffee-2 []
    (let [step_1a (promise)
          step_1b (promise)
          step_2  (promise)
          step_3  (promise)]
      (future #(deliver step_1a (grind-beans (coffee-beans.))))  ;; 1a
      (future #(deliver step_1b (heat-water (cold-water.))))     ;; 1b
      (future #(deliver step_2 (mix @step_1a @step_1b)))         ;; 2
      (future #(deliver step_3 (filter-coffee @step_2)))         ;; 3
      @step_3))

  (make-coffee))
```


Timeouts:

```clojure
(-> (promise (fn [] (sleep 100) "The quick brown fox..."))
    (or-timeout 300 :milliseconds)  ; throws a TimeoutException
    (then-apply str/upper-case)
    (deref))
```

```clojure
(-> (promise (fn [] (sleep 500) "The quick brown fox..."))
    (complete-on-timeout "The fox did not jump" 300 :milliseconds)
    (deref))
```



## Delays

The delay function takes a body of expressions and yields a Delay object that will
invoke the body only the first time it is forced (with `force` or `deref`/`@`), and
will cache the result and return it on all subsequent `force` calls.

```clojure
(do  
   (def x (delay (println "realizing...") 100))
   (sleep 1 :seconds)
   (deref x))
```

```clojure
(do  
   (def x (delay (println "realizing...") 100))
   (sleep 1 :seconds)
   (force x))
```


## Agents

Agents provide uncoordinated, asynchronous access to a single identity. Actions are 
functions that are asynchronously applied to an Agent's state and whose 
return value becomes the Agent's new state.

```clojure
(do
   (def x (agent 100))
   (send x + 5)
   (await-for 100 x)
   @x)
```



The following example is an implementation of a message relay. A chain of n 
agents is created, then a sequence of m actions are dispatched to the head 
of the chain and relayed through it:

*Note: The example has been taken from the Clojure Agent demo and uses a synchronous queue*

```clojure
;; Agent chain
;;
;; +-------+    +-------+    ........    +-------+    +--------+    +-------+
;; | agent |    | agent |    .      .    | agent |    | agent  |    | queue |
;; |  998  |    |  997  |    .      .    |   0   |    |   *    |    |  ---  |
;; |       |    |       |    .      .    |       |    |        |    |  ---  |
;; | :next o--->| :next o--->.      .--->| :next o--->| :queue o--->|  ---  |
;; +-------+    +-------+    ........    +-------+    +--------+    +-------+

(do
  (defn relay [x i]
    (when (:next x)
      (send (:next x) relay i))
    (when (and (zero? i) (:queue x))
      (offer! (:queue x) :indefinite i))
    x)
    
  (defn chain-agents [m q]
    (reduce (fn [next _] (agent {:next next}))
                         (agent {:queue q}) 
                         (range (dec m))))

  (defn run [m n]
    (let [q  (queue)
          hd (chain-agents m q)]
      (doseq [i (reverse (range n))]
        (send hd relay i))
      (poll! q :indefinite)))

  ;; 1 million message sends:
  (time (run 1000 1000))) ; Elapsed time: 2.94s
```

### Actors

While Agents accept functions to process the Agent's state, Actors accept 
data to be processed by the Actor's function

A simple Actors model can be implemented on top of Agents:

```clojure
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
  (cancel s)
  (println "done."))
```


## Bare Threads

The `thread` function executes a function in another thread, returning immediately to the 
calling thread. It returns a _promise_ which will receive the result of the calling function 
when completed.

**Note:** Each call to `thread` creates a new expensive system thread. Consider to use 
futures or promises that use an *ExecutorService* to deal efficiently with threads. 

Simple Thread:

```clojure
@(thread #(do (sleep 1000) 1))
```

Producer/Consumer:

```clojure
(do
  (defn produce [q n]
    (doseq [x (range n)] (sleep 1000) (put! q x))
    (put! q nil))
    
  (defn consume [q]
    (docoll println q))
    
  (let [q (queue 10)] 
    (thread #(produce q 4))
    @(thread #(consume q))))
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


## Examples


### Estimating PI using the Monte Carlo method

See [PI Monte Carlo](https://www.geeksforgeeks.org/estimating-value-pi-using-monte-carlo/)


#### Estimating PI single-threaded

```clojure
(do  
  (defn circle? [x y]
    (<= (+ (* x x) (* y y)) 1.0))

  (defn sample []
    (circle? (rand-double) (rand-double)))

  (defn pi [iterations]
    (let [measurements (repeatedly iterations sample)
          inside       (filter true? measurements)]
       (* 4.0 (/ (double (count inside)) iterations)))))
       
  ;; (time (pi 10_000_000))
  ;; Elapsed time: 16.34s
  ;; => 3.1418628  
```


#### Estimating PI multi-threaded

```clojure
(do  
  (defn circle? [x y]
    (<= (+ (* x x) (* y y)) 1.0))

  (defn sample []
    (circle? (rand-double) (rand-double)))

  (defn worker [iterations]
    (->> (repeatedly iterations sample)
         (filter true?)
         (count)))

  (defn pi [iterations workers]
    (let [iter_worker (/ iterations workers)
          inside (->> (repeatedly workers (fn [] (future #(worker iter_worker))))
                      (map deref)
                      (apply +))]
      (* 4.0 (/ (double inside) iter_worker workers)))))
       
  ;; (time (pi 10_000_000 n))
  ;; n       1       2       3       4       5       6
  ;; time    16.68s  12.32s  11.28s  12.47s  14.83s  16.02s
```


### Dining Philosophers

#### Dining Philosophers with Semaphores

```clojure
(do
  (import :java.util.concurrent.Semaphore)
  
  ;;              [P0]
  ;;         F0          F1
  ;;
  ;;    [P4]                 [P1]
  ;;
  ;;     F4                   F2
  ;;
  ;;        [P3]        [P2]            Px: philosopher
  ;;               F3                   Fx: fork 
  
  (def n-philosophers 5)
  (def max-eating-time 5000)
  (def max-thinking-time 3000)
  (def retry-time 5)
  (def forks (repeatedly n-philosophers #(. :Semaphore :new 1)))
  (def log-mutex 0)

  (defn log [& xs]
    (locking log-mutex (println (apply str xs))))

  (defn left-fork [n]
    (nth forks (mod (inc n) n-philosophers)))

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


### Dining Philosophers with Atoms

```clojure
(do
  ;;              [P0]
  ;;         F0          F1
  ;;
  ;;    [P4]                 [P1]
  ;;
  ;;     F4                   F2
  ;;
  ;;        [P3]        [P2]            Px: philosopher
  ;;               F3                   Fx: fork

  (def n-philosophers 5)
  (def max-eating-time 5000)
  (def max-thinking-time 3000)
  (def retry-time 5)
  ;; fork values: nil if the fork is not locked else the philosophers index that 
  ;; locked the fork
  (def forks (atom (into [] (repeat n-philosophers nil))))
  (def log-mutex 0)

  (defn log [& xs]
    (locking log-mutex (println (apply str xs))))

  (defn left-fork [n]
    (mod (inc n) n-philosophers))

  (defn right-fork [n]
    n)

  (defn forks-acquired? [fs n]
    (and (some? (fs (left-fork n))) (some? (fs (right-fork n)))))

  (defn forks-acquired-by? [fs n x]
    (and (= (fs (left-fork n)) x) (= (fs (right-fork n)) x)))

  (defn forks-free? [fs n]
    (and (nil? (fs (left-fork n))) (nil? (fs (right-fork n)))))

  (defn forks-set [fs n val]
    (-> fs
        (assoc (left-fork n) val)
        (assoc (right-fork n) val)))

  (defn debug [fs n]
    (log "Philosopher " n " forks: " fs))

  (defn take-forks [n]
    (let [upd (swap! forks
                     (fn [fs]
                       (if (forks-free? fs n)
                         (forks-set fs n n)
                         fs)))
          acquired (forks-acquired-by? upd n n)]
       (when acquired (debug upd n))
       acquired))

  (defn put-down-forks [n]
    (let [upd (swap! forks (fn [fs] (forks-set fs n nil)))]
      (debug upd n)))

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
