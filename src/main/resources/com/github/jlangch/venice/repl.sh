#!/bin/sh
# ------------------------------------------------------------------------------
# Starts a Venice REPL
# ------------------------------------------------------------------------------
# --home
#    |
#    +-- libs
#    |    +-- repl.json
#    |    +-- venice-1.10.21.jar
#    |
#    +-- scripts
#    |    +-- script-1.venice
#    |    +-- script-2.venice
#    |
#    +-- tmp
#    |
#    +-- repl.sh
# ------------------------------------------------------------------------------


REPL_HOME={{INSTALL_PATH}}

if [ ! -d ${REPL_HOME} ]; then
  echo "Error: The REPL home dir ${REPL_HOME} does not exist!"
  sleep 5
  exit 1
fi

if [ ! -d ${REPL_HOME}/tmp ]; then
  mkdir ${REPL_HOME}/tmp
fi

cd $REPL_HOME

while true; do
  java \
    -server \
    -Xmx4G \
    -XX:-OmitStackTraceInFastThrow \
    -Djava.io.tmpdir=${REPL_HOME}/tmp \
    -cp "libs:libs/*" \
    com.github.jlangch.venice.Launcher \
    -loadpath "" \
    -restartable \
    -colors

  # if the REPL exits with exit code  99 restart the REPL otherwise
  # exit the shell
  if [ $? -ne 99 ]; then exit 0; fi
done
