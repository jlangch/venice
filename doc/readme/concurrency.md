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
   (deref (future task))        
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


## Thread local vars

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
