###############################################################################
# Venice management script                                                    #
# ----------------------------------------------------------------------------#
# Starts a Venice REPL, loads 'venice.venice' and runs it.                    #
#                                                                             #
# Layout:                                                                     #
#    scripts                                                                  #
#      +--libs                                                                #
#      |   +-- venice-x.y.z.jar                                               #
#      +--venice.sh                                                           #
#      +--venice.venice                                                       #
###############################################################################

export VENICE_HOME={{INSTALL_PATH}}


if [ ! -d ${VENICE_HOME} ]; then
  echo "Error: The Venice console home dir${VENICE_HOME} does not exist!"
  sleep 5
  exit 1
fi

cd ${VENICE_HOME}

${JAVA_11_HOME}/bin/java \
  -server \
  -cp "libs:libs/*" \
  com.github.jlangch.venice.Launcher \
  -Xmx2G \
  -XX:-OmitStackTraceInFastThrow \
  -colors \
  -macroexpand \
  -app-repl venice.venice
