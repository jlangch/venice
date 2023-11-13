#!/bin/sh
# ------------------------------------------------------------------------------
# Starts a Venice REPL
# ------------------------------------------------------------------------------
# --home
#    |
#    +-- libs
#    |    +-- repl.json
#    |    +-- venice-1.10.53.jar
#    |
#    +-- scripts
#    |    +-- script-1.venice
#    |    +-- script-2.venice
#    |
#    +-- tmp
#    |
#    +-- repl.sh
# ------------------------------------------------------------------------------


export REPL_HOME={{INSTALL_PATH}}

if [ ! -d ${REPL_HOME} ]; then
  echo "Error: The REPL home dir ${REPL_HOME} does not exist!"
  sleep 5
  exit 1
fi

[ ! -d ${REPL_HOME}/tmp ] && mkdir ${REPL_HOME}/tmp
[ ! -d ${REPL_HOME}/fonts ] && mkdir ${REPL_HOME}/fonts
[ ! -d ${REPL_HOME}/scripts ] && mkdir ${REPL_HOME}/scripts


cd $REPL_HOME

while true; do
  java \
    -server \
    -Xmx4G \
    -XX:-OmitStackTraceInFastThrow \
    -Djava.io.tmpdir=${REPL_HOME}/tmp \
    -Dvenice.repl.home=${REPL_HOME} \
    -cp "libs:libs/*:fonts" \
    com.github.jlangch.venice.Launcher \
    -loadpath "" \
    -restartable \
    -colors

  # if the REPL exits with exit code  99 restart the REPL otherwise
  # exit the shell
  if [ $? -ne 99 ]; then exit 0; fi
done
