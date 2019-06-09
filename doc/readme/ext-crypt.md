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



## Encryption /Decryption