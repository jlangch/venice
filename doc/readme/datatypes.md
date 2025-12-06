# Datatypes

Venice has a rich set of data types (boolean, string, char, int, long, 
double, decimal, list, vector, set, and map). All data types share 
common features:

- they are immutable
- they support equals semantics
- they provide a hash value
- they are comparable (lower, equal, greater)
- collections support persistent manipulation
- they support meta data

Types supported in Venice:

* [Nil](#nil)
* [Boolean](#boolean)
* [String](#string)
* [Character](#character)
* [Number Types](#number-types)
* [Keyword](#keyword)
* [Symbol](#symbol)
* [Collections](#collections)
* [Stack, Queue, Deque, Delay Queue, and Circular Buffer (all mutable)](#queues-and-deques)

 
 

## nil

In Venice _nil_ is a value and has a meaning of void. _nil_ can be used for any 
data type. _nil_ has the same value as _null_ in Java but compared to 
Java _nil_ is a first class value and has a type (:core/nil).

```clojure
(def x nil)
(nil? x)
(type nil)
```


## Boolean

Booleans are defined by the constants _true_ and _false_.

```clojure
(and true (== 1 1))
(and false (== 1 1))
```


## String

```clojure
(println "abcd")
(println "ab\"cd")
(println "PI: \u03C0")  ;; string with unicode escaped characters
(println """{ "age": 42 }""")

(str "ab" "c" "d")  ;; => "abcd"
(str/format "value: %.4f" 1.45)  ;; "value: 1.4500"
```


## Character

```clojure
;; char literals
#\A          ; => "A"
#\π          ; => "π"
#\u03C0      ; => "π"

(char "A")   ; => "A"
(char 65)    ; => "A"

;; UTF-8 code for "A"
(long #\A)   ; => 65

;; Unicode PI
(println #\u03C0)
```


## Number Types

### long

Based on the Java type _Long_. long is Venice's standard integer type.

```clojure
(+ 1 2)
```

### int

Based on the Java type _Integer_.

```clojure
(+ 2I 3I)
```

### double

Based on the Java type _Double_.

```clojure
(+ 1.0 2.0)
```

### decimal

Based on the Java type _BigDecimal_.

```clojure
(+ 1.0M 2.0M)
```

### big integer

Based on the Java type _BigInteger_.

```clojure
(+ 1N 2N)
```

### type coercion

Number types are implicitly coerced to the most complex type in an expression

```clojure
(+ 1 2.0 1.0M) ;; => 4.0M

(+ 1.0M 2.0 1I) ;; => 4.0M
```


## Keyword

Keywords (e.g. `:a`) are symbolic identifiers.

```clojure
{:a 100, :b 200}

[:a :b]
```

## Symbol

Symbols are identifiers that are normally used to refer to function parameters, 
let bindings, and global vars.

```clojure
(defn sum [x y] (+ x y))

(def x 100)

(let [a 100]
  (println a))
```


## Collections

### list

Immutable persistent list.

```clojure
'(1 2 3)
(list 1 2 (+ 1 2))

(cons 1 '(2 3 4))  ;; => (1 2 3 4)
(conj '(1 2 3) 4)  ;; => (1 2 3 4)
(first '(2 3 4))   ;; => 2
(rest '(2 3 4))    ;; => (3 4)
```

### vector

Immutable persistent vector.

```clojure
[1 2 3]
(vector 1 2 (+ 1 2))

(cons 1 [2 3 4])  ;; => [1 2 3 4]
(conj [1 2 3] 4)  ;; => [1 2 3 4]
(first [2 3 4])   ;; => 2
(rest [2 3 4])    ;; => [3 4]
```

### hash-set

Immutable persistent hash set.

```clojure
#{1 2 3}
(set 1 2 3)

(cons 3 #{1 2})          ;; => #{1 2 3}
(contains? #{:a :b} :a)  ;; => true
```

### sorted-set

Immutable persistent sorted set.

```clojure
(sorted-set 2 3 1)

(cons 3 (sorted-set 2 1))          ;; => #{1 2 3}
(contains? (sorted-set :a :b) :a)  ;; => true
```

### hash-map

Immutable persistent hash map.

```clojure
{:a 100 :b 200}
(hash-map :a 100 :b 200)

(cons {:c 3} {:a 1 :b 2})        ;; => {:a 1 :b 2 :c 3}
(get {:a 1 :b 2} :b)             ;; => 2
(:b {:a 1 :b 2})                 ;; => 2
(assoc {:a 1} :b 2 :c 3)         ;; => {:a 1 :b 2 :c 3}
(dissoc {:a 1 :b 2 :c 3} :c :b)  ;;=> {:a 1}
```

### ordered-map

Immutable persistent ordered map.

```clojure
(ordered-map :a 100 :b 200)

(cons {:c 3} (ordered-map :a 1 :b 2))  ;; => {:a 1 :b 2 :c 3}
(get (ordered-map :a 1 :b 2) :b)       ;; => 2
(:b (ordered-map :a 1 :b 2))           ;; => 2
```

### sorted-map

Immutable persistent sorted map.

```clojure
(sorted-map :a 100 :b 200)

(cons {:c 3} (sorted-map :b 2 :a 1))  ;; => {:a 1 :b 2 :c 3}
(get (sorted-map :a 1 :b 2) :b)       ;; => 2
(:b (sorted-map :a 1 :b 2))           ;; => 2
```

## Queues and Deques

### stack

Threadsafe mutable stack based on the Java type _ConcurrentLinkedDeque_.


```
     head                          tail
       +---------+---------+---------+
       |    1    |    2    |    3    |
       +---------+---------+---------+
       ^
       \-- push,pop elements 
           to/from the head 
```

```clojure
(stack )

(let [s (stack)]
  (push! s 4)
  (push! s 3)
  (pop! s)
  (peek s))   ;; => 4
```


### queue

Threadsafe mutable queue based on the Java type _LinkedBlockingQueue_.

```
     head                          tail
       +---------+---------+---------+
       |    1    |    2    |    3    |
       +---------+---------+---------+
       ^                             ^
       \-- poll,take                 \--  offer,put new 
           elements from                  elements to the
           the head                       tail
```

```clojure
(queue) ;; unbounded queue

(queue 100) ;; bounded queue

;; asynchronous access
;;   offer!  returns immediately with false if the queue is full otherwise
;;           adds the value to the tail of the queue and returns true
;;   poll!   returns immediately with nil if the queue is empty 
;;           otherwise returns the head value 
(let [q (queue 10)]
  (offer! q 1)
  (offer! q 2)
  (offer! q 3)
  (peek q)      ;; => 1
  (poll! q))    ;; => 1

;; asynchronous access with timeouts
;;   offer!  returns false the value cannot be added to the tail of the
;;           queue within the given timeout time, otherwise adds the 
;;           value to the tail of the queue and returns true
;;   poll!   returns the head value of the queue if one is available
;;           within the given timeout time, otherwise returns nil
(let [q (queue 10)]
  (offer! q 500 1)
  (offer! q 500 2)
  (offer! q 500 3)
  (peek q)          ;; => 1
  (poll! q 500))    ;; => 1
  
;; synchronous access
;;   put!    adds the value to the tail of the queue, waiting if  
;;           necessary for space to become available
;;   take!   returns the head value, waiting if necessary for a head
;;           value to become available
(let [q (queue 10)]
  (put! q 1)
  (put! q 2)
  (put! q 3)
  (peek q)      ;; => 1
  (take! q))    ;; => 1
```


### deque

Threadsafe mutable deque based on the Java type _LinkedBlockingDeque_.

While queues add elements at the tail of the queue and remove elements from
the head of the queue, deques are double-ended-queues that allow to add and remove
elements from both sides of the queue.

```
     head                          tail
       +---------+---------+---------+
       |    1    |    2    |    3    |
       +---------+---------+---------+
       ^                             ^
       \-- poll,take                 \--  offer,put new 
           elements from                  elements to
           the head                       the tail

       \-- offer,put new             \--  poll,take
           elements to                    elements from 
           the head                       the tail
```

```clojure
(deque) ;; unbounded deque

(deque 100) ;; bounded deque

;; asynchronous access
;;   offer!  returns immediately with false if the deque is full otherwise
;;           adds the value to the tail of the deque and returns true
;;   poll!   returns immediately with nil if the deque is empty 
;;           otherwise returns the head value 
(let [q (deque 10)]
  (offer! q 1)
  (offer! q 2)
  (offer! q 3)
  (peek q)      ;; => 1
  (poll! q))    ;; => 1

;; asynchronous access with timeouts
;;   offer!  returns false the value cannot be added to the tail of the
;;           deque within the given timeout time, otherwise adds the 
;;           value to the tail of the deque and returns true
;;   poll!   returns the head value of the queue if one is available
;;           within the given timeout time, otherwise returns nil
(let [q (deque 10)]
  (offer! q 500 1)
  (offer! q 500 2)
  (offer! q 500 3)
  (peek q)          ;; => 1
  (poll! q 500))    ;; => 1

;; asynchronous access at both ends of the queue
(let [q (deque 10)]
  (offer! q 500 1)
  (offer! q 500 2)
  (offer! q 500 3)
  (peek q)               ;; => 1
  (offer-head! q 500 0)
  (poll! q 500)          ;; => 0
  (poll-tail! q 500))    ;; => 3


;; synchronous access
;;   put!    adds the value to the tail of the deque, waiting if  
;;           necessary for space to become available
;;   take!   returns the head value, waiting if necessary for a head
;;           value to become available
(let [q (deque 10)]
  (put! q 1)
  (put! q 2)
  (put! q 3)
  (take! q))    ;; => 1

;; synchronous access at both ends of the queue
(let [q (deque 10)]
  (put! q 1)
  (put! q 2)
  (put! q 3)
  (peek q)           ;; => 1
  (put-head! q 0)
  (take! q)          ;; => 0
  (take-tail! q))    ;; => 3
```


### delay queue

A delay-queue is an unbounded blocking queue of delayed elements,
in which an element can only be taken when its delay has expired.
The head of the queue is that delayed element whose delay expired
furthest in the past. If no delay has expired there is no head and
`poll!` will return nil. Unexpired elements cannot be removed using
`take!` or `poll!`, they are otherwise treated as normal elements.
For example, the `count` method returns the count of both expired and
unexpired elements.

A delay queue does not permit `nil` elements.

```clojure
(let [q (delay-queue)]
  (put! q 1 100)   ;; delay 100ms
  (put! q 1 200)   ;; delay 200ms
  (take! q))
```

**Implement a rate limiter on top of a delay queue:**

```clojure
(do
  (defprotocol RateLimiter (init [x]) (aquire [x]))

  (deftype :rate-limiter [queue                :core/delay-queue
                          limit-for-period     :long
                          limit-refresh-period :long]
           RateLimiter
             (init [this]   (let [q (:queue this)
                                  n (:limit-for-period this)]
                              (empty q)
                              (repeatedly n #(put! q :token 0))
                              this))
             (aquire [this] (let [q (:queue this)
                                  p (:limit-refresh-period this)]
                              (take! q)
                              (put! q :token p))))
 
  ;; create a limiter with a limit of 5 actions within a 2s period
  (def limiter (init (rate-limiter. (delay-queue) 5 2000)))

  ;; test the limiter
  (doseq [x (range 1 26)]
    (aquire limiter)
    (sleep (rand-long 100)) ;; func simulation
    (printf "%s: run %2d%n" (time/local-date-time) x)))
```


### circular buffer

A circular buffer stores the N most recently inserted elements.
If the circular buffer is already full, the oldest element (the head)
will be evicted, and then the new element added at the tail.

The circular buffer does not permit `nil` elements."

```clojure
(let [q (circular-buffer 3)]
  (put! q 1)
  (put! q 2)
  (put! q 3)
  (put! q 4)
  (println q)
  (println (take! q)))

```

