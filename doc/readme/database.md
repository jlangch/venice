# Database Tutorial (PostgreSQL)


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


## Start a PostgreSQL 16.2 Docker instance

Download and start the PostgreSQL container:

```clojure
(do
  (load-module :cargo-postgresql ['cargo-postgresql :as 'pg])
           
  (let [storage-dir  (io/file (repl/home-dir) "postgres-storage")]
    (when-not (io/exists-dir? storage-dir)
       (io/mk-dir storage-dir))
       
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


Show the database model (opens a browser):

```clojure
(do
  (load-module :chinook-postgresql ['chinook-postgresql :as 'chinook])
  
  (chinook/show-data-model))
```

List all tables:

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


Describe the 'album' table:

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


List the foreign key constraints in the database:

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
 
Top 3 best selling artists.
 
```clojure
(do
  (load-module :jdbc-core ['jdbc-core :as 'jdbc])
  (load-module :jdbc-postgresql ['jdbc-postgresql :as 'jdbp])
           
  (try-with [conn (jdbp/create-connection "localhost" 5432 
                                          "chinook_auto_increment" 
                                          "postgres" "postgres")]
    (-> (jdbc/execute-query 
            conn 
            """
            SELECT a.Name "Artist", sum(li.Unit_Price) "Total Sold" 
            FROM Invoice_Line li, Track t, Album al, Artist a
            WHERE li.Track_Id = t.Track_Id 
	          and al.Album_Id = t.Album_Id 
	          and a.Artist_Id = al.Artist_Id
            GROUP BY a.Name
            ORDER BY COUNT(a.Artist_Id) DESC
            LIMIT 3;
            """)
        (jdbc-core/print-query-result))))
```

```
Artist      Total Sold
----------- ----------
Iron Maiden 138.60    
U2          105.93    
Metallica   90.09     
```

       