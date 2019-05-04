# WebDAV

Venice supports WebDAV if the [Sardine](https://github.com/lookfirst/sardine) libs are on the runtime classpath:

 - com.github.lookfirst:sardine:5.8
 
transitive dependencies:
 
  - httpclient-4.5.2.jar
  - httpcore-4.4.4.jar
  - httpcore-nio-4.4.4.jar
  - httpmime-4.5.2.jar
  - commons-logging-1.2-api.jar
  - commons-logging-1.2.jar
 
```clojure
(do
  (load-module :webdav)

  (webdav/with {:username "jon.doe" :password "123456"}
     (let [url "http://0.0.0.0:8080/foo/webdav/document.doc" ]
          (do
             (webdav/exists? url)
             (webdav/list "http://0.0.0.0:8080/foo/webdav/" 1)
             (webdav/delete! "http://0.0.0.0:8080/foo/webdav/foo.doc")
             (webdav/get-as-file url "download.doc")
             (webdav/put-file! url "upload.doc" "application/msword")))))
```
