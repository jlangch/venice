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
            
  (println "Driver class:" (get-in cfg [:db :classname]))   
  (println "Password:    " (get-in cfg [:db :password])))
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
  
  (def cfg (c/build
             (c/env-var "JAVA_HOME" [:java-home])
             (c/env-var "TERM_PROGRAM" [:term :prog])
             (c/env-var "TERM" [:term :name])
             (c/env-var "SERVER_PORT" [:http :port])))
             
  (println "Java home:" (get cfg :java-home))         ; => /Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home
  (println "Term prog:" (get-in cfg [:term :prog]))   ; => Apple_Terminal
  (println "Term name:" (get-in cfg [:term :name]))   ; => xterm-256color
  (println "Http port:" (get-in cfg [:http :port])))  ; => nil
```

Specifying default values:

```
(do
  (load-module :config ['config :as 'c])
  
  (def cfg (c/build
             (c/env-var "SERVER_PORT" [:http :port] "8080")))
  
  (println "Http port:" (get-in cfg [:http :port])))  ; => "8080"
```

**Java Properties**

Reads configuration values from system properties and associates them with the 
specified paths in the configuration map

```
(do
  (load-module :config ['config :as 'c])
  
  (def cfg (c/build
             (c/property-var "java.vendor" [:java :vendor])
             (c/property-var "java.version" [:java :version])
             (c/property-var "SERVER_PORT" [:http :port])))
             
  (println "Java vendor: " (get-in cfg [:java :vendor]))    ; => Temurin
  (println "Java version:" (get-in cfg [:java :version]))   ; => 1.8.0_322
  (println "Http port:   " (get-in cfg [:http :port])))     ; => nil
```

Specifying default values:

```
(do
  (load-module :config ['config :as 'c])
  
  (def cfg (c/build
             (c/property-var "SERVER_PORT" [:http :port] "8080")))
  
  (println "Http port:"    (get-in cfg [:http :port])))  ; => "8080"
```

## Using configurations with the component module

```
(do
  (load-module :config ['config :as 'cfg])
  (load-module :component ['component :as 'cmp])

  ;; define the server component
  (deftype :server [components :map]
     cmp/Component
       (start [this]
          (let [port (get-in this [:components :config :server :port])]
            (println (id this) "started at port " port)
            this))
       (stop [this]
          (println (id this) "stopped")
          this)
       (inject [this deps]
          (assoc this :components deps)))

  ;; note that the configuration is a plain vanilla Venice map and does not
  ;; implement the protocol 'Component'
  (defn create-system []
    (-> (cmp/system-map
           "test"
           :config (cfg/build
                     (cfg/env-var "SERVER_PORT" [:server :port] "8800"))
           :server (server. {}))
        (cmp/system-using
           {:server [:config]})))

  (defn- id [this]
    (get-in this [:components :component-info :id]))

  (-> (create-system)
      (cmp/start)
      (cmp/stop))
      
  nil)
```

prints

```
:server started at port 8800
:server stopped
```
