export REPL_HOME={{INSTALL_PATH}}

if [ ! -d ${REPL_HOME} ]; then
  echo "Error: The REPL home dir ${REPL_HOME} does not exist!"
  exit 1
fi

if [ ! -d ${REPL_HOME}/tmp ]; then
  echo "Error: The REPL tmp dir ${REPL_HOME}/tmp does not exist!"
  exit 1
fi

if [ -f ${REPL_HOME}/repl.env ]; then
  source ${REPL_HOME}/repl.env
fi

SCRIPT=$1
shift

${JAVA_8_HOME}/bin/java \
  -server \
  -XX:-OmitStackTraceInFastThrow \
  -Djava.io.tmpdir=${REPL_HOME}/tmp \
  -cp "${REPL_HOME}/libs:${REPL_HOME}/libs/*" \
  com.github.jlangch.venice.Launcher \
  -file ${SCRIPT} \
  "$@"
