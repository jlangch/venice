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


## nil

In Venice _nil_ is a value and has a meaning of void. _nil_ can be used for any 
data type. _nil_ has the same value as _null_ in Java but compared to 
Java _nil_ is a first class value and has a type (:core/nil).

```clojure
(def x nil)
(nil? x)
(type nil)
```


## Booleans

Booleans are defined by constants _true_ and _false_.

```clojure
(and true (== 1 1))
(and false (== 1 1))
```


## Strings

```clojure
(println "abcd")
(println "ab\"cd")
(println "PI: \u03C0")
(println """{ "age": 42 }""")

(str "ab" "c" "d")  ;; => "abcd"
(str/format "value: %.4f" 1.45)  ;; "value: 1.4500"
```


## Characters

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


## Numbers

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


## Keywords

Keywords (e.g. `:a`) are symbolic identifiers.

```clojure
{:a 100, :b 200}

[:a :b]
```

## Symbols

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

### stack

Threadsafe mutable stack based on the Java type _ConcurrentLinkedDeque_.

```clojure
(stack )

(let [s (stack)]
  (push! s 4)
  (push! s 3)
  (pop! s)
  (peek s))   ;; => 4
```


### queue

Threadsafe mutable queue based on the Java type _LinkedBlockingDeque_.

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
  (take! q))    ;; => 1
```

Inserts the specified element into this queue, waiting if necessary for 
space to become available.
