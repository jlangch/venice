# Destructuring

Destructuring in Venice is a powerful feature that allows you to easily 
extract values from data structures (like lists, vectors, maps, and sequences) 
and bind them to variables. This makes your code more readable and concise 
by reducing the need for explicit indexing or lookups.


* [Sequential Destructuring](#sequential-destructuring)
* [Associative Destructuring](#associative-destructuring)


## Sequential Destructuring

Sequential destructuring breaks up a sequential data structure as a Venice 
list or vector within a let binding

```clojure
(do
   (let [[x y z] [1 2 3]]
      (println x y z))
      ;=> 1 2 3

   (let [[x y z] '(1 2 3)]
      (println x y z))
      ;=> 1 2 3

   (let [[k v] (map-entry "a" 100)]
      (println k v))
      ;=> "a" 100
      
   ;; for strings, the elements are destructured by character.
   (let [[x y z] "abc"]
     (println x y z))
     ;=> a b c
)
```

or within function parameters

```clojure
(do
   (defn position [[x y]]
      (println "x:" x "y:" y))
      
   (position [1 2]) ;=> x: 1 y: 2
)
```

The destructured collection must not be of same size as the number of binding names

```clojure
(do
   (let [[a b c d e f] '(1 2 3)]
      (println a b c d e f))
      ;=> 1 2 3 nil nil nil
      
   (let [[a b c] '(1 2 3 4 5 6 7 8 9)]
      (println a b c))
      ;=> 1 2 3
)
```

### Working with tail elements `&` and ignoring bindings `_`

```clojure
(do
   (let [[a b c & z] '(1 2 3 4 5 6 7 8 9)]
      (println a b c z))
      ;=> 1 2 3 (4 5 6 7 8 9)

   (let [[a _ b _ c & z] '(1 2 3 4 5 6 7 8 9)]
      (println a b c z))
      ;=> 1 3 5 (6 7 8 9)
)
```

### Binding the entire collection with `:as`

```clojure
(do
   (let [[a b c & z :as all] '(1 2 3 4 5 6 7 8 9)]
      (println a b c z all))
      ;=> 1 2 3 (4 5 6 7 8 9) (1 2 3 4 5 6 7 8 9)
)
```

### Nested bindings

```clojure
(do
   (def line [[5 10] [10 20]])
   (let [[[x1 y1][x2 y2]] line]
      (printf "Line from (%d,%d) to (%d,%d)%n" x1 y1 x2 y2))
      ;=> "Line from (5,10) to (10,20)"
)
```

`:as` or `&` can be used at any level

```clojure
(do
   (def line [[5 10] [10 20]])
   (let [[[a b :as group1] [c d :as group2]] line]
      (println a b group1)
      (println c d group2)))
      ;=> 5 10 [5 10]
      ;=> 10 20 [10 20])
```


### Lazy sequences

```clojure
(do
   (let [[x y & z] (lazy-seq 1 inc)]
      (println x y (doall (take 5 z)))))
      ;=> 1 2 (3 4 5 6 7)
```

```clojure
(do
   (let [[x _ y _ z] (lazy-seq 1 inc)]
      (println x y z)))
      ;=> 1 3 5
```


### Destructuring Function Parameters

Destructuring can be used directly in function parameters, making it easy to pass 
and work with complex data structures.

```clojure
(do
  (defn sum [[x y]]
    (println "Sum: " (+ x y)))

  (sum [1 2]))
```



## Associative Destructuring

Associative destructuring breaks up an associative (key/value) data structure 
as a Venice map within a let binding.

```clojure
(do
   (let [{a :a, b :b, c :c} {:a "A" :b "B" :d "D"}]
      (println a b c))
      ; => A B nil
)
```

```clojure
(do
   (def map_keyword {:a "A" :b "B" :c 3 :d 4})
   (def map_strings {"a" "A" "b" "B" "c" 3 "d" 4})
   
   (let [{:keys [a b c]} map_keyword]
      (println a b c))
      ; => A B 3
      
   (let [{:strs [a b c]} map_strings]
      (println a b c))
      ; => A B 3
)
```

### Binding the entire collection with `:as`

```clojure
(do
   (def map_keyword {:a "A" :b "B" :c 3 :d 4})

   (let [{:keys [a b c] :as all} map_keyword]
      (println a b c all))
      ; => A B 3 {:a A :b B :c 3 :d 4}
)
```

### Binding with defaults `:or`

```clojure
(do
  (defn configure [options]
     (let [{:keys [port debug verbose] :or {port 8000, debug false, verbose false}} options]
     (println "port =" port " debug =" debug " verbose =" verbose)))
     ;=> port 8000, debug false, verbose false

  (configure {:debug true})
)
```

### Nested destructuring

Associative destructuring can be nested and combined with sequential destructuring

```clojure
(do
   (def users
      {:peter {:role "clerk"
               :branch "Zurich"
               :age 40}
               
       :magda {:role "head of HR"
               :branch "Bern"
               :age 45}
               
       :kurt  {:role "assistant"
               :branch "Lucerne"
               :age 32}})

   (let [{{:keys [role branch]} :peter} users]
      (println "Peter is a" role "located at" branch))
      ;=> Peter is a clerk located at Zurich
)
```


### Destructuring Function Parameters

Destructuring can be used directly in function parameters, making it easy to pass 
and work with complex data structures.

```clojure
(do
  (defn greet [{:keys [name age]}]
    (println "Hello," name "you are" age "years old."))

  (greet {:name "Charlie" :age 25}))
```
