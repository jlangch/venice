
cd /Users/juerg/Desktop/venice/

DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=1044,server=y,suspend=n"

macroexpand=

while true; do
  ${JAVA_8_HOME}/bin/java \
    -server \
    -Xmx4G \
    -XX:-OmitStackTraceInFastThrow \
    -Djava.io.tmpdir=tmp \
    -cp "libs/*" \
    com.github.jlangch.venice.Launcher \
    -loadpath "/Users/juerg/Desktop/venice/scripts;/Users/juerg/Desktop/venice/scripts.zip" \
    $macroexpand \
    -restartable \
    -colors

    # if the REPL exits with exit code 98 or 99 restart the REPL otherwise
    # exit the shell
    case "$?" in
      98) echo "Restarting..."
          macroexpand="-macroexpand"
          ;;
      99) echo "Restarting..."
          macroexpand=
          ;;
      *)  exit 0
          ;;
    esac
done
