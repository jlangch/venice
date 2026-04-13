#!/bin/bash
# ------------------------------------------------------------------------------
# Starts a Venice REPL
# ------------------------------------------------------------------------------
# REPL_HOME
#    |
#    +-- libs
#    |    +-- repl.json
#    |    +-- venice-1.12.89.jar
#    |
#    +-- scripts
#    |    +-- script-1.venice
#    |    +-- script-2.venice
#    |
#    +-- tmp
#    |
#    +-- repl.env
#    +-- repl.sh
# ------------------------------------------------------------------------------

# Do not set custom variables in this script. Instead put them into the
# repl.env in the REPL_HOME to keep your customizations separate.


export REPL_HOME={{INSTALL_PATH}}


if [ ! -d "${REPL_HOME}" ]; then
  echo "Error: The REPL home dir ${REPL_HOME} does not exist!"
  sleep 5
  exit 1
fi

[ ! -d "${REPL_HOME}/tmp" ] && mkdir "${REPL_HOME}/tmp"
[ ! -d "${REPL_HOME}/scripts" ] && mkdir "${REPL_HOME}/scripts"


cd "$REPL_HOME"

while true; do
  
  # load REPL environment variables (note: source command is available in bash only!)
  [ -f "${REPL_HOME}/repl.env" ] && source "${REPL_HOME}/repl.env"

  if [ "${JAVA_HOME}" = "" ] ; then
    echo "ERROR: JAVA_HOME not found in your environment."
    echo
    echo "Please, set the JAVA_HOME variable in your environment to match the"
    echo "location of the Java Virtual Machine you want to use."
    echo 
    echo Alternatively set JAVA_HOME in the 'repl.env' file
    sleep 5
    exit 1
  fi

  if [ -f "${REPL_HOME}/.repl.upgrade" ]; then 
    # finish the initiated upgrade
    "${JAVA_HOME}/bin/java" \
      -Djava.io.tmpdir="${REPL_HOME}/tmp" \
      -Dvenice.repl.home="${REPL_HOME}" \
      -cp "libs:libs/*" \
      com.github.jlangch.venice.Launcher \
      -repl-upgrade 
  fi


  JAVA_OPTS="-server -Xmx4G -XX:-OmitStackTraceInFastThrow ${DEBUG_OPTS}"

  "${JAVA_HOME}/bin/java" \
    ${JAVA_OPTS} \
    -Djava.io.tmpdir="${REPL_HOME}/tmp" \
    -Dvenice.repl.home="${REPL_HOME}" \
    -cp "libs:libs/*" \
    com.github.jlangch.venice.Launcher \
    -repl \
    -loadpath "" \
    -restartable \
    -colors

  # if the REPL exits with exit code  99 restart the REPL otherwise
  # exit the shell
  if [ $? -ne 99 ]; then exit 0; fi
done
