# JSON Lines

*JSON Lines* essentially consists of several lines where each individual line is a valid JSON object, separated by a newline character `\n`.

Venice has built-in support for the *JSON Lines* text format as described in 
[JSON Lines](https://jsonlines.org/). It reads/writes *JSON Lines* from/to 
Venice data structures. No 3rd-party libraries are required.


## Usage

To convert to/from a *JSON Line* string, use *jsonl/write-str* and *jsonl/read-str*:

```clojure
(do
  (load-module :jsonl)
  
  ;; write two JSON lines (passing as list with two values)
  (println (jsonl/write-str [{"a" 10 :b 20} {"a" 11 "b" 21}])))
  
;; output (a two-line string)
;; {"a":10,"b":20}
;; {"a":11,"b":21}
```

```clojure
(do
  (load-module :jsonl)

  ;; read three JSON lines (returned as list with three values)
  (println (jsonl/read-str """
                           {"a":10,"b":20}
                           {"a":11,"b":21}
                           {"a":12,"b":23}
                           """)))
                           
;; output (a Venice list with 3 maps)
;; ({"a" 10 "b" 20} {"a" 11 "b" 21} {"a" 12 "b" 23})
```

Note that these operations are not symmetric. Converting Venice data into JSON is lossy. 
JSON has a restricted set of data types so not all Venice datatypes can be adequately 
converted. E.g. there is no real decimal type and Venice `int` is converted to `long`.


### Streams

*JSON Lines* can be spit to Java OutputStreams, Writers, or files:
 * `io/bytebuf-out-stream`
 * `io/file-out-stream`
 * `io/buffered-writer`
 * `io/file`

```clojure
(do
  (load-module :jsonl)
  
  ;; spit a list of json lines (linefeeds are added implicitly )
  (try-with [wr (io/buffered-writer (io/file "data.jsonl"))]
     (jsonl/spit wr [{"a" 100, "b" 200} 
                     {"a" 101, "b" 201} 
                     {"a" 102, "b" 202}])
     (flush wr)))
```

```clojure
(do
  (load-module :jsonl)
  
  ;; spit a list of json lines, line by line (linefeeds must be added exlicitly)
  (try-with [wr (io/buffered-writer (io/file "data.jsonl"))]
    (jsonl/spit wr {"a" 100, "b" 200})
    (println wr)
    (jsonl/spit wr {"a" 101, "b" 201})
    (println wr)
    (jsonl/spit wr {"a" 102, "b" 202})
    (flush wr)))
```


*JSON Lines* can be slurped from byte buffers, Java InputStreams, Readers, or files:
 * `bytebuf`
 * `io/file-in-stream`
 * `io/bytebuf-in-stream`
 * `io/buffered-reader`
 * `io/file`

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/slurp (io/file "data.jsonl")))
```

```clojure
(do
  (load-module :jsonl)
  
  (try-with [rd (io/buffered-reader (io/file "data.jsonl"))]
    (jsonl/slurp rd)))
```

For memory efficient reading of large *JSON Lines* datasets use a transducer with 
filter-map-reduce functionality:

_Note: make sure that Venice' up-front macro expansion is activated when processing large datasets to get best performance!_

```clojure
(do
  (load-module :jsonl)

  (defn test-data [lines]
    (let [template {"a" 100, "b" 200}
          data     (reduce #(conj %1 (assoc template :id %2)) 
                           [] 
                           (range 0 lines))]
      (jsonl/write-str data)))

  ;; transducer filter-map
  (def xform (comp (map #(dissoc % :c))
                   (map #(update % :b (fn [x] (+ x 5))))
                   (filter #(= 100 (:a %)))))

  (let [json (test-data 1_000)]
    (try-with [rd (io/buffered-reader json)]
      (let [slurper (jsonl/lazy-seq-slurper rd :key-fn keyword)]
        ;; transduce the lazy sequence
        (pr-str (transduce xform conj slurper))))))
```



### Converting JSON Lines object key/value types

Venice supports for *JSON Lines* the same data type customizations as for its standard 
JSON handling.

**Map JSON object keys to keywords**

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/read-str """{"a":100,"b":100}""" :key-fn keyword)
  ;;=> {:a 100 :b 100}
)
```

**Mapping JSON Lines object values explicitly**

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/read-str """{"a": "2018-08-01T10:15:30", "b": "100.23", "c": 100}""" 
                  :key-fn keyword 
                  :value-fn (fn [k v] (case k 
                                        :a (time/local-date-time v)
                                        :b (decimal v) 
                                        v)))
                 
  ;;=> {:a 2018-08-01T10:15:30 :b 100.23M :c 100}
)
```

Note: the value function `value-fn` is applied after the key function `key-fn` and thus receives the mapped keys


### Special data types

#### Decimals

When dealing with floating-point numbers, we often encounter rounding 
errors known as the double precision issue.

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/write-str {:a (+ 0.1 0.2)})
  ;;=> "{\"a\":0.30000000000000004}"
)
```

Decimals avoid this problem and are the means of choice when dealing
with financial amounts but JSON does not support decimals as data type.


Venice decimals are converted to strings by default:

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/write-str {:a 100.23M})
  ;;=> "{\"a\":\"100.23\"}"
)
```

But Venice decimals can also be forced to be converted to doubles:

```clojure
(do
  (load-module :jsonl)
  
  (json/write-str {:a (+ 0.1M 0.2M)} :decimal-as-double true)
  ;;=> "{\"a\":0.3}"
  
  (jsonl/write-str {:a 100.23M} :decimal-as-double true)
  ;;=> "{\"a\":100.23}"
)
```

Venice can emit decimals as 'double' floating-point values in 
exact representation. On reading back this floating-point string
is directly converted into a decimal without intermediate double 
conversion, thus keeping the precision and allow for full decimal 
value range.

```clojure
(do
  (load-module :jsonl)
 
  (jsonl/write-str {:a 100.33M} :decimal-as-double true)
  ;;=> "{\"a\":100.33}"

  (jsonl/write-str {:a 99999999999999999999999999999999999999999999999999.33M} 
                   :decimal-as-double true)
  ;;=> "{\"a\":99999999999999999999999999999999999999999999999999.33}"
  
  
  (jsonl/read-str """{"a":10.33}""" :decimal true)
  ;;=> {"a" 10.33M}  
  
  (jsonl/read-str """{"a":99999999999999999999999999999999999999999999999999.33}""" 
                  :decimal true)
  ;;=> {"a" 99999999999999999999999999999999999999999999999999.33M}  
)
```

Parsing decimals explicitly:

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/read-str """{"a": "2018-08-01T10:15:30", "b": "100.23"}""" 
                  :key-fn keyword 
                  :value-fn (fn [k v] (case k 
                                        :a (time/local-date-time v)
                                        :b (decimal v) 
                                        v)))
                 
  ;;=> {:a 2018-08-01T10:15:30 :b 100.23M}
)
```


#### Binary data

Venice binary data is converted to a _Base64_ encoded string:

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/write-str {:a (bytebuf-from-string "abcdefgh" :utf-8)})
  
  ;;=> "{\"a\":\"YWJjZGVmZ2g=\"}"
)
```


#### Date & Time

Venice date/time data types are formatted as ISO date/time strings: 

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/write-str {:a (time/local-date 2018 8 1)})
  ;;=> "{\"a\":\"2018-08-01\"}"

  (jsonl/write-str {:a (time/local-date-time "2018-08-01T14:20:10.200")})
  ;;=> "{\"a\":\"2018-08-01T14:20:10.2\"}"

  (jsonl/write-str {:a (time/zoned-date-time "2018-08-01T14:20:10.200+01:00")})
  ;;=> "{\"a\":\"2018-08-01T14:20:10.2+01:00\"}"
)
```


#### Integers

JSON does not distinguish between integer and long values hence Venice integers 
are converted to longs always on JSON write/read:

```clojure
(do
  (load-module :jsonl)
  
  (-> (jsonl/write-str {:a 100I})
      (jsonl/read-str :key-fn keyword))
  
  ;;=> {:a 100}
)
```

However, if integers are required they can be parsed explicitly:

```clojure
(do
  (load-module :jsonl)
  
  (-> (jsonl/write-str {:a 100I})
      (jsonl/read-str :key-fn keyword 
                      :value-fn (fn [k v] (case k 
                                            :a (int v) 
                                            v))))
                 
  ;;=> {:a 100I}
)
```

