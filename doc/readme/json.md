# JSON

Venice has built-in support for reading/writing JSON from/to Venice data 
structures. No 3rd-party libraries are required.


## Usage

To convert to/from JSON strings, use *json/write-str* and *json/read-str*:

```clojure
(json/write-str {:a 1 :b 2})
;;=> "{\"a\":1,\"b\":2}"

(json/read-str """{"a":1,"b":2}""")
;;=> {"a" 1, "b" 2}
```

Note that these operations are not symmetric. Converting Venice data into JSON is lossy. 
JSON has a restricted set of data types so not all Venice datatypes can be adequately 
converted. E.g. there is no real decimal type and Venice `int` is converted to `long`.


### Streams

JSON can be spit to Java OutputStreams, Writers, or files

```clojure
(let [out (io/bytebuf-out-stream)]
  (json/spit out {:a 100 :b 100 :c [10 20 30]})
  (try-with [in (io/bytebuf-in-stream @out)]
     (pr-str (json/slurp in))))
;;=> "{\"a\":100,\"b\":100,\"c\":[10,20,30]}"
```

JSON can be slurped from Java InputStreams, Readers, or files

```clojure
(let [json (json/write-str {:a 100 :b 100})]
  (try-with [is (io/string-in-stream json)]
    (pr-str (json/slurp is))))
;;=> "{a 100 b 100}"
```


### Converting JSON object key/value types

Map JSON object keys to keywords

```clojure
(json/read-str """{"a":100,"b":100}""" :key-fn keyword)
;;=> {:a 100 :b 100}
```

Map JSON object values to local-date-time

```clojure
(json/read-str """{"a": "2018-08-01T10:15:30", "b": 100}""" 
               :value-fn (fn [k v] (if (== "a" k) (time/local-date-time v) v)))
;;=> {"a" 2018-08-01T10:15:30 "b" 100}
```

```clojure
(json/read-str """{"a": "2018-08-01T10:15:30", "b": 100}""" 
               :key-fn keyword 
               :value-fn (fn [k v] (if (== :a k) (time/local-date-time v) v)))
;;=> {:a 2018-08-01T10:15:30 :b 100}
```


### Special data types

Decimals are converted to strings

```clojure
(json/write-str {:a 100.23M})
;;=> "{\"a\":\"100.23\"}"
```

Decimals can be forced to be converted to doubles:

```clojure
(json/write-str {:a 100.23M} :decimal-as-double true)
;;=> "{\"a\":100.23}"
```

Read doubles as decimals without precision loss. 
The decimals are converted from the read string without
intermediate double conversion:

```clojure
(json/read-str """{"a":10.33}""" :decimal true)
;;=> {"a" 10.33M}
```


Binary data is converted to a _Base64_ encoded string

```clojure
(json/write-str {:a (bytebuf-from-string "abcdefgh" :utf-8)})
;;=> "{\"a\":\"YWJjZGVmZ2g=\"}"
```

Date/Time data types are formatted as ISO date/time strings 

```clojure
(json/write-str {:a (time/local-date 2018 8 1)})
;;=> "{\"a\":\"2018-08-01\"}"

(json/write-str {:a (time/local-date-time "2018-08-01T14:20:10.200")})
;;=> "{\"a\":\"2018-08-01T14:20:10.2\"}"

(json/write-str {:a (time/zoned-date-time "2018-08-01T14:20:10.200+01:00")})
;;=> "{\"a\":\"2018-08-01T14:20:10.2+01:00\"}"
```

Ints are converted to longs with write/read

```clojure
(json/read-str (json/write-str {:a 100I}))
;;=> {"a" 100}
```

