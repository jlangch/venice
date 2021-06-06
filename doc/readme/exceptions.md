# Exception Handling


## Exceptions

A few exception types are imported implicitly to simplify its usage:

  * `:java.lang.Exception`
  * `:java.lang.RuntimeException`
  * `:com.github.jlangch.venice.VncException`
  * `:com.github.jlangch.venice.ValueException`




**try - catch - finally**

```clojure
(do
   (import :java.io.IOException)
  
   (try
      (throw (ex :RuntimeException "a message"))
      (catch :IOException e "IOException, msg: ~(:message e)")
      (catch :RuntimeException e "RuntimeException, msg: ~(:message e)")
      (finally (println "... finally."))))
      
;; output:
;;
;; ... finally.
;; => "RuntimeException msg: a message"
```

*Note:*
The finally block is just for side effects, like closing resources. It never returns a value!

Throw, catch, and finally blocks may contain multiple expressions:

```clojure
(do
   (import :java.io.IOException)
  
   (try
      (println "try...")
      (throw (ex :RuntimeException "a message"))
      (catch :IOException e 
         (println "caught IOException")
         "IOException, msg: ~(:message e)")
      (catch :RuntimeException e
         (println "caught RuntimeException")
         "RuntimeException, msg: ~(:message e)")
      (finally 
         (println "... finally."))))
      
;; output:
;;
;; try...
;; caught RuntimeException
;; ... finally.
;; => "RuntimeException msg: a message"
```

Any Venice data can be thrown resulting in a `:ValueException`:

```clojure
(do
   (try
      (throw [1 2 3])  ; ValueException
      (catch :ValueException e (:value e))))
      
;; output:
;;
;; => [1 2 3]
```


**try-with resources**

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


