# Database Example


In the following example we'll use the OpenAI chat completion API to answer questions
about a database. 

For simplicity the Chinook sample database will be used. See 
[Venice and Chinook Dataset](database.md#chinook-dataset-overview)

The OpenAI model shall be enabled to answer questions on the dataset like: *Who are the top 5 artists by number of tracks?*

Before starting, follow the [Venice Database Tutorial](database.md) to:

 1. Install the PostgreSQL JDBC driver
 2. Start a PostgreSQL Docker Instance
 3. Load the Chinook dataset into the PostgreSQL database
 
All these preliminary tasks can be run from a Venice REPL.


Then run the full example:

```clojure
(do
  (load-module :openai)
  (load-module :openai-demo)
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])


  ;; create a database connection
  (defn db-connection []
    (jdbp/create-connection "localhost" 5432 
                            "chinook_auto_increment" 
                            "postgres" "postgres"))


  ;; get the database schema (formatted text for OpenAI) in the format
  ;;   Table: table1
  ;;   Columns: col1, col2, col3, ...
  ;;   Table: table2
  ;;   Columns: col1, col2, col3, ...
  ;;   ...
  (defn db-schema [conn]
    (->> (jdbc/tables-with-columns conn)
         (map (fn [[t c]] 
                (str "Table: " t "\nColumns: " (str/join ", " c)))) 
         (str/join "\n")))
  
  
  ;; create the OpenAI API 'tools' function definition for "ask_database"
  (defn function-defs [database-schema]
    [ { :type "function"
        :function {
          :name "ask_database"
          :description """
                       Use this function to answer user questions about music. 
                       Input should be a fully formed SQL query.
                       """
          :parameters {
            :type "object"
            :properties {
              "query" {
                :type "string"
                :description  """
                              SQL query extracting info to answer the user's question.
                              
                              SQL should be written using this database schema:
                              ~{database-schema}
                              
                              The query should be returned in plain text, not in JSON.
                              """
              }
            }
            :required ["query"]
          }
        }
      } ] )


  ;; query the database with a provided SQL.
  ;;   conn:       a JDBC database connection
  ;;   named-args: a map e.g.: {"query" "SELECT * FROM Foo" }
  (defn ask-database [conn named-args]
    (println "Calling function 'ask-database'")
    (try-with [query (get named-args "query")
               stmt  (jdbc/create-statement conn)]
      (println "DB Query:" query)
      (let [result (jdbc/execute-query stmt query)
            rows   (:rows result)]       
        (json/write-str rows))  ;; return the rows as a JSON string
      (catch :Exception e
             ;; return the error as a JSON string
             (json/write-str { "query" query
                               "error" "Query failed with error: ~(ex-message e)" }))))


  ;; Ask the model
  ;; Note: for simplicity this example just handles the happy path!
  
  ;; Phase 1: Initial question to the model
  (try-with [conn (db-connection)]
    (let [model       "gpt-4"
          prompt      [ { :role     "system"
                          :content  """
                                    Answer user questions by generating SQL queries against 
                                    the Chinook Music Database.
                                    """ }
                        { :role     "user"
                          :content  "Hi, who are the top 5 artists by number of tracks?" } ]
          fn-defs     (function-defs (db-schema conn))
          response    (openai/chat-completion prompt 
                                              :model model
                                              :tools fn-defs
                                              :chat-opts { :temperature 0.1 })] 
      (openai/assert-response-http-ok response)
      
      ;; Phase 2: model requests to call the function "ask_database"
      (let [response (:data response)]
        ;;(println "Message:" (->> (openai/chat-extract-response-message response)
        ;;                         (openai/pretty-print-json message)))

        (assert (openai/chat-finish-reason-tool-calls?  response))
        
        ;; call the function "ask_database"
        (let [fn-map     { "ask_database" (partial ask-database conn) }
              fn-result  (first (openai/exec-fn response fn-map))
              answer     (:ok fn-result)
              err        (:err fn-result)]
          (when err (throw err))  ;; "ask_database" failed
          (println "Fn call result:" (pr-str answer))
          
          ;; Phase 3: Ask the model again with the queried music data obtained
          ;;            from the function "ask_database"
          (let [prompt-fn  { :role     "function"
                             :name     (openai/chat-extract-function-name response)
                             :content  answer }
                response   (openai/chat-completion (conj prompt prompt-fn) 
                                                   :model model
                                                   :chat-opts { :temperature 0.1 })]
            (openai/assert-response-http-ok response)
            (let [response (:data response)
                  content  (openai/chat-extract-response-message-content response)]
              (assert (openai/chat-finish-reason-stop?  response))
              (println)
              (println "Question: \"Hi, who are the top 5 artists by number of tracks?\"")
              (println)
              (println content))))))))
```

The model answers the question "Hi, who are the top 5 artists by number of tracks?" 
with:

```
The top 5 artists by number of tracks are:

1. Iron Maiden with 213 tracks
2. U2 with 135 tracks
3. Led Zeppelin with 114 tracks
4. Metallica with 112 tracks
5. Deep Purple with 92 tracks
```
