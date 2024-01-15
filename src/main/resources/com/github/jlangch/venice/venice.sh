###############################################################################
# Venice management script                                                    #
# ----------------------------------------------------------------------------#
# Starts a Venice REPL, loads 'venice.venice' and runs it.                    #
#                                                                             #
# Layout:                                                                     #
#    scripts                                                                  #
#      +--libs                                                                #
#      |   +-- venice-x.y.z.jar                                               #
#      |   +-- jansi-2.4.1.jar                                                #
#      +--venice.sh                                                           #
#      +--venice.venice                                                       #
###############################################################################

export VENICE_CONSOLE_HOME=/Users/juerg/Desktop/scripts
export VENICE_PROJECT_HOME=/Users/juerg/Documents/workspace-omni/venice
export VENICE_REPL_HOME=/Users/juerg/Desktop/venice


if [ ! -d ${VENICE_CONSOLE_HOME} ]; then
  echo
  echo "Error: The Venice console home dir ${VENICE_CONSOLE_HOME} does not exist!"
  sleep 5
  exit 1
fi

if [ ! -d ${VENICE_CONSOLE_HOME}/libs ]; then
  echo
  echo "Error: The Venice console libs dir ${VENICE_CONSOLE_HOME}/libs does not exist!"
  sleep 5
  exit 1
fi


cd ${VENICE_CONSOLE_HOME}


${JAVA_11_HOME}/bin/java \
  -server \
  -cp "libs/*" com.github.jlangch.venice.Launcher \
  -Xmx2G \
  -XX:-OmitStackTraceInFastThrow \
  -Dvenice.repl.home=${VENICE_REPL_HOME} \
  -colors \
  -macroexpand \
  -app-repl venice.venice
