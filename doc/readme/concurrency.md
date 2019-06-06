# Concurrency


## Atoms

```clojure
(do
   (def counter (atom 2))
   (swap! counter + 2))
   (deref counter))
```

## Futures & Promises

```clojure
(do
   (defn task [] (sleep 1000) 200)
   (deref (future task)))
```

```clojure
(do
   (def p (promise))
   (defn task [] (sleep 500) (deliver p 123))

   (future task)
   (deref p))
```


## Delays

```clojure
(do  
   (def x (delay (println "realizing...") 100))
   (sleep 1000)
   (deref x))
```


## Agents

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


Executes a one-shot action that becomes enabled after 3 seconds.
						
```clojure
(schedule-delay #(println 100) 3 :seconds)
```

`(deref s)` blocks the current thread until the result gets available. 
						
```clojure
(let [s (schedule-delay (fn [] 100) 3 :seconds)]
  (println "result: " (deref s)))
```

### periodic

Executes a periodic action that becomes enabled first after 1s initial delay and
is subsequently executed with a period of 3s 

```clojure
(schedule-at-fixed-rate #(println "test") 1 3 :seconds)
```

Cancel the periodic task after 16s 

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
