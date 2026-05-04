#!/bin/bash
# ------------------------------------------------------------------------------
# Starts a Venice REPL
# ------------------------------------------------------------------------------
# REPL_HOME
#    |
#    +-- libs
#    |    +-- repl.json
#    |    +-- venice-1.13.3.jar
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

# ------------------------------------------------------------------------------
# !!!!!             PLEASE DO NOT MODIFY THIS REPL START SCRIPT            !!!!!
# ------------------------------------------------------------------------------
# Do not set custom variables in this script. Instead put them into the
# {REPL_HOME}/repl.env in the REPL_HOME to keep your customizations separate.
#
# The {REPL_HOME}/repl.env defines the REPL env vars 
#   - JAVA_HOME
#   - JAVA_OPTS
#   - LOADPATH
# used within this start script. These can be modified as well.
# ------------------------------------------------------------------------------


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


  # finish an initiated upgrade
  if [ -d "${REPL_HOME}/.upgrade" ]; then
    "${JAVA_HOME}/bin/java" \
      -Djava.io.tmpdir="${REPL_HOME}/tmp" \
      -Dvenice.repl.home="${REPL_HOME}" \
      -cp "${REPL_HOME}/.upgrade/*" \
      com.github.jlangch.venice.Upgrader

    rm -rf "${REPL_HOME}/.upgrade"
  fi


  JAVA_VM_OPTS="-server -XX:-OmitStackTraceInFastThrow ${JAVA_OPTS} ${DEBUG_OPTS}"

  # start the REPL
  "${JAVA_HOME}/bin/java" \
    ${JAVA_VM_OPTS} \
    -Djava.io.tmpdir="${REPL_HOME}/tmp" \
    -Dvenice.repl.home="${REPL_HOME}" \
    -cp "libs:libs/*" \
    com.github.jlangch.venice.Launcher \
    -repl \
    -loadpath "${LOADPATH}" \
    -restartable \
    -colors ${COLOR_MODE}

  # if the REPL exits with exit code  99 restart the REPL otherwise
  # exit the shell
  if [ $? -ne 99 ]; then exit 0; fi

done
