# Cryptography

* [File Encryption](#file-encryption)
* [File Hashing](#file-hashing)
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

²⁾ AES256-CBC is regarded as a broken or risky cryptographic algorithm. Use AES256-GCM or ChaCha20 in production!
   
³⁾ only available with Java 11+

⁴⁾ only available with BouncyCastle libraries but works with Java 8+,
   there is no need to register the BouncyCastle provider with the Java VM.
    
⁵⁾ AES-256 encrypted and password protected ZIP files

    
Warning: files encrypted with ChaCha20 cannot be decrypted by
ChaCha20-BC (and vice versa) due to different initial counter handling 
and the IV size (12 bytes vs 8 bytes)

The ChaCha family of ciphers are an oder of magnitude more efficient 
on servers that do not provide hardware acceleration. Apple Silicon
does not seem to have AES hardware acceleration probably due to its 
RISC nature. 

**Deriving a 256 bit key from a passhrase**

The 256 bit encryption key is derived from the passphrase using a
*PBKDF2WithHmacSHA256* secret key factory with a 16 byte random salt 
and 3000 iterations. Carefully choose a long enough passphrase.

You can pass your own key salt and iteration count to all encryptors.


**IV, Nonce, Counter**

*IV*, *Nonce* and/or *Counter* are random and unique for every call
of the encryption functions.
          
While encrypting a file the *IV*, *Nonce* and/or *Counter* are written to the 
start of the encrypted file and extracted before decrypting the file:

```
      AES256-GCM              AES256-CBC               ChaCha20              ChaCha20-BC
   AES/GCM/NoPadding     AES/CBC/PKCS5Padding                               (BouncyCastle)
+--------------------+  +--------------------+  +--------------------+  +--------------------+
|       iv  (12)     |  |      iv  (16)      |  |      nonce (12)    |  |       iv (8)       |
+--------------------+  +--------------------+  +--------------------+  +--------------------+
|       data (n)     |  |      data (n)      |  |     counter (4)    |  |      data (n)      | 
+--------------------+  +--------------------+  +--------------------+  +--------------------+
                                                |      data (n)      | 
                                                +--------------------+
                                                
¹⁾ Only used when files are encrypted with passphrases
```


### Examples


**Encrypted binary buffers:**

```clojure
(do
  (load-module :crypt)
 
  ;; available encryptors
  ;;   - AES256-GCM:   crypt/encryptor-aes-256-gcm
  ;;   - AES256-CBC:   crypt/encryptor-aes-256-cbc
  ;;   - ChaCha20:     crypt/encryptor-chacha20
  ;;   - ChaCha20-BC:  crypt/encryptor-chacha20-bouncycastle
  
  (let [encryptor  (crypt/encryptor-aes-256-gcm "secret")
        data       (bytebuf-allocate-random 100)]
    
    (->> (encryptor :encrypt data)   ;; encrypt data
         (encryptor :decrypt))))     ;; decrypt data
```

**Encrypted files:**

```clojure
(do
  (load-module :crypt)
 
  ;; available encryptors
  ;;   - AES256-GCM:   crypt/encryptor-aes-256-gcm
  ;;   - AES256-CBC:   crypt/encryptor-aes-256-cbc
  ;;   - ChaCha20:     crypt/encryptor-chacha20
  ;;   - ChaCha20-BC:  crypt/encryptor-chacha20-bouncycastle
  
  (let [encryptor  (crypt/encryptor-aes-256-gcm "secret")
        data       (bytebuf-allocate-random 100)
        file-in    (io/temp-file "test-", ".data")
        file-enc   (io/temp-file "test-", ".data.enc")
        file-dec   (io/temp-file "test-", ".data.dec")]
    (io/delete-file-on-exit file-in file-enc file-dec)
    (io/spit file-in data :binary true)
    
    ;; encrypt file-in -> file-enc (overwrite dest file)
    (encryptor :encrypt file-in file-enc true)
    
    ;; decrypt file-enc -> file-dec (overwrite dest file)
    (encryptor :decrypt file-enc file-dec true)))
```

**Encrypted string:**

```clojure
(do
  (load-module :crypt)
 
  ;; available encryptors
  ;;   - AES256-GCM:   crypt/encryptor-aes-256-gcm
  ;;   - AES256-CBC:   crypt/encryptor-aes-256-cbc
  ;;   - ChaCha20:     crypt/encryptor-chacha20
  ;;   - ChaCha20-BC:  crypt/encryptor-chacha20-bouncycastle
  
  (let [encryptor  (crypt/encryptor-aes-256-gcm "secret")
        data       "hello-world"]
    
    ;; Text 
    ;; - encrypt text to Base64 encoded data
    ;; - decrypt Base64 encoded data to text
    ;; Supports Base64 :Standard (RFC4648) or :UrlSafe (RFC4648_URLSAFE) format
    
    ;; use Base64 :Standard () encoding
    (-<> (encryptor :encrypt data :Standard)   ;; encrypt text and encode to Base64 :Standard
         (encryptor :decrypt <> :Standard))))  ;; decrypt Base64 :Standard encoded data to text
```


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


### Configuring Encryptors

**AES256-GCM**

```clojure
(crypt/encryptor-aes-256-gcm "your-passphrase"
```
Passing an explicit salt and iteration count:

```clojure
(crypt/encryptor-aes-256-gcm 
   "your-passphrase"
   :key-salt          (bytebuf [0x34 0x7F 0x45 0xAE  0x09 0xF0 0xE4 0x7B  
                                0x78 0xC4 0xDA 0x66  0x51 0xA0 0xFF 0x21])
   :key-iterations    10000)
```

**AES256-CBC**

```clojure
(crypt/encryptor-aes-256-cbc "your-passphrase"
```
Passing an explicit salt and iteration count:

```clojure
(crypt/encryptor-aes-256-cbc 
   "your-passphrase"
   :key-salt              (bytebuf [0x34 0x7F 0x45 0xAE  0x09 0xF0 0xE4 0x7B  
                                    0x78 0xC4 0xDA 0x66  0x51 0xA0 0xFF 0x21])
   :key-iterations        10000
   :custom-iv             (bytebuf [0x00 0x00 0x00 0x00  0x00 0x00 0x00 0x00
                                    0x00 0x00 0x00 0x00  0x00 0x00 0x00 0x00])
   :custom-iv-add-to-data false)
```

**ChaCha20**

```clojure
(crypt/encryptor-chacha20 "your-passphrase"
```
Passing an explicit salt and iteration count:

```clojure
(crypt/encryptor-chacha20 
   "your-passphrase"
   :key-salt         (bytebuf [0x34 0x7F 0x45 0xAE  0x09 0xF0 0xE4 0x7B  
                               0x78 0xC4 0xDA 0x66  0x51 0xA0 0xFF 0x21])
   :key-iterations   10000)
```

**ChaCha20-BC**

```clojure
(crypt/encryptor-chacha20-bouncycastle "your-passphrase"
```
Passing an explicit salt and iteration count:

```clojure
(crypt/encryptor-chacha20-bouncycastle
   "your-passphrase"
   :key-salt          (bytebuf [0x34 0x7F 0x45 0xAE  0x09 0xF0 0xE4 0x7B 
                                0x78 0xC4 0xDA 0x66  0x51 0xA0 0xFF 0x21])
   :key-iterations    10000)
```



### Encrypt/decrypt a file tree

Encrypt all "*.doc" and "*.docx" in a file tree:

```clojure
(do 
  (load-module :crypt)
    
  (defn encrypted-file-name [f]
    (io/file (str (io/file-path f) ".enc")))
  
  (defn decrypted-file-name [f]
    (io/file (str/strip-end path ".enc")))
  
  (defn encrypt [dir passphrase]
    (let [encryptor (crypt/encryptor-aes-256-gcm passphrase)]
      (->> (io/list-file-tree-lazy dir #(io/file-ext? % ".doc" ".docx"))
           (docoll #(encryptor :encrypt % (encrypted-file-name %) true)))))

  (defn decrypt [dir passphrase]
    (let [encryptor (crypt/encryptor-aes-256-gcm passphrase)]
      (->> (io/list-file-tree-lazy dir #(io/file-ext? % ".enc"))
           (docoll #(encryptor :decrypt % (decrypted-file-name %) true)))))

  ;; (encrypt "/data/docs" "-passphrase-")
  ;; (decrypt "/data/docs" "-passphrase-")
  )
```


### Performance Byte Buffer

Test encrypt/decrypt byte buffer

```
                              MacBookAir M2, Java 8 (Zulu), BouncyCastle 1.77
┏━━━━━━━━━━━━━━━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┓
┃                      │    2KB │   20KB │  200KB │    2MB │   20MB │  204MB ┃
┣━━━━━━━━━━━━━━━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┫
┃ AES-256 CBC  encrypt │      1 │      1 │      2 │      9 │     89 │    926 ┃
┃ AES-256 CBC  decrypt │      0 │      0 │      1 │      8 │     84 │    869 ┃
┃ AES-256 GCM  encrypt │      1 │      1 │      4 │     29 │    293 │   2947 ┃
┃ AES-256 GCM  decrypt │      0 │      1 │      4 │     29 │    291 │   3028 ┃
┃ ChaCha20     encrypt │      - │      - │      - │      - │      - │      - ┃
┃ ChaCha20     decrypt │      - │      - │      - │      - │      - │      - ┃
┃ ChaCha20-BC  encrypt │      1 │      0 │      0 │      5 │     52 │    564 ┃
┃ ChaCha20-BC  decrypt │      0 │      1 │      1 │      5 │     52 │    542 ┃
┗━━━━━━━━━━━━━━━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┛
                                               (elapsed time in milliseconds)
```

```
                             MacBookAir M2, Java 17 (Zulu), BouncyCastle 1.77
┏━━━━━━━━━━━━━━━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┓
┃                      │    2KB │   20KB │  200KB │    2MB │   20MB │  204MB ┃
┣━━━━━━━━━━━━━━━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┫
┃ AES-256 CBC  encrypt │      1 │      1 │      1 │     11 │    117 │   1172 ┃
┃ AES-256 CBC  decrypt │      1 │      0 │      1 │     11 │    116 │   1221 ┃
┃ AES-256 GCM  encrypt │      0 │      0 │      4 │     31 │    317 │   3160 ┃
┃ AES-256 GCM  decrypt │      1 │      0 │      3 │     31 │    314 │   3142 ┃
┃ ChaCha20     encrypt │      0 │      1 │      1 │      4 │     32 │    322 ┃
┃ ChaCha20     decrypt │      0 │      0 │      1 │      4 │     31 │    308 ┃
┃ ChaCha20-BC  encrypt │      1 │      0 │      1 │      7 │     77 │    775 ┃
┃ ChaCha20-BC  decrypt │      0 │      0 │      1 │      7 │     77 │    771 ┃
┗━━━━━━━━━━━━━━━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┛
                                               (elapsed time in milliseconds)
```

```
                             MacBookAir M2, Java 21 (Zulu), BouncyCastle 1.77
┏━━━━━━━━━━━━━━━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┓
┃                      │    2KB │   20KB │  200KB │    2MB │   20MB │  204MB ┃
┣━━━━━━━━━━━━━━━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┫
┃ AES-256 CBC  encrypt │      1 │      0 │      1 │      4 │     33 │    325 ┃
┃ AES-256 CBC  decrypt │      0 │      0 │      0 │      2 │     20 │    200 ┃
┃ AES-256 GCM  encrypt │      1 │      0 │      0 │      1 │      8 │     83 ┃
┃ AES-256 GCM  decrypt │      0 │      1 │      0 │      1 │      6 │     64 ┃
┃ ChaCha20     encrypt │      1 │      0 │      0 │      2 │     16 │    159 ┃
┃ ChaCha20     decrypt │      0 │      0 │      0 │      2 │     15 │    149 ┃
┃ ChaCha20-BC  encrypt │      0 │      1 │      1 │      6 │     50 │    503 ┃
┃ ChaCha20-BC  decrypt │      0 │      0 │      1 │      6 │     51 │    512 ┃
┗━━━━━━━━━━━━━━━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┛
                                               (elapsed time in milliseconds)
```


### Performance File

Test encrypt: read file -> encrypt -> write file
Test decrypt: read file -> decrypt -> write file

```
                              MacBookAir M2, Java 8 (Zulu), BouncyCastle 1.77
┏━━━━━━━━━━━━━━━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┓
┃                      │    2KB │   20KB │  200KB │    2MB │   20MB │  204MB ┃
┣━━━━━━━━━━━━━━━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┫
┃ AES-256 CBC  encrypt │      1 │      1 │      1 │     10 │    104 │   1042 ┃
┃ AES-256 CBC  decrypt │      3 │      0 │      1 │     10 │     99 │   1018 ┃
┃ AES-256 GCM  encrypt │      1 │      1 │      4 │     31 │    305 │   3034 ┃
┃ AES-256 GCM  decrypt │      5 │      1 │      3 │     30 │    304 │   3026 ┃
┃ ChaCha20     encrypt │      - │      - │      - │      - │      - │      - ┃
┃ ChaCha20     decrypt │      - │      - │      - │      - │      - │      - ┃
┃ ChaCha20-BC  encrypt │      5 │      0 │      1 │      6 │     63 │    631 ┃
┃ ChaCha20-BC  decrypt │      5 │      0 │      1 │      7 │     63 │    636 ┃
┃ AES-256 ZIP  encrypt │     19 │      6 │      9 │     60 │    569 │   5617 ┃
┃ AES-256 ZIP  decrypt │      9 │      4 │      6 │     26 │    232 │   2165 ┃
┗━━━━━━━━━━━━━━━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┛
                                               (elapsed time in milliseconds)
```

```
                             MacBookAir M2, Java 17 (Zulu), BouncyCastle 1.77
┏━━━━━━━━━━━━━━━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┓
┃                      │    2KB │   20KB │  200KB │    2MB │   20MB │  204MB ┃
┣━━━━━━━━━━━━━━━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┫
┃ AES-256 CBC  encrypt │      1 │      1 │      2 │     13 │    127 │   1275 ┃
┃ AES-256 CBC  decrypt │      6 │      1 │      1 │     13 │    126 │   1265 ┃
┃ AES-256 GCM  encrypt │      1 │      1 │      4 │     33 │    337 │   3318 ┃
┃ AES-256 GCM  decrypt │      2 │      0 │      3 │     33 │    329 │   3284 ┃
┃ ChaCha20     encrypt │      1 │      1 │      1 │      4 │     42 │    426 ┃
┃ ChaCha20     decrypt │      5 │      0 │      1 │      5 │     41 │    427 ┃
┃ ChaCha20-BC  encrypt │      5 │      1 │      1 │      9 │     87 │    887 ┃
┃ ChaCha20-BC  decrypt │      5 │      0 │      1 │      9 │     88 │    868 ┃
┃ AES-256 ZIP  encrypt │     20 │      4 │     10 │     61 │    587 │   5924 ┃
┃ AES-256 ZIP  decrypt │      9 │      4 │      6 │     28 │    243 │   2376 ┃
┗━━━━━━━━━━━━━━━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┛
                                               (elapsed time in milliseconds)
```

```
                             MacBookAir M2, Java 21 (Zulu), BouncyCastle 1.77
┏━━━━━━━━━━━━━━━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┓
┃                      │    2KB │   20KB │  200KB │    2MB │   20MB │  204MB ┃
┣━━━━━━━━━━━━━━━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┫
┃ AES-256 CBC  encrypt │      2 │      1 │      1 │      5 │     55 │    435 ┃
┃ AES-256 CBC  decrypt │     11 │      2 │      0 │      3 │     29 │    299 ┃
┃ AES-256 GCM  encrypt │      1 │      1 │      3 │     23 │    219 │   2209 ┃
┃ AES-256 GCM  decrypt │     17 │      0 │      3 │     23 │    219 │   2068 ┃
┃ ChaCha20     encrypt │      1 │      1 │      0 │      3 │     26 │    263 ┃
┃ ChaCha20     decrypt │      5 │      0 │      1 │      3 │     24 │    262 ┃
┃ ChaCha20-BC  encrypt │      5 │      1 │      1 │      7 │     60 │    623 ┃
┃ ChaCha20-BC  decrypt │      5 │      0 │      1 │      6 │     61 │    656 ┃
┃ AES-256 ZIP  encrypt │     18 │      2 │      6 │     57 │    569 │   5740 ┃
┃ AES-256 ZIP  decrypt │      6 │      2 │      4 │     20 │    190 │   1893 ┃
┗━━━━━━━━━━━━━━━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┛
                                               (elapsed time in milliseconds)
```


## File Hashing

Venice computes hashes for files, streams, and buffers with the 
algorithms MD5, SHA-1, SHA-256, and SHA-512.

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


**SHA-512**

```clojure
(do
  (load-module :crypt)
  
  (let [data   (bytebuf-allocate-random 100)
        file   (io/temp-file "test-", ".data")]
    (io/delete-file-on-exit file)
    (io/spit file data :binary true)
    
    (let [hash (crypt/hash-file "SHA-512" "-salt-" file)]
      (crypt/verify-file-hash "SHA-512" "-salt-" file hash))))
```


### Performance

Test: read file -> hash

```
                                             MacBookAir M2, Java 8 (Zulu)
┏━━━━━━━━━━━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┓
┃                  │    2KB │   20KB │  200KB │    2MB │   20MB │  200MB ┃
┣━━━━━━━━━━━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┫
┃ MD5 (file)       │      1 │      1 │      1 │      6 │     62 │    549 ┃
┃ SHA-1 (file)     │      1 │      1 │      2 │      7 │     97 │    673 ┃
┃ SHA-256 (file)   │      1 │      1 │      1 │      9 │     77 │    812 ┃
┃ MD5 (memory)     │      3 │      1 │      1 │      6 │     54 │    527 ┃
┃ SHA-1 (memory)   │      0 │      0 │      2 │      7 │     62 │    651 ┃
┃ SHA-256 (memory) │      1 │      1 │      1 │      8 │     76 │    744 ┃
┗━━━━━━━━━━━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┛
                                           (elapsed time in milliseconds)
```

```
                                            MacBookAir M2, Java 17 (Zulu)
┏━━━━━━━━━━━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┓
┃                  │    2KB │   20KB │  200KB │    2MB │   20MB │  200MB ┃
┣━━━━━━━━━━━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┫
┃ MD5 (file)       │      1 │      0 │      1 │      3 │     36 │    339 ┃
┃ SHA-1 (file)     │      1 │      1 │      1 │      7 │     56 │    559 ┃
┃ SHA-256 (file)   │      0 │      1 │      2 │      9 │     88 │    877 ┃
┃ MD5 (memory)     │      0 │      0 │      1 │      4 │     32 │    321 ┃
┃ SHA-1 (memory)   │      1 │      1 │      1 │      5 │     54 │    542 ┃
┃ SHA-256 (memory) │      0 │      0 │      1 │      9 │     86 │    858 ┃
┗━━━━━━━━━━━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┛
                                           (elapsed time in milliseconds)
```

```
                                            MacBookAir M2, Java 21 (Zulu)
┏━━━━━━━━━━━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┯━━━━━━━━┓
┃                  │    2KB │   20KB │  200KB │    2MB │   20MB │  200MB ┃
┣━━━━━━━━━━━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┿━━━━━━━━┫
┃ MD5 (file)       │      4 │      1 │      1 │      5 │     43 │    340 ┃
┃ SHA-1 (file)     │      1 │      1 │      1 │      2 │     17 │    130 ┃
┃ SHA-256 (file)   │      0 │      0 │      0 │      2 │     12 │    124 ┃
┃ MD5 (memory)     │      1 │      1 │      1 │      4 │     31 │    311 ┃
┃ SHA-1 (memory)   │      0 │      1 │      1 │      1 │     16 │    107 ┃
┃ SHA-256 (memory) │      0 │      0 │      0 │      2 │     15 │    105 ┃
┗━━━━━━━━━━━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┷━━━━━━━━┛
                                           (elapsed time in milliseconds)
```



## String and Password Hashing

Venice supports PBKDF2, SHA-512, SHA-1, and MD5 for hashing strings.

**Note:** For hashing files use `crypt/file-hash` and `crypt/verify-file-hash`


### PBKDF2

PBKDF2 is the preferred hashing algorithm to hash passwords.

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


