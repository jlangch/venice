#!/bin/bash
# ------------------------------------------------------------------------------
# Venice script runner
#
# Demo script 'demo1.venice':
#     (do
#       (defn circle-area [radius] (* math/PI radius radius))
#       (println (circle-area 2.5)))
#
# Demo script 'demo2.venice', passing command line args:
#     (+ 1 (long (first *ARGV*)))
# 
# > run-script ./demo1.venice
# > run-script ./demo2.venice 200
# ------------------------------------------------------------------------------

# Set JAVA_HOME locally
# export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home/

export REPL_HOME={{INSTALL_PATH}}


if [ "${JAVA_HOME}" = "" ] ; then
  echo "ERROR: JAVA_HOME not found in your environment."
  echo
  echo "Please, set the JAVA_HOME variable in your environment to match the"
  echo "location of the Java Virtual Machine you want to use."
  echo 
  echo Alternatively set JAVA_HOME in this run-script.sh
  exit 1
fi

if [ ! -d "${REPL_HOME}" ]; then
  echo "Error: The REPL home dir ${REPL_HOME} does not exist!"
  exit 1
fi

if [ ! -d "${REPL_HOME}/tmp" ]; then
  echo "Error: The REPL tmp dir ${REPL_HOME}/tmp does not exist!"
  exit 1
fi

if [ -f "${REPL_HOME}/repl.env" ]; then
  source "${REPL_HOME}/repl.env"
fi

SCRIPT=$1
shift

if [ "${SCRIPT}" = "" ] ; then
  echo "ERROR: No script file passed to run."
  echo "       E.g.:  > echo \"(+ 1 (long (first *ARGV*)))\" > ./demo.venice"
  echo "              > ./run-script.sh ./demo.venice 10"
  exit 1
fi

if [ ! -f "${SCRIPT}" ]; then
  echo "ERROR: The script file '${SCRIPT}' does not exist."
  exit 1
fi

"${JAVA_HOME}/bin/java" \
  -server \
  -XX:-OmitStackTraceInFastThrow \
  -Djava.io.tmpdir="${REPL_HOME}/tmp" \
  -cp "${REPL_HOME}/libs:${REPL_HOME}/libs/*" \
  com.github.jlangch.venice.Launcher \
  -file ${SCRIPT} \
  "$@"
