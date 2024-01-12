###############################################################################
# Venice management script                                                    #
# ----------------------------------------------------------------------------#
# Starts a Venice REPL, loads 'step.venice' and runs it.                      #
#                                                                             #
# Layout:                                                                     #
#    scripts                                                                  #
#      +--libs                                                                #
#      |   +-- venice-x.y.z.jar                                               #
#      +--venice.sh                                                             #
#      +--venice.venice                                                         #
###############################################################################

cd /Users/juerg/Desktop/scripts/

${JAVA_11_HOME}/bin/java \
  -server \
  -cp "libs/*" com.github.jlangch.venice.Launcher \
  -Xmx2G \
  -XX:-OmitStackTraceInFastThrow \
  -colors \
  -macroexpand \
  -app-repl venice.venice
