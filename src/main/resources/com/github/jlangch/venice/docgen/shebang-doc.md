# Shebang Scripts


With a little bit of sorcery a Venice script can be run as a Unix *Shebang* script.


## REPL based Venice Shebang script

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


## Standalone Venice Shebang script

This *shebang* demo implicitly downloads the Venice library from the Maven 
repository when the script starts, provided the Venice library is not yet 
available in the installation directory.


**Example: shebang-demo.venice**

```clojure
#!/bin/sh

#_ """ 

  # Venice Shebang demo script

  VERSION=1.12.51                      # Venice version to use
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

## Running a Venice Shebang script as Unix cron job

Open the cron editor:

```
> export EDITOR=/bin/vi
> crontab -e
```

Add the following line to schedule the job:

```
30 23 * * Mon-Fri /bin/sh /home/foo/shebang-demo.venice 1 2 3
```

