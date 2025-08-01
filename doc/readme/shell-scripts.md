# Shell Scripts

Venice allows you to write shell scripts without a hassle. The 'io' functions
are pretty powerful when dealing with files. You're not going to miss Unix tools
like 'awk, 'sed'.

Venice offers a comprehensive set of file io functions.

The REPL let's you interactively run and test commands.

* [Shell Extension Module](#shell-extension-module)
* [Example](#example)
* [Running Venice as a Shebang script](#running-venice-as-a-shebang-script)
* [Noticeable I/O Functions](#noticeable-i-o-functions)
* [Executing OS Shell Commands](#executing-os-shell-commands)



## Shell Extension Module

The [Shell Extension Module ](ext-shell.md) offers OS agnostic functions to manage processes or 
open files with the platform specific application.

**Utils**

- shell/open
- shell/open-macos-app
- shell/diff

**Processes**

- shell/alive?
- shell/process-handle
- shell/process-info
- shell/current-process
- shell/pid
- shell/processes
- shell/parent-process
- shell/descendant-processes
- shell/kill, shell/kill-forcibly
- shell/wait-for-process-exit

For details see: The [Shell Extension Module ](ext-shell.md)



## Example

A larger example that zips Tomcat log files on a monthly base:

 * catalina.yyyy-mm-*.log  -> catalina.yyyy-mm.zip
 * host-manager.yyyy-mm-*.log  -> host-manager.yyyy-mm.zip
 * localhost_access.yyyy-mm-*.log  -> localhost_access.yyyy-mm.zip
 * localhost.yyyy-mm-*.log  -> localhost.yyyy-mm.zip
 * manager.yyyy-mm-*.log  -> host-manager.yyyy-mm.zip


```clojure
;; -------------------------------------------------------------------------------
;; Zips the last month's Tomcat log files
;;
;; > java -jar venice-1.12.52.jar -file zip-tomcat-logs.venice ./logs
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
        (let [zip    (tomcat-log-file-zip prefix dir year month)
              filter (tomcat-log-file-filter prefix year month)
              logs   (io/list-files dir filter)]
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

   (let [dir   (io/file (nth *ARGV* 2))
         date  (first-day-of-month -1)
         year  (time/year date)
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

## Running Venice as a Shebang script

With a little bit of sorcery a Venice script can be run as a Unix Shebang script.


### REPL based Venice Shebang Script

This *shebang* demo uses the Venice interpreter from an installed Venice REPL, giving the script access to all the 3rd party libraries installed within the REPL.

**Prerequisites**

1. The Venice REPL must be installed
2. The Venice version must be v1.12.26 or higher
3. MacOS or Linux operating systems


**Example: shebang-demo.venice**

```clojure
#!/bin/sh

#_ """ 

  # Venice Shebang demo script

  # The "run-script.sh" is provided by the installed Venice REPL. It
  # starts a Venice interpreter on the REPL environment and runs this
  # script.

  REPL_HOME=/Users/juerg/Desktop/venice/

  exec ${REPL_HOME}/run-script.sh "$0" "$@"

"""

(println "Venice Shebang Demo")
(println)

(println "Args:" *ARGV*)

(println "Time:" (time/local-date-time))
```

Execution:

```
> chmod +x ./shebang-demo.venice
> ./shebang-demo.venice 1 2 3
Venice Shebang Demo

Args: (1 2 3)
Time: 2024-07-26T14:49:47.963
nil
```

### Standalone Venice Shebang Script

This shebang demo implicitly downloads the Venice library from the Maven 
repository when the script starts, provided the Venice library is not yet 
available in the installation directory.


**Example: shebang-demo.venice**

```clojure
#!/bin/sh

#_ """ 

  # Venice Shebang demo script

  VERSION=1.12.30                      # Venice version to use
  DIR=/tmp/venice                      # Install dir
  REPO=https://repo1.maven.org/maven2  # Maven repository

  JAR=venice-${VERSION}.jar

  [ -d ${DIR} ] || mkdir ${DIR}

  if [ ! -f ${DIR}/${JAR} ]; then
    echo "Downloading ${JAR} from ${REPO} to ${DIR} ..."
    curl -s "${REPO}/com/github/jlangch/venice/${VERSION}/${JAR}" --output ${DIR}/${JAR}
  fi

  exec java -server -jar "${DIR}/${JAR}" -file "$0" "$@"

"""

(println "Venice Shebang Demo")
(println)

(println "Args:" *ARGV*)

(println "Time:" (time/local-date-time))
```

Execution:

```
> chmod +x ./shebang-demo.venice
> ./shebang-demo.venice 1 2 3
Venice Shebang Demo

Args: (1 2 3)
Time: 2024-07-26T14:49:47.963
nil
```

### Running a Venice Shebang script as Unix cron job

Open the cron editor:

```
> export EDITOR=/bin/vi
> crontab -e
```

Add the following line to schedule the job:

```
30 23 * * Mon-Fri /bin/sh /home/foo/shebang-demo.venice 1 2 3
```


## Noticeable I/O Functions

### System functions

- os−type, os−type? 
- os−arch, os−name, os−version
- system-env
- current−time−millis
- sleep 
- host−name, host−address


### File I/O

- io/file 
- io/file−parent, io/file−name
- io/file−path, io/file−absolute−path, io/file−canonical−path 
- io/mkdir, io/mkdirs 
- io/slurp, io/slurp−lines, io/spit 
- io/copy−file, io/move−file
- io/delete−file, io/delete−file−tree 
- io/list−files, io/list−files−glob, io/list−file−tree 
- io/exists−file?, io/exists−dir?, io/file−can−read?, io/file−can−write?, io/file−can−execute?
- io/temp−file, io/tmp−dir, io/user−dir, io/user−home−dir 	
- ...


### Zip/Gzip

- io/zip, io/zip−file 
- io/zip−list, io/zip−list−entry−names 
- io/zip−append, io/zip−remove
- io/unzip, io/unzip−to−dir
- io/unzip−first, io/unzip−all
- io/gzip, io/gzip−to−stream 
- io/ungzip, io/ungzip−to−stream


### Web download

- io/download 
- io/internet−avail?


## Executing OS Shell Commands 

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

;; asynchronously slurping stdout and stderr from a unix shell command
(sh "/bin/sh" "-c" "for i in {1..5}; do sleep 1; echo \"Hello $i\"; done"
              :out-fn println
              :err-fn println)

;; asynchronously slurping stdout and stderr from a unix shell command with timeout
(sh "/bin/sh" "-c" "for i in {1..5}; do sleep 1; echo \"Hello $i\"; done"
              :out-fn println
              :err-fn println
              :timeout 2500)

;; list files in a directory with a glob pattern
(->> (io/list-files-glob "." "*.png")
     (docoll println))
```
