# Exception Handling


## Exceptions

try - catch - finally

```clojure
(do
   (import :java.lang.RuntimeException)
   (import :java.io.IOException)
  
   (try
      (throw (. :RuntimeException :new "a message"))
      (catch :IOException ex (:message ex))
      (catch :RuntimeException ex (:message ex))
      (finally (println "... finally."))))
```

Throw, catch, and finally blocks may contain multiple
expressions:

```clojure
(do
   (import :java.lang.RuntimeException)
   (import :java.io.IOException)
  
   (try
      (println "try...")
      (throw (. :RuntimeException :new "a message"))
      (catch :IOException ex 
         (println "caught IOException")
         (:message ex))
      (catch :RuntimeException ex 
         (println "caught RuntimeException")
         (:message ex))
      (finally 
         (println "... finally."))))
```

Any Venice data can be thrown resulting in a ValueException:

```clojure
(do
   (try
      (throw [1 2 3])  ; ValueException
      (catch :ValueException ex (pr-str (:value ex)))))
```


try-with resources

```clojure
(do
   (import :java.io.FileInputStream)
   
   (let [file (io/temp-file "test-", ".txt")]
      (io/spit file "123456789" :append true)
      (try-with [is (. :FileInputStream :new file)]
        (io/slurp-stream is :binary false))))
```


## Stack traces

Venice generates user friendly stack traces

```clojure
(do
   (defn fn1 [x] (fn2 x))
   (defn fn2 [x] (fn3 x))
   (defn fn3 [x] (/ 1 x))
   (fn1 0))
   
=>
Exception in thread "main" VncException: / by zero

[Callstack]
    at: / (user: line 4, col 19)
    at: user/fn3 (user: line 3, col 19)
    at: user/fn2 (user: line 2, col 19)
    at: user/fn1 (user: line 5, col 5)
```


