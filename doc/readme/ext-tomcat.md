# Apache Tomcat

The Apache Tomcat extension module provides starting an embedded Tomcat server.

dependencies:

 - tomcat-embed-core-9.0.19.jar
 - javax.annotation-api-1.3.2.jar
 
 
From the REPL start Tomcat with the 'HelloWorld' servlet

```text
venice> (load-module :tomcat)
venice> (tc/run-tomcat (tc/hello-world-servlet) {:await? false})
```

Open a browser and with URL `http://localhost:8080`

Type `Ctrl-c` in the REPL to shutdown the server.
