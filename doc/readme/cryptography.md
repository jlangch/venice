# Cryptography

* [File Encryption](#file-encryption)
* [File Hashing](#file-hashing)
* [String Encryption](#string-encryption)
* [String and Password Hashing](#string-and-password-hashing)


## File Encryption

Venice supports encrypting and decrypting files, streams and buffers using 
AES and ChaCha20, both with 256 bit keys:

  * AES256-GCM ¹⁾
  * AES256-CBC ²⁾
  * ChaCha20 ³⁾
  * ChaCha20-BC ⁴⁾
  * AES-ZIP ⁵⁾
  
¹⁾ Recommended by NIST

²⁾ AES256-CBC is regarded as a broken or risky  cryptographic algorithm
   (CWE-327, CWE-328). Use AES256-GCM in production!
   
³⁾ only available with Java 11+

⁴⁾ only available with BouncyCastle libraries but works with Java 8+
    
⁵⁾ AES-256 encrypted and password protected ZIP files

    
Warning: files encrypted with ChaCha20 cannot be decrypted 
by ChaCha20-BC (and vice versa) due to different initial 
counter  handling and IV size (96bit vs 64bit)

The ChaCha family of ciphers are an oder of magnitude more efficient 
on servers that do not provide hardware acceleration. Apple Silicon
does not seem to have AES hardware acceleration probably due to its 
RISC nature. 


### Examples

**AES and ChaCha encrypted files:**

```clojure
(do
  (load-module :crypt)
 
  (let [algo       "AES256-GCM"  ;; "AES256-CBC", "AES256-GCM, "ChaCha20", "ChaCha20-BC"
        data       (bytebuf-allocate-random 100)
        file-in    (io/temp-file "test-", ".data")
        file-enc   (io/temp-file "test-", ".data.enc")
        file-dec   (io/temp-file "test-", ".data.dec")]
    (io/delete-file-on-exit file-in file-enc file-dec)
    (io/spit file-in data :binary true)
    
    (crypt/encrypt-file algo "-passphrase-" file-in file-enc)
    (crypt/decrypt-file algo "-passphrase-" file-enc file-dec)))
```

`crypt/encrypt-file` and `crypt/decrypt-file` work both on files, streams 
and memory buffers.


**AES encrypted ZIP files:**

```clojure
(do
  (load-module :zipvault)
  
  (let [data       (bytebuf-allocate-random 100)
        file-in    (io/temp-file "test-", ".data")
        file-zip   (io/temp-file "test-", ".data.zip")
        file-unzip (io/temp-file "test-", ".data.unzip")
        entry-name (io/file-name file-unzip)
        dest-dir   (io/file-parent file-unzip)]
    (io/delete-file-on-exit file-in file-zip file-unzip)
    (io/spit file-in data :binary true)
    
    (zipvault/zip file-zip "-passphrase-" entry-name file-in)
    (zipvault/extract-file file-zip "-passphrase-" entry-name dest-dir)))
```

`zipvault/zip` and `zipvault/extract-file` work both on files, streams 
and memory buffers.


### Perfomance

```
                     MacBookAir M2, Java 8 (Zulu), BouncyCastle 1.77
--------------------------------------------------------------------
                         2KB    20KB   200KB     2MB    20MB   200MB
--------------------------------------------------------------------
Encrypt AES-256 CBC:    85ms    65ms    66ms    74ms   172ms  1165ms
Decrypt AES-256 CBC:    67ms    67ms    65ms    76ms   162ms  1053ms
Encrypt AES-256 GCM:    64ms    65ms    70ms    96ms   364ms  3170ms
Decrypt AES-256 GCM:    66ms    65ms    67ms    94ms   363ms  3215ms
Encrypt AES-256 ZIP:    11ms     5ms    10ms    60ms   565ms  5681ms
Decrypt AES-256 ZIP:     7ms     5ms     6ms    24ms   204ms  2045ms
Encrypt ChaCha20:          -       -       -       -       -       -
Decrypt ChaCha20:          -       -       -       -       -       -
Encrypt ChaCha20-BC:    75ms    63ms    66ms    71ms   127ms   701ms
Decrypt ChaCha20-BC:    66ms    65ms    65ms    71ms   127ms   704ms
--------------------------------------------------------------------
```

```
                    MacBookAir M2, Java 17 (Zulu), BouncyCastle 1.77
--------------------------------------------------------------------
                         2KB    20KB   200KB     2MB    20MB   200MB
--------------------------------------------------------------------
Encrypt AES-256 CBC:    96ms    73ms    73ms    84ms   193ms  1337ms
Decrypt AES-256 CBC:    75ms    72ms    74ms    85ms   195ms  1562ms
Encrypt AES-256 GCM:    73ms    72ms    75ms   103ms   388ms  3593ms
Decrypt AES-256 GCM:    75ms    73ms    76ms   103ms   392ms  3283ms
Encrypt AES-256 ZIP:     7ms     5ms     9ms    60ms   600ms  5900ms
Decrypt AES-256 ZIP:     7ms     4ms     7ms    26ms   240ms  2311ms
Encrypt ChaCha20:       83ms    72ms    73ms    77ms   119ms   566ms
Decrypt ChaCha20:       73ms    72ms    73ms    76ms   118ms   527ms
Encrypt ChaCha20-BC:    74ms    73ms    73ms    87ms   160ms   949ms
Decrypt ChaCha20-BC:    74ms    73ms    74ms    85ms   160ms   931ms
--------------------------------------------------------------------
```


## File Hashing

Venice computes hashes for files, streams, and buffers with the 
algorithms MD5, SHA-1, and SHA-256.

Warning: The MD5 hash function’s security is considered to be 
severely compromised. Collisions can be found within seconds, 
and they can be used for malicious purposes. 


### Examples

**SHA-1**

```clojure
(do
  (load-module :crypt)
  
  (let [data   (bytebuf-allocate-random 100)
        file   (io/temp-file "test-", ".data")]
    (io/delete-file-on-exit file)
    (io/spit file data :binary true)
    
    (let [hash (crypt/hash-file "SHA-1" "-salt-" file)]
      (crypt/verify-file-hash "SHA-1" "-salt-" file hash))))
```


**SHA-256**

```clojure
(do
  (load-module :crypt)
  
  (let [data   (bytebuf-allocate-random 100)
        file   (io/temp-file "test-", ".data")]
    (io/delete-file-on-exit file)
    (io/spit file data :binary true)
    
    (let [hash (crypt/hash-file "SHA-256" "-salt-" file)]
      (crypt/verify-file-hash "SHA-256" "-salt-" file hash))))
```


### Perfomance


```
                                        MacBookAir M2, Java 8 (Zulu)
--------------------------------------------------------------------
                         2KB    20KB   200KB     2MB    20MB   200MB
--------------------------------------------------------------------
Hash MD5 (file):         1ms     1ms     1ms     6ms    56ms   547ms
Hash SHA-1 (file):       0ms     1ms     2ms     7ms    65ms   685ms
Hash SHA-256 (file):     0ms     0ms     2ms     8ms    77ms   764ms
Hash MD5 (memory):       0ms     1ms     1ms     5ms    54ms   535ms
Hash SHA-1 (memory):     0ms     1ms     1ms     7ms    64ms   642ms
Hash SHA-256 (memory):   1ms     0ms     1ms     8ms    76ms   749ms
--------------------------------------------------------------------
```



## String Encryption

Venice supports DES, 3DES and AES256.


### Encryption

Encrypting strings:

```clojure
(do
  (load-module :crypt) 

  ;; define the encryption function
  (def encrypt (crypt/encrypt "3DES" "secret" :url-safe true))
  
  (encrypt "hello") ; => "ndmW1NLsDHA"
  (encrypt "world") ; => "KPYjndkZ8vM"
) 
```

Encrypting bytebufs:

```clojure
(do
  (load-module :crypt) 
  (load-module :hexdump)

  ;; define the encryption function
  (def encrypt (crypt/encrypt "AES256" "secret" :url-safe true))
  
  (-> (encrypt (bytebuf [ 0  1  2  3  4  5  6  7  8  9 
                         10 11 12 13 14 15 16 17 18 19]))
      (hexdump/dump)))
```

Encrypts a string or a bytebuf. String data is returned as Base64 encoded string.

The :url-safe option controls the base64 encoding regarding URL safety.
If _true_ the base64 encoder will emit '-' and '_' instead of the usual 
'+' and '/' characters. Defaults to _false_.

Note: no padding is added when encoding using the URL-safe alphabet.

Supported algorithms: DES, 3DES, AES256


### Decryption

The crypt/decrypt function expects a Base64 encoded string or a bytebuf.

Decrypting strings:

```clojure
(do
  (load-module :crypt) 

  ;; define the encryption/decryption function
  (def encrypt (crypt/encrypt "3DES" "secret" :url-safe true))
  (def decrypt (crypt/decrypt "3DES" "secret" :url-safe true))
  
  (-> (encrypt "hello")
      (decrypt)))
```

Decrypting bytebufs:

```clojure
(do
  (load-module :crypt) 

  ;; define the decryption function
  (def decrypt (crypt/decrypt "3DES" "secret" :url-safe true))

  
  (-> (encrypt (bytebuf [ 0  1  2  3  4  5  6  7  8  9 
                         10 11 12 13 14 15 16 17 18 19]))
      (decrypt)))
```


## String and Password Hashing

Venice supports PBKDF2, SHA-512, SHA-1, and MD5 for hashing strings.


### PBKDF2

PBKDF2 is the preferred hashing algorithm to hash password.

Just using a salt:

```clojure
(do
  (load-module :crypt)

  (-> (crypt/pbkdf2-hash "hello world" "-salt-")
      (str/bytebuf-to-hex :upper)))
```

Specifying a salt, the number of iterations, and key length:

```clojure
(do
  (load-module :crypt)

  (-> (crypt/pbkdf2-hash "hello world" "-salt-" 1000 256)
      (str/bytebuf-to-hex :upper)))
```


### SHA-512

```clojure
(do
  (load-module :crypt)

  (-> (crypt/sha512-hash "hello world" "-salt-")
      (str/bytebuf-to-hex :upper)))
```

```clojure
(do
  (load-module :crypt)

  (-> (crypt/sha512-hash (bytebuf [54 78 99]) "-salt-")
      (str/bytebuf-to-hex :upper)))
```


### SHA-1

```clojure
(do
  (load-module :crypt)

  (-> (crypt/sha1-hash "hello world" "-salt-")
      (str/bytebuf-to-hex :upper)))
```

```clojure
(do
  (load-module :crypt)

  (-> (crypt/sha1-hash (bytebuf [54 78 99]) "-salt-")
      (str/bytebuf-to-hex :upper)))
```


### MD5

Warning: The MD5 hash function’s security is considered to be 
severely compromised. Collisions can be found within seconds, 
and they can be used for malicious purposes. 

```clojure
(do
  (load-module :crypt)

  (-> (crypt/md5-hash "hello world")
      (str/bytebuf-to-hex :upper)))
```

```clojure
(do
  (load-module :crypt)

  (-> (crypt/md5-hash (bytebuf [54 78 99]))
      (str/bytebuf-to-hex :upper)))
```


