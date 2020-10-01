# Shell Scripts

Venice allows you to write shell scripts without a hassle. The 'io' functions
are pretty powerful when dealing with files. You're not going to miss Unix tools
like 'awk, 'sed'.

The REPL let's you interactively run and test commands.

## Noticeable Functions 

### System functions

- os−type, os−type? 
- os−arch, os−name, os−version
- system-env
- current−time−millis
- sleep 
- host−name, host−address


### File I/O

### Web download

### Executing shell commands 

The `sh` function is the swiss army knife to deal with the native processes of the 
underlying operating system (see the Venice _cheatsheet_ for details).

```clojure
(sh "kill" "-9" 56789 :throw-ex true)

;; printing 
(println (sh "ls" "-l"))
(sh "ls" "-l" :out-fn println :err-fn println)

;; run background process
(println (sh "/bin/sh" "-c" "sleep 30 >/dev/null 2>&1 &")) 
(println (sh "/bin/sh" "-c" "nohup sleep 30 >/dev/null 2>&1 &"))

;; working directory
(println (with-sh-dir "/tmp" (sh "ls" "-l") (sh "pwd")))
(println (sh "pwd" :dir "/tmp"))

;; list files in a directory with a glob pattern
(->> (io/list-files-glob "." "*.png")
     (docoll println))
```


### Shell Extension Module

The [Shell Extension Module ](ext-shell.md) offers OS agnostic functions to manage processes or 
open files with the platform specific application.



## Example

A larger example that zips Tomcat log files

```clojure
;; -------------------------------------------------------------------------------
;; Zips the last month's Tomcat log files
;;
;; > java -jar venice-1.9.0.jar -file zip-tomcat-logs.venice ./logs
;; -------------------------------------------------------------------------------
(do
   (defn tomcat-log-file-filter [prefix year month]
     (let [regex (str/format "%s[.]%d-%02d-[0-9][0-9][.]log" prefix year month)]
       (fn [f] (match? (io/file-name f) regex))))

   (defn tomcat-log-file-zip [prefix dir year month]
     (io/file dir (str/format "%s.%d-%02d.zip" prefix year month)))

   (defn zip-files [dir zip files]
     (with-sh-throw
        (with-sh-dir dir
           (apply sh (concat ["zip" (:name zip)] (map #(:name %) files))))))

   (defn zip-tomcat-logs [prefix dir year month]
     (try
        (let [zip (tomcat-log-file-zip prefix dir year month)
              filter (tomcat-log-file-filter prefix year month)
              logs (io/list-files dir filter)]
           (printf "Compacting %s ...\n" prefix)
           (printf "   Found %d log files\n" (count logs))
           (when-not (empty? logs)
              (zip-files dir zip logs)
              (printf "   Zipped to %s\n" (:name zip))
              (apply io/delete-file logs)
              (printf "   Removed %d files\n" (count logs))))
        (catch :com.github.jlangch.venice.ShellException ex
           (printf "Error compacting %s: %s" prefix (:message ex)))))

   (defn first-day-of-month [offset]
     (-> (time/local-date) 
         (time/first-day-of-month) 
         (time/plus :month offset)))

   (let [dir (io/file (nth *ARGV* 2))
         date (first-day-of-month -1)
         year (time/year date)
         month (time/month date)]
      (if (io/exists-dir? dir)
         (do
            (printf "Compacting %d-%02d logs from '%s' ...\n" year month dir)
            (zip-tomcat-logs "localhost_access_log" dir year month)
            (zip-tomcat-logs "host-manager" dir year month)
            (zip-tomcat-logs "manager" dir year month)
            (zip-tomcat-logs "localhost" dir year month)
            (zip-tomcat-logs "catalina" dir year month)
            (println "Done."))
         (printf "Error: The Tomcat log dir '%s' does not exist" dir))))
```
