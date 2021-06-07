# Exception Handling


## Exceptions

Exception handling is offered by many programming languages to handle the 
runtime errors so that the normal flow of the application can be maintained. 
Venice's exception handling is built on Java exceptions thus providing 
seamless interoperability with existing Java code.

All exceptions in Venice are *unchecked*. If checked exceptions are caught
by Venice they are immediately converted to a runtime exception.

A few exception types are imported implicitly to simplify usage:

  * `:java.lang.Exception`
  * `:java.lang.RuntimeException`
  * `:com.github.jlangch.venice.VncException`
  * `:com.github.jlangch.venice.ValueException`


Create exceptions using the function `ex` :

```clojure
(do
   (import :java.io.IOException)

   ;; create an unchecked RuntimeException
   (ex :RuntimeException "exception message")
  
   ;; create a checked IOException
   (ex :IOException "exception message")
   
   ;; create a Venice exception
   (ex :VncException "exception message")
   
   ;; create an exception with a cause
   (let [cause (ex :RuntimeException "exception message")]
     (ex :VncException "exception message" cause))
```

*Note:*
Prefer using the `ex` function over Java interop to create exceptions! `ex` 
works even with a full restricted sandbox where as the Java interop variant 
requires a specifically configured sandbox.

Create exceptions with arbitrary arguments using Java interop  if `ex` is not 
suitable:

```clojure
(do
   (import :java.text.ParseException)

   ;; public ParseException(String s, int errorOffset)
   (. :ParseException :new "Expected '['" 1000)
```




## try - catch - finally

`catch` clauses within a `try` can catch any Java exception:

The first catch clause that matches the thrown exception will execute.

```clojure
(do
   (import :java.io.IOException)
  
   (try
      (throw (ex :RuntimeException "a message"))
      (catch :IOException e "IOException, msg: ~(:message e)")
      (catch :RuntimeException e "RuntimeException, msg: ~(:message e)")
      (catch :VncException e "VncException, msg: ~(:message e)")
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

Any Venice data can be thrown resulting in a `:ValueException` with the data 
as the exception's value:

```clojure
(do
   (try
      (throw [1 2 3])  ; ValueException
      (catch :ValueException e (:value e))))
      
;; output:
;;
;; => [1 2 3]
```



## try-with resources

```clojure
(do
   (import :java.io.FileInputStream)
   
   (let [file (io/temp-file "test-", ".txt")]
      (io/spit file "123456789" :append true)
      (try-with [is (. :FileInputStream :new file)]
         (io/slurp-stream is :binary false))))
```



## Selectors

The first catch clause whose selector matches the thrown exception will execute.

A selector can be:

  * a class: (e.g., :RuntimeException, :java.text.ParseException), matches 
    any instance of that class

  * a key-values vector: (e.g., [key val & kvs]), matches any instance of 
    :ValueException where the exception's value meets the expression 
    `(and (= (get ex-value key) val) ...)`

  * a predicate: (a function of one argument like `map?`, `set?`), matches any 
    instance of :ValueException where the predicate applied to the exception's 
    value returns `true`
    

***Selector Examples***

Class selector:

```clojure
(do
   (try
      (throw (ex :RuntimeException "a message"))
      (catch :RuntimeException e
         (println "RuntimeException, msg: ~(:message e)"))))
```


key-value selector:

```clojure
(do
   (try
      (throw {:a 100, :b 200})
      (catch [:a 100] e
         (println "ValueException, value: ~(:value e)"))
      (catch [:a 100, :b 200] e
         (println "ValueException, value: ~(:value e)"))))
```


Predicate selector:

```clojure
(do
   (try
      (throw {:a 100, :b 200})
      (catch long? e
         (println "ValueException, value: ~(:value e)"))
      (catch map? e
         (println "ValueException, value: ~(:value e)"))
      (catch #(= 100 (:a %)) e
         (println "ValueException, value: ~(:value e)"))))
```


## Custom Exceptions

Venice *Custom Types* are a perfect fit for custom exceptions. Throw an instance
of a custom type as a :ValueException and define a `catch` clause with a predicate.


Example:

```clojure
(do
   (deftype :my-exception1 [message :string, position :long])
   
   (deftype :my-exception2 [message :string]) 
   
   (try
      (throw (my-exception1. "error" 100))
      
      (catch my-exception1? e 
         (println (:value e)))
         
      (catch my-exception2? e 
         (println (:value e)))))
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


