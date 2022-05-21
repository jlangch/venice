# Configuration

The configuration module manages application configuration using JSON files, 
environment variables and Java system properties. 

It represents an application configuration as a single map which is read once 
on startup.

Reads configuration from multiple sources and recursively merges it:

* JSON files in classpath – shipped with JAR, useful for default options
* JSON files in filesystem
* Environment variables
* Java properties
* Override parameter – useful for overriding options for test systems



## Examples

```
(do
  (load-module :config ['config :as 'c])
  
  (def cfg (config/build 
              (c/resource "config-defaults.json" :key-fn keyword)
              (c/file "./config-local.json" :key-fn keyword)
              (c/env-var "SERVER_PORT" [:http :port])
              (c/env-var "SERVER_THREADS" [:http :threads])
              (c/property-var "MASTER_PWD" [:app :master-pwd]))))
```

**JSON Files**

Reads a JSON file into a configuration map.

```
(do
  (load-module :config ['config :as 'c])

  (def cfg-json """
                { "db" : {
                    "classname" : "com.mysql.jdbc.Driver",
                    "subprotocol" : "mysql",
                    "subname" : "//127.0.0.1:3306/test",
                    "user" : "test",
                    "password" : "123"
                  }
                }
                """)
    
  (def cfg (let [json (io/buffered-reader cfg-json)]
             (config/build
               (c/file json :key-fn keyword)))) 
            
  (println "Driver class:" (-> cfg :db :classname))   
  (println "Password:    " (-> cfg :db :password)))
```

*Note*: The functions `config/resource` and `config/file` accept an optional translating 
function for keys in the map. Mostly you may want to use `keyword` to turn strings 
to keywords.


**Environment Variables**

Reads configuration values from environment variables and associates them with the 
specified paths in the configuration map

```
(do
  (load-module :config ['config :as 'c])
  
  (def cfg (config/build
             (config/env-var "JAVA_HOME" [:java-home])
             (config/env-var "TERM_PROGRAM" [:term :prog])
             (config/env-var "TERM" [:term :name])
             (config/env-var "SERVER_PORT" [:http :port])))
             
  (println "Java home:" (-> cfg :java-home))    ; => /Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home
  (println "Term prog:" (-> cfg :term :prog))   ; => Apple_Terminal
  (println "Term name:" (-> cfg :term :name))   ; => xterm-256color
  (println "Http port:" (-> cfg :http :port)))  ; => nil
```

Specifying default values:

```
(do
  (load-module :config ['config :as 'c])
  
  (def cfg (config/build
             (config/env-var "SERVER_PORT" [:http :port] "8080")))
  
  (println "Http port:" (-> cfg :http :port)))  ; => "8080"
```

**Java Properties**

Reads configuration values from system properties and associates them with the 
specified paths in the configuration map

```
(do
  (load-module :config ['config :as 'c])
  
  (def cfg (config/build
             (config/property-var "java.vendor" [:java :vendor])
             (config/property-var "java.version" [:java :version])
             (config/property-var "SERVER_PORT" [:http :port])))
             
  (println "Java vendor: " (-> cfg :java :vendor))    ; => Temurin
  (println "Java version:" (-> cfg :java :version))   ; => 1.8.0_322
  (println "Http port:   " (-> cfg :http :port)))     ; => nil
```

Specifying default values:

```
(do
  (load-module :config ['config :as 'c])
  
  (def cfg (config/build
             (config/property-var "SERVER_PORT" [:http :port] "8080")))
  
  (println "Http port:"    (-> cfg :http :port)))  ; => "8080"
```
