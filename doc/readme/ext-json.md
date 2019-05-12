# JSON

Venice supports reading/writing JSON from/to Venice data structures


## Usage

To convert to/from JSON strings, use json/write-str and json/read-str:

```clojure
(json/write-str {:a 1 :b 2})
;;=> "{\"a\":1,\"b\":2}"

(json/read-str "{\"a\":1,\"b\":2}")
;;=> {"a" 1, "b" 2}
```

Note that these operations are not symmetric. Converting Venice data into JSON is lossy.


### Special data types

Decimals are converted to string

```clojure
(json/write-str {:a 100.23M})
;;=> "{\"a\":\"100.23\"}"
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




