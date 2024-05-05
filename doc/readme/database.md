# Database Tutorial (PostgreSQL)

The Venice database modules are based on the [Java Database Connectivity (JDBC) API](https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/). 

Basically Venice can handle any SQL database for which a JDBC driver is available.

* [Install the PostgreSQL JDBC driver](#install-the-postgresql-jdbc-driver)
* [Start a PostgreSQL Docker Instance](#start-a-postgresql-docker-instance)
* [Load the Chinook Dataset](#load-the-chinook-dataset)
* [Chinook Dataset Overview](#chinook-dataset-overview)
* [Queries](#queries)
* [Updates](#updates)
* [Create / Drop Tables](#create-and-drop-tables)
* [Transactions](#transactions)



## Install the PostgreSQL JDBC driver


```clojure
(do
  (load-module :postgresql-jdbc-install)
  
  (postgresql-jdbc-install/install :dir (repl/libs-dir) :silent false))
```

Restart the REPL to reflect the classpath change

```
venice> !restart
```


## Start a PostgreSQL Docker Instance

Download and start the PostgreSQL container:

```clojure
(do
  (load-module :cargo-postgresql ['cargo-postgresql :as 'pg])
           
  (let [storage-dir  (io/file (repl/home-dir) "postgres-storage")]
    (when-not (io/exists-dir? storage-dir)
       (io/mkdir storage-dir))
       
    ;; Run a PostgreSQL container labeled as "postgres"
    ;; username: postgres
    ;; password: postgres
    (pg/start "postgres" "16.2" storage-dir "postgres" "postgres")))
```

*If the Docker image for PostgreSQL 16.2 is already downloaded `pg/start` will just start the container otherwise it will download the image first.*


To stop the PostgreSQL container:

```clojure
(do
  (load-module :cargo-postgresql ['cargo-postgresql :as 'pg])
           
  (pg/stop "postgres"))
```



## Load the Chinook dataset 

Loads the Chinook data into the PostgreSQL database "chinook_auto_increment".

*Chinook is sample database for a digital media store that can be used to explore and learn database commands.*

If the database exists already it will be dropped, then recreated and the data 
loaded.

```clojure
(do
  (load-module :chinook-postgresql ['chinook-postgresql :as 'chinook])
  
  (chinook/load-data "localhost" 5432 "postgres" "postgres"))
```

The Chinook data set is provided by [Luis Rocha](https://github.com/lerocha/chinook-database)



## Chinook dataset overview


**Show the database model (opens a browser):**

```clojure
(do
  (load-module :chinook-postgresql ['chinook-postgresql :as 'chinook])
  
  (chinook/show-data-model))
```

**List all tables:**

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
   
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
    (jdbc/tables conn)))
```

```
["album" "artist" "customer" "employee" "genre"  "invoice" "invoice_line" 
 "media_type" "playlist" "playlist_track" "track"]
```


**Describe the 'album' table:**

```clojure
(do
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
   
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
    (jdbp/describe-table conn "album")))
```

```
column_name data_type         character_maximum_length is_nullable column_default
----------- ----------------- ------------------------ ----------- --------------
album_id    integer           <null>                   NO          <null>        
artist_id   integer           <null>                   NO          <null>        
title       character varying 160                      NO          <null>        
```


**List the foreign key constraints in the database:**

```clojure
(do
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
    (jdbp/foreign-key-constraints conn)))
```

```
table_name     foreign_key                     pg_get_constraintdef                                            
-------------- ------------------------------- ----------------------------------------------------------------
album          album_artist_id_fkey            FOREIGN KEY (artist_id) REFERENCES artist(artist_id)            
customer       customer_support_rep_id_fkey    FOREIGN KEY (support_rep_id) REFERENCES employee(employee_id)   
employee       employee_reports_to_fkey        FOREIGN KEY (reports_to) REFERENCES employee(employee_id)       
invoice        invoice_customer_id_fkey        FOREIGN KEY (customer_id) REFERENCES customer(customer_id)      
invoice_line   invoice_line_invoice_id_fkey    FOREIGN KEY (invoice_id) REFERENCES invoice(invoice_id)         
invoice_line   invoice_line_track_id_fkey      FOREIGN KEY (track_id) REFERENCES track(track_id)               
playlist_track playlist_track_playlist_id_fkey FOREIGN KEY (playlist_id) REFERENCES playlist(playlist_id)      
playlist_track playlist_track_track_id_fkey    FOREIGN KEY (track_id) REFERENCES track(track_id)               
track          track_album_id_fkey             FOREIGN KEY (album_id) REFERENCES album(album_id)               
track          track_genre_id_fkey             FOREIGN KEY (genre_id) REFERENCES genre(genre_id)               
track          track_media_type_id_fkey        FOREIGN KEY (media_type_id) REFERENCES media_type(media_type_id)
```


   
## Queries

**Show first 10 albums from the album table:**

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")
             stmt (jdbc/create-statement conn)]
    (-> (jdbc/execute-query stmt "SELECT * FROM Album LIMIT 10")
        (jdbc/print-query-result))))
```

```
album_id title                                 artist_id
-------- ------------------------------------- ---------
1        For Those About To Rock We Salute You 1        
2        Balls to the Wall                     2        
3        Restless and Wild                     2        
4        Let There Be Rock                     1        
5        Big Ones                              3        
6        Jagged Little Pill                    4        
7        Facelift                              5        
8        Warner 25 Anos                        6        
9        Plays Metallica By Four Cellos        7        
10       Audioslave                            8        
```


**List the number of albums**

```
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
            
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
   (println "Albums:" (jdbc/count-rows conn "Album"))))
```

```
Albums: 356
```


**List the Led Zeppelin albums:**

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
  
  (defn find-led-zeppelin [conn]
    (try-with [stmt (jdbc/create-statement conn)]
      (-> (jdbc/execute-query stmt "SELECT * FROM Artist a WHERE a.Name = 'Led Zeppelin'")
          (:rows)
          (first))))
  
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")
             sql  """
                  SELECT a.Name "Artist", al.Title "Title"	   
                  FROM Artist a
                  JOIN Album al ON al.Artist_Id = a.Artist_Id
                  WHERE a.Name = 'Led Zeppelin' 
                  """ 
             stmt (jdbc/create-statement conn)]
      (-> (jdbc/execute-query stmt sql)
          (jdbc-core/print-query-result))))
```

```
Artist       Title                             
------------ ----------------------------------
Led Zeppelin BBC Sessions [Disc 1] [Live]      
Led Zeppelin Physical Graffiti [Disc 1]        
Led Zeppelin BBC Sessions [Disc 2] [Live]      
Led Zeppelin Coda                              
Led Zeppelin Houses Of The Holy                
Led Zeppelin In Through The Out Door           
Led Zeppelin IV                                
Led Zeppelin Led Zeppelin I                    
Led Zeppelin Led Zeppelin II                   
Led Zeppelin Led Zeppelin III                  
Led Zeppelin Physical Graffiti [Disc 2]        
Led Zeppelin Presence                          
Led Zeppelin The Song Remains The Same (Disc 1)
Led Zeppelin The Song Remains The Same (Disc 2)
```



**Top 5 artists by number of tracks:**

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")
             stmt (jdbc/create-statement conn)]
    (-> (jdbc/execute-query 
            stmt 
            """
            SELECT Artist.Name, COUNT(Track.Track_Id) AS TrackCount 
            FROM Artist 
            JOIN Album ON Artist.Artist_Id = Album.Artist_Id 
            JOIN Track ON Album.Album_Id = Track.Album_Id 
            GROUP BY Artist.Artist_Id 
            ORDER BY TrackCount DESC LIMIT 5;
            """)
        (jdbc-core/print-query-result))))
```

```
name         trackcount
------------ ----------
Iron Maiden  213       
U2           135       
Led Zeppelin 114       
Metallica    112       
Lost         92        
```


 
**Top 3 best selling artists:**
 
```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")
             stmt (jdbc/create-statement conn)]
    (-> (jdbc/execute-query 
            stmt 
            """
            SELECT a.Name "Artist", sum(il.Unit_Price) "Total Sold" 	   
            FROM Artist a
            JOIN Album al ON al.Artist_Id = a.Artist_Id
            JOIN Track t ON t.Album_Id = al.Album_Id
            JOIN Invoice_Line il ON t.track_Id = il.Invoice_Line_Id 	       
            GROUP BY a.Name
            ORDER BY "Total Sold" DESC
            LIMIT 3;
            """)
        (jdbc-core/print-query-result))))
```

```
Artist       Total Sold
------------ ----------
Iron Maiden  210.87    
Led Zeppelin 130.86    
Metallica    110.88    
```

   
## Updates

**Add new album for artist "Led Zeppelin":**

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
  
  (defn find-led-zeppelin [conn]
    (try-with [stmt (jdbc/create-statement conn)]
      (-> (jdbc/execute-query stmt "SELECT * FROM Artist a WHERE a.Name = 'Led Zeppelin'")
          (:rows)
          (first))))  
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
    (let [led-zeppelin (find-led-zeppelin conn)
          artist-id    (first led-zeppelin)
          sql          """
                       INSERT INTO Album (Title,Artist_Id) 
                       VALUES('How the West Was Won',~(str artist-id))
                       """]
      (try-with [stmt (jdbc/create-statement conn)]
        (jdbc/execute-update stmt sql)))))
```


**Return generated keys (variant 1):**

Using: `(jdbc/execute-update stmt sql :gen-key ["album_id"])`


```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
  
  (defn find-led-zeppelin [conn]
    (try-with [stmt (jdbc/create-statement conn)]
      (-> (jdbc/execute-query stmt "SELECT * FROM Artist a WHERE a.Name = 'Led Zeppelin'")
          (:rows)
          (first))))  
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
    (let [led-zeppelin (find-led-zeppelin conn)
          artist-id    (first led-zeppelin)
          sql          """
                       INSERT INTO Album (Title,Artist_Id) 
                       VALUES('How the West Was Won',~(str artist-id))
                       """]
      (try-with [stmt (jdbc/create-statement conn)]
        (jdbc/execute-update stmt sql :gen-key ["album_id"])
        
        ;; generated keys
        (println "Generated keys: \n")
        (->> (jdbc/generated-keys stmt)
             (jdbc-core/print-query-result))))))
```

```
Generated keys: 

album_id
--------
364     
```

**Return generated keys (variant 2):**

Using: `(jdbc/execute-update stmt sql :gen-key true)`


```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
  
  (defn find-led-zeppelin [conn]
    (try-with [stmt (jdbc/create-statement conn)]
      (-> (jdbc/execute-query stmt "SELECT * FROM Artist a WHERE a.Name = 'Led Zeppelin'")
          (:rows)
          (first))))  
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
    (let [led-zeppelin (find-led-zeppelin conn)
          artist-id    (first led-zeppelin)
          sql          """
                       INSERT INTO Album (Title,Artist_Id) 
                       VALUES('How the West Was Won',~(str artist-id))
                       """]
      (try-with [stmt (jdbc/create-statement conn)]
        (jdbc/execute-update stmt sql :gen-key true) 
        
        ;; generated keys
        (println "Generated keys: \n")
        (->> (jdbc/generated-keys stmt)
             (jdbc-core/print-query-result))))))
```

```
Generated keys: 

album_id title                artist_id
-------- -------------------- ---------
365      How the West Was Won 22       
```


## Prepared Statements


**Find albums by artist "Led Zeppelin":**

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")
             sql  """
                  SELECT a.Name "Artist", al.Title "Title"	   
                  FROM Artist a
                  JOIN Album al ON al.Artist_Id = a.Artist_Id
                  WHERE a.Name = ? 
                  """ 
             stmt (jdbc/prepare-statement conn sql)]
    (jdbc/ps-string stmt 1 "Led Zeppelin")
    (-> (jdbc/execute-query  stmt)
        (jdbc-core/print-query-result))))
```

```
Artist       Title                             
------------ ----------------------------------
Led Zeppelin BBC Sessions [Disc 1] [Live]      
Led Zeppelin Physical Graffiti [Disc 1]        
Led Zeppelin BBC Sessions [Disc 2] [Live]      
Led Zeppelin Coda                              
Led Zeppelin Houses Of The Holy                
Led Zeppelin In Through The Out Door           
Led Zeppelin IV                                
Led Zeppelin Led Zeppelin I                    
Led Zeppelin Led Zeppelin II                   
Led Zeppelin Led Zeppelin III                  
Led Zeppelin Physical Graffiti [Disc 2]        
Led Zeppelin Presence                          
Led Zeppelin The Song Remains The Same (Disc 1)
Led Zeppelin The Song Remains The Same (Disc 2)
```

**Add new album for artist "Led Zeppelin":**

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
  
  (defn find-led-zeppelin [conn]
    (try-with [stmt (jdbc/create-statement conn)]
      (-> (jdbc/execute-query stmt "SELECT * FROM Artist a WHERE a.Name = 'Led Zeppelin'")
          (:rows)
          (first))))
  
  (defn list-led-zeppelin-albums [conn]
    (try-with [sql  """
                    SELECT a.Name "Artist", al.Title "Title"	   
                    FROM Artist a
                    JOIN Album al ON al.Artist_Id = a.Artist_Id
                    WHERE a.Name = 'Led Zeppelin' 
                    """ 
               stmt (jdbc/create-statement conn)]
      (-> (jdbc/execute-query stmt sql)
          (jdbc-core/print-query-result))))
  
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
    (let [led-zeppelin (find-led-zeppelin conn)
          artist-id    (first led-zeppelin)
          sql          "INSERT INTO Album (Title,Artist_Id) VALUES(?,?)"]
      (try-with [stmt (jdbc/prepare-statement conn sql)]
        (jdbc/ps-string stmt 1 "How the West Was Won")
        (jdbc/ps-int stmt 2 artist-id)
        (jdbc/execute-update stmt))
       
      ;; list Led Zeppelin albums
      (list-led-zeppelin-albums conn))))
```

**Return the generated keys (variant 1):**

Using: `(jdbc/prepare-statement conn sql :gen-key ["album_id"])`


```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
  
  (defn find-led-zeppelin [conn]
    (try-with [stmt (jdbc/create-statement conn)]
      (-> (jdbc/execute-query stmt "SELECT * FROM Artist a WHERE a.Name = 'Led Zeppelin'")
          (:rows)
          (first))))  
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
    (let [led-zeppelin (find-led-zeppelin conn)
          artist-id    (first led-zeppelin)
          sql          "INSERT INTO Album (Title,Artist_Id) VALUES(?,?)"]
      (try-with [stmt (jdbc/prepare-statement conn sql :gen-key ["album_id"])]
        (jdbc/ps-string stmt 1 "How the West Was Won")
        (jdbc/ps-int stmt 2 artist-id)
        (jdbc/execute-update stmt)
        
        ;; generated keys
        (println "Generated keys: \n")
        (->> (jdbc/generated-keys stmt)
             (jdbc-core/print-query-result))))))
```

```
Generated keys: 

album_id
--------
366     
```


**Return the generated keys (variant 2):**

Using: `(jdbc/prepare-statement conn sql :gen-key true)`


```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
  
  (defn find-led-zeppelin [conn]
    (try-with [stmt (jdbc/create-statement conn)]
      (-> (jdbc/execute-query stmt "SELECT * FROM Artist a WHERE a.Name = 'Led Zeppelin'")
          (:rows)
          (first))))  
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
    (let [led-zeppelin (find-led-zeppelin conn)
          artist-id    (first led-zeppelin)
          sql          "INSERT INTO Album (Title,Artist_Id) VALUES(?,?)"]
      (try-with [stmt (jdbc/prepare-statement conn sql :gen-key true)]
        (jdbc/ps-string stmt 1 "How the West Was Won")
        (jdbc/ps-int stmt 2 artist-id)
        (jdbc/execute-update stmt)
        
        ;; generated keys
        (println "Generated keys: \n")
        (->> (jdbc/generated-keys stmt)
             (jdbc-core/print-query-result))))))
```

```
Generated keys: 

album_id title                artist_id
-------- -------------------- ---------
367      How the West Was Won 22       
```


## Create and Drop Tables

**Create "Accounts" Table**

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])

  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")                                         
             stmt (jdbc/create-statement conn)]
    (jdbc/execute stmt 
                  """
                  CREATE TABLE IF NOT EXISTS Accounts (
                    User_Id SERIAL PRIMARY KEY, 
                    Username VARCHAR (50) UNIQUE NOT NULL, 
                    Password VARCHAR (50) NOT NULL, 
                    Email VARCHAR (255) UNIQUE NOT NULL, 
                    Created_At TIMESTAMP NOT NULL, 
                    Last_Login TIMESTAMP); 
                  """ )))
```

Add a new account:

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
 
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]     
    (let [sql  """
               INSERT INTO Accounts (Username, Password, Email, Created_At, Last_Login) 
               VALUES(?,?,?,?,?);
               """]
      (try-with [stmt (jdbc/prepare-statement conn sql)]
        (jdbc/ps-string stmt 1 "John Doe")
        (jdbc/ps-string stmt 2 "42")
        (jdbc/ps-string stmt 3 "john.doe@foo.org")
        (jdbc/ps-timestamp stmt 4 (time/local-date-time))
        (jdbc/ps-timestamp stmt 5 (time/local-date-time))
        
        (jdbc/execute-update stmt)))))     
```


**Drop**

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])

  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")                                         
             stmt (jdbc/create-statement conn)]
    (jdbc/execute stmt "DROP TABLE IF EXISTS Accounts;")))
```


## Transactions

**Check TX isolation level:**

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])

  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")                                         
             stmt (jdbc/create-statement conn)]
    (println "TX isolation level:" (jdbc/tx-isolation conn))))
```

```
TX isolation level: :tx-read-commited
```

**Set TX isolation level to `:tx-repeatable-read`**

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])

  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")                                         
             stmt (jdbc/create-statement conn)]
    (jdbc/tx-isolation! conn :tx-repeatable-read)
    (println "TX isolation level:" (jdbc/tx-isolation conn))))
```

```
TX isolation level: :tx-repeatable-read
```

       
**Commit/Rollback (the hard way):**

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
  
  (defn find-led-zeppelin [conn]
    (try-with [stmt (jdbc/create-statement conn)]
      (-> (jdbc/execute-query stmt "SELECT * FROM Artist a WHERE a.Name = 'Led Zeppelin'")
          (:rows)
          (first))))  
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
      
    (println "Albums:" (jdbc/count-rows conn "Album"))

    ;; - transactional ----------------------------------------------------
    (try
      (jdbc/auto-commit! conn :off)       ;; switch to explicit transaction
      
      (let [led-zeppelin (find-led-zeppelin conn)
            artist-id    (first led-zeppelin)
            sql          """
                         INSERT INTO Album (Title,Artist_Id) 
                         VALUES('How the West Was Won',~(str artist-id))
                         """]
        (try-with [stmt (jdbc/create-statement conn)]
          (jdbc/execute-update stmt sql)))
          
        (jdbc/commit! conn)                           ;; commit transaction
      (catch :Exception e
         (jdbc/rollback! conn)                ;; rollback in exception case
         (throw e))
      (finally
        (jdbc/auto-commit! conn :on)))         ;; restore auto transactions
    ;; - transactional ----------------------------------------------------
        
    (println "Albums:" (jdbc/count-rows conn "Album"))))
```
   
   
**Commit/Rollback with a TX template:**

The TX template greatly reduces the boiler plate code with JDBC transaction handling.

The work sequence of a TX template:
  * Switches to the commit mode to explicit transactions
  * Runs the forms within a JDBC transaction
  * Commits the transaction at the end of the forms 
  * Or rolls the transaction back if the forms threw an exception.
  * Restores the original JDBC commit mode
  * On commit returns the value of the last form executed
  * On rollback throws a :com.github.jlangch.venice.TransactionException
  

```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
  
  (defn find-led-zeppelin [conn]
    (try-with [stmt (jdbc/create-statement conn)]
      (-> (jdbc/execute-query stmt "SELECT * FROM Artist a WHERE a.Name = 'Led Zeppelin'")
          (:rows)
          (first))))  
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
    (println "Albums:" (jdbc/count-rows conn "Album"))
      
    ;; - transactional ----------------------------------------------------
    (jdbc/with-tx conn
      (let [led-zeppelin (find-led-zeppelin conn)
            artist-id    (first led-zeppelin)
            sql          """
                         INSERT INTO Album (Title,Artist_Id) 
                         VALUES('How the West Was Won',~(str artist-id))
                         """]
        (try-with [stmt (jdbc/create-statement conn)]
          (jdbc/execute-update stmt sql))))
    ;; - transactional ----------------------------------------------------
          
    (println "Albums:" (jdbc/count-rows conn "Album"))))
```
    