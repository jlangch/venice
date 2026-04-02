# Advanced REPL 


## Macro Expansion

Expanding macros ahead of evaluation can speed up the execution of a script by 
a factor of 3 to 10.

Upfront macro expansion can be activated in the REPL by the `!macroexpand` command:

``` text
venice> !macroexpand
```

The upfront macro expansion is applied to typed scripts and files loaded from 
filesystem or classpath.


An example (on an MacBook Air M2):

``` text
venice> (time (reduce + (map (fn [x] (cond (< x 0) -1 (> x 0) 1 :else 0)) 
                             (range -10000 10001))))
Elapsed time: 110.38ms
=> 0
```
     
``` text
venice> !macroexpand
venice> (time (reduce + (map (fn [x] (cond (< x 0) -1 (> x 0) 1 :else 0)) 
                             (range -10000 10001))))
Elapsed time: 12.14ms
=> 0
```

Check if macro expansion is enabled:

``` text
venice> (macroexpand-on-load?)
=> true
```


## Drag and Drop Venice files into the REPL for execution

Drag and drop a Venice file into the REPL and press [RETURN] to execute it:

``` text
venice> /Users/foo/test.venice
```

This is identical to run `(load-file "/Users/foo/test.venice")`.


## Function documentation

Print the documentation for a Venice function

``` text
venice> (doc count)
(count coll)

Returns the number of items in the collection. (count nil) returns 0. Also works
on strings, and Java Collections

EXAMPLES:
   (count {:a 1 :b 2})

   (count [1 2])

   (count "abc")
```


E.g.: Find the Venice cryptography PBKDF2 hash function and print the doc for it:

``` text
venice> (load-module :crypt)

venice> (finder "crypt*")
crypt/add-bouncy-castle-provider                  :core/function
crypt/ciphers                                     :core/function
crypt/encryptor-aes-256-cbc                       :core/function
crypt/encryptor-aes-256-cbc-supported?            :core/function
crypt/encryptor-aes-256-gcm                       :core/function
crypt/encryptor-aes-256-gcm-supported?            :core/function
crypt/encryptor-chacha20                          :core/function
crypt/encryptor-chacha20-bouncycastle             :core/function
crypt/encryptor-chacha20-bouncycastle-supported?  :core/function
crypt/encryptor-chacha20-supported?               :core/function
crypt/hash-file                                   :core/function
crypt/max-key-size                                :core/function
crypt/md5-hash                                    :core/function
crypt/pbkdf2-hash                                 :core/function
crypt/provider?                                   :core/function
crypt/sha1-hash                                   :core/function
crypt/sha512-hash                                 :core/function
crypt/verify-file-hash                            :core/function

venice> (doc crypt/pbkdf2-hash)
```


## Code Completion

The REPL supports code completion. Completion is triggered by the `TAB` key.


### Code completion for functions

``` text
venice> (regex<TAB>
regex/find              regex/matcher           regex/find-group
regex/find?             regex/matches           regex/groupcount
regex/group             regex/pattern           regex/find-all-groups
regex/reset             regex/matches?
```

Cycle through the candidates with the `TAB`, `←`, `↑`, `→`, `↓` keys or narrow 
the candidates by typing more characters. `Ctrl-C` stops the completion.


### Code completion for loading a module

``` text
venice> (load-module <TAB>
:kira     :math     :ring     :maven    :tomcat   :webdav   :xchart
```

The namespace alias for a module can be completed with a single char default:

``` text
venice> (load-module :grep <TAB>
```

auto completes to

``` text
venice> (load-module :grep ['grep :as 'g])
```
 

### Code completion for loading a Venice file

``` text
venice> (load-file "<TAB>
chart.venice             exception.venice         perf-test-1.venice
indent.venice            parsatron.venice         perf-test-2.venice
script.venice            chart-swing.venice       login-webapp.venice
webdav.venice            demo-webapp.venice       vaadin-download.venice
```


### Code completion for doc function

``` text
venice> (doc li<TAB>
list        list*       list?       list-comp
```


## Adding 3rdParty JARs to the REPL

3rdParty JARs can be manually copied to the REPL's library path `libs`. 

``` text
REPL_HOME
├── libs
│   ├── venice-1.12.87.jar
│   ├── jansi-2.4.1.jar
│   ├── xchart-3.8.8.jar    (added jar)
│   └── repl.json
├── tools
│   └── apache-maven-3.9.6
│       └── ...
├── tmp
│   └── ...
├── scripts
│   └── ... (example scripts)
├── repl.env
├── repl.sh
└── run-script.sh
```

Just restart the REPL after adding the libraries by running the REPL `!restart` 
command:

``` text
venice> !restart
```

To check the new REPL classpath run the REPL `!classpath` command:

``` text
venice> !classpath
REPL classpath:
  libs
  libs/jansi-2.4.1.jar
  libs/venice-1.12.87.jar
  libs/xchart-3.8.8.jar
```


## Reload Venice context

Reload the Venice context without restarting the REPL

``` text
venice> !reload
```


## Sandbox with the REPL

The Venice sandbox can be managed from within the REPL: [managing the sandbox](repl-sandbox.md)

