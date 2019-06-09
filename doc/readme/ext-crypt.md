# Cryptographic Functions


## Hashes

### PBKDF2

Just using a salt ("1234"):

```clojure
(crypt/pbkdf2-hash "hello world" "1234")
```

Specifying a salt, the number of iterations, and key length:

```clojure
(crypt/pbkdf2-hash "hello world" "1234" 1000 256)
```


### MD5

Note: MD5 is not safe anymore use PBKDF2 instead

```clojure
(crypt/md5-hash "hello world")
```


## Encryption

```clojure
(do
  (def encrypt (crypt/encrypt "3DES" "secret" :url-safe true))
  (encrypt "hello") ; => "ndmW1NLsDHA"
  (encrypt "world") ; => "KPYjndkZ8vM"
  (encrypt (bytebuf [1 2 3 4 5])) ; => [234 220 237 189 12 176 242 147]
) 
```

Encrypts a string or a bytebuf. String data is returned as base64 encoded string.

The :url-safe option controls the base64 encoding regarding URL safety.
If true this base64 encoder will emit '-' and '_' instead of the usual 
'+' and '/' characters. Defaults to false.
Note: no padding is added when encoding using the URL-safe alphabet.

Supported algorithms: "DES", "3DES", "Blowfish", "AES256"


## Decryption

```clojure
(do
  (def decrypt (crypt/decrypt "3DES" "secret" :url-safe true))
  (decrypt "ndmW1NLsDHA") ; => "hello"
  (decrypt "KPYjndkZ8vM") ; => "world"
  (decrypt  (bytebuf [234 220 237 189 12 176 242 147])) ; => [1 2 3 4 5]
) 
```

The decrypt function expects a base64 encoded string or a bytebuf.
