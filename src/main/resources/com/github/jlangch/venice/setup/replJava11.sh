#!/bin/sh
# ------------------------------------------------------------------------------
# Starts a Venice REPL
# ------------------------------------------------------------------------------

export REPL_HOME=/Users/juerg/Desktop/venice/

if [ ! -d ${REPL_HOME} ]; then
  echo "Error: The REPL home dir ${REPL_HOME} does not exist!"
  exit 1
fi

[ ! -d ${REPL_HOME}/tmp ] && mkdir ${REPL_HOME}/tmp
[ ! -d ${REPL_HOME}/scripts ] && mkdir ${REPL_HOME}/scripts

cd $REPL_HOME

DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=1044,server=y,suspend=n"

while true; do
  ${JAVA_11_HOME}/bin/java \
    -server \
    -Xmx4G \
    -XX:-OmitStackTraceInFastThrow \
    -XX:+AlwaysPreTouch \
    -Djava.io.tmpdir=${REPL_HOME}/tmp \
    -Dvenice.repl.home=${REPL_HOME} \
    -cp "libs:libs/*" \
    com.github.jlangch.venice.Launcher \
    -loadpath "" \
    -restartable \
    -colors

  # if the REPL exits with exit code 99 restart the REPL otherwise
  # exit the shell
  if [ $? -ne 99 ]; then exit 0; fi

done
