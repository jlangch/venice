# JSON Lines

Venice has built-in support JSON Lines text format as described in [JSON Lines](https://jsonlines.org/). It reads/writes JSON Lines from/to Venice data 
structures. No 3rd-party libraries are required.


## Usage

To convert to/from JSON strings, use *jsonl/write-str* and *jsonl/read-str*:

```clojure
(do
  (load-module :jsonl)
  (println (jsonl/write-str [{"a" 10 :b 20} {"a" 11 "b" 21}])))
  
;; outputs
;; {"a":10,"b":20}
;; {"a":11,"b":21}


(do
  (load-module :jsonl)
  (println (jsonl/read-str """
                           {"a":10,"b":20}
                           {"a":11,"b":21}
                           {"a":12,"b":23}
                           """)))
;; outputs
;; ({"a" 10 "b" 20} {"a" 11 "b" 21} {"a" 12 "b" 23})
```

Note that these operations are not symmetric. Converting Venice data into JSON is lossy. 
JSON has a restricted set of data types so not all Venice datatypes can be adequately 
converted. E.g. there is no real decimal type and Venice `int` is converted to `long`.


### Streams

JSON Lines can be spit to Java OutputStreams, Writers, or files

```clojure
(do
  (load-module :jsonl)
  
  ;; spit a list of json lines
  (try-with [wr (io/buffered-writer (io/file "data.jsonl"))]
            (jsonl/spit wr [{"a" 100, "b" 200} 
                            {"a" 101, "b" 201} 
                            {"a" 102, "b" 202}])
            (flush wr)))
```

```clojure
(do
  (load-module :jsonl)
  
  ;; spit a list of json lines, line by line
  (try-with [wr (io/buffered-writer (io/file "data.jsonl"))]
            (jsonl/spit wr {"a" 100, "b" 200})
            (jsonl/spit wr {"a" 101, "b" 201})
            (jsonl/spit wr {"a" 102, "b" 202})
            (flush wr)))
```

JSON can be slurped from Java InputStreams, Readers, or files

```clojure
(do
  (load-module :jsonl)
  
  (try-with [rd (io/buffered-reader (io/file "data.jsonl"))]
    (jsonl/slurp rd)))
```


### Converting JSON object key/value types

Venice supports for JSON Lines the same data type customizations as for its standard 
JSON handling.

**Map JSON object keys to keywords**

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/read-str """{"a":100,"b":100}""" :key-fn keyword))
;;=> {:a 100 :b 100}
```

**Map JSON object values to local-date-time**

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/read-str """{"a": "2018-08-01T10:15:30", "b": 100}""" 
                 :value-fn (fn [k v] (if (== "a" k) (time/local-date-time v) v))))
;;=> {"a" 2018-08-01T10:15:30 "b" 100}
```

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/read-str """{"a": "2018-08-01T10:15:30", "b": 100}""" 
                 :key-fn keyword 
                 :value-fn (fn [k v] (if (== :a k) (time/local-date-time v) v))))
;;=> {:a 2018-08-01T10:15:30 :b 100}
```


### Special data types

Decimals are converted to strings

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/write-str {:a 100.23M}))
;;=> "{\"a\":\"100.23\"}"
```

Decimals can be forced to be converted to doubles:

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/write-str {:a 100.23M} :decimal-as-double true))
;;=> "{\"a\":100.23}"
```

Read doubles as decimals without precision loss. 
The decimals are converted from the read string without
intermediate double conversion:

```clojure
(do
  (load-module :jsonl)
  
  (json/read-str """{"a":10.33}""" :decimal true))
;;=> {"a" 10.33M}
```


Binary data is converted to a _Base64_ encoded string

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/write-str {:a (bytebuf-from-string "abcdefgh" :utf-8)}))
;;=> "{\"a\":\"YWJjZGVmZ2g=\"}"
```

Date/Time data types are formatted as ISO date/time strings 

```clojure
(do
  (load-module :jsonl)
  
  (jsonl/write-str {:a (time/local-date 2018 8 1)})
  ;;=> "{\"a\":\"2018-08-01\"}"

  (jsonl/write-str {:a (time/local-date-time "2018-08-01T14:20:10.200")})
  ;;=> "{\"a\":\"2018-08-01T14:20:10.2\"}"

  (jsonl/write-str {:a (time/zoned-date-time "2018-08-01T14:20:10.200+01:00")}))
  ;;=> "{\"a\":\"2018-08-01T14:20:10.2+01:00\"}"
```

Ints are converted to longs with write/read

```clojure
(json/read-str (json/write-str {:a 100I}))
;;=> {"a" 100}
```

