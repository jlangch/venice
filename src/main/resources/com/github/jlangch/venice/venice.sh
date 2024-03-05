###############################################################################
# Venice shell                                                                #
# ----------------------------------------------------------------------------#
# Starts a Venice shell (REPL), loads 'venice.venice' and runs it.            #
#                                                                             #
# Layout:                                                                     #
#    scripts                                                                  #
#      +--libs                                                                #
#      |   +-- venice-x.y.z.jar                                               #
#      |   +-- jansi-2.4.1.jar                                                #
#      +--venice.sh                                                           #
#      +--venice.venice                                                       #
###############################################################################

export VENICE_SHELL_HOME=/Users/juerg/Desktop/scripts
export VENICE_PROJECT_HOME=/Users/juerg/Documents/workspace-omni/venice
export VENICE_REPL_HOME=/Users/juerg/Desktop/venice


if [ ! -d ${VENICE_SHELL_HOME} ]; then
  echo
  echo "Error: The Venice shell home dir ${VENICE_SHELL_HOME} does not exist!"
  echo
  read -p "Press any key to exit..." -n 1 -s
  exit 1
fi

if [ ! -d ${VENICE_SHELL_HOME}/libs ]; then
  echo
  echo "Error: The Venice shell libs dir ${VENICE_SHELL_HOME}/libs does not exist!"
  echo
  read -p "Press any key to exit..." -n 1 -s
  exit 1
fi

if [ ! -d ${JAVA_8_HOME} ]; then
  echo
  echo "Error: The Java 8 home dir ${JAVA_8_HOME} does not exist!"
  echo
  read -p "Press any key to exit..." -n 1 -s
  exit 1
fi


cd ${VENICE_SHELL_HOME}


${JAVA_8_HOME}/bin/java \
  -server \
  -cp "lib:libs/*" com.github.jlangch.venice.Launcher \
  -Xmx2G \
  -XX:-OmitStackTraceInFastThrow \
  -Dvenice.repl.home=${VENICE_SHELL_HOME} \
  -colors \
  -macroexpand \
  -app-repl venice.venice
