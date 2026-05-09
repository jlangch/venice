# Files

 

* [List Files](#list-files)
* [Retrieve File](#retrieve-file)
* [Delete File](#delete-file)
* [Create File](#create-file)

 
 

## List Files

Returns a list of files stored with OpenAI

```clojure
(do
  (load-module :openai-java)

  (let [client  (openai-java/client)]
    (->> (openai-java/list-files client)
         (docoll #(prn %)))))
```

Result:

```
{ :id "file-H7...B" 
  :filename "Tour_Eiffel_1.pdf" 
  :purpose user_data 
  :bytes 45787 
  :createdAt 1777820599 
  :expiresAt -1 
  :valid? true }
{ :id "file-By...K" 
  :filename "Tour_Eiffel_2.pdf" 
  :purpose user_data 
  :bytes 41239 
  :createdAt 1777820466 
  :expiresAt -1 
  :valid? true }
```

 

## Retrieve File

Returns information about a specific file.

```clojure
(do
  (load-module :openai-java)

  (let [client  (openai-java/client)]
    (openai-java/retrieve-file client "file-By...K")))
```

Result:

```
{ :id "file-By...K" 
  :filename "Eiffel-2.pdf" 
  :purpose user_data 
  :bytes 41239 
  :createdAt 1777820466 
  :expiresAt -1 
  :valid? true }
```

 

## Delete File

Delete a file.

```
(do
  (load-module :openai-java)

  (let [client  (openai-java/client)]
    (openai-java/delete-file client "file-H7...B")))
```

Result:

```
=> true
```

 

## Create File

Create a file

```
(do
  (load-module :openai-java)

  (let [client  (openai-java/client)
        file    (io/file "/Users/foo/Desktop/Tour_Eiffel.pdf")]
    (openai-java/create-file client file :USER_DATA 3600)))
```

Result:

```
{ :id "file-P7...w" 
  :filename "Tour_Eiffel.pdf" 
  :purpose user_data 
  :bytes 45787 
  :createdAt 1778048157 
  :expiresAt 1778051757 
  :valid? true }
```
