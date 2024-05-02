# LangChain4J

* [LangChain](#langchain4j)
* [Qdrant Vector DB](#qdrant-vector-db)


## LangChain

*coming soon...*



## Qdrant Vector DB

Qdrant is a Vector Database and Vector Search Engine that empowers
LLM applications with Retrieval Augmented Generation (RAG) to access 
data outside the LLM data world.



### Start Qdrant Docker Container

Parameters:

| Parameter        | Description |
| :---             | :---        |
| cname            | A unique container name |
| version          | The Qdrant version to use. E.g.: 1.8.3 |
| mapped-rest-port | The published (mapped) Qdrant REST port on the  host. Defaults to 6333 |
| mapped-grpc-port | The published (mapped) Qdrant GRPC port on the host. Defaults to 6334 |
| storage-dir      | Directory where Qdrant persists all the data. |
| config-file      | An optional custom configuration yaml file |
| log              | A log function, may be *nil*. E.g: `(fn [s] (println "Qdrant:" s))`|


```clojure
(do
  (load-module :cargo-qdrant ['cargo-qdrant :as 'qdrant])
   
  ;; Run a Qdrant docker container labeled as "qdrant"
  (qdrant/start "qdrant" "1.8.3" "./qdrant-storage"))
```


### Stop Qdrant Docker Container

```clojure
(do
  (load-module :cargo-qdrant ['cargo-qdrant :as 'qdrant])
   
  ;; Stop the Qdrant docker container labeled as "qdrant"
  (qdrant/stop "qdrant"))
```



