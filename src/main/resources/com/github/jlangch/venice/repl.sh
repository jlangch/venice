#!/bin/sh
# ------------------------------------------------------------------------------
# Starts a Venice REPL
# ------------------------------------------------------------------------------
# --home
#    |
#    +-- libs
#    |    +-- repl.json
#    |    +-- venice-1.9.16.jar
#    |
#    +-- scripts
#    |    +-- script-1.venice
#    |    +-- script-2.venice
#    |
#    +-- tmp
#    |
#    +-- repl.sh
# ------------------------------------------------------------------------------


cd /Users/foo/venice/

macroexpand=

while true; do
  java \
    -server \
    -Xmx4G \
    -XX:-OmitStackTraceInFastThrow \
    -Djava.io.tmpdir=tmp \
    -cp "libs:libs/*" \
    com.github.jlangch.venice.Launcher \
    -loadpath "scripts" \
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
