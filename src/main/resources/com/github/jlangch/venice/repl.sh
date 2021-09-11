#!/bin/sh
# ------------------------------------------------------------------------------
# Starts a Venice REPL
# ------------------------------------------------------------------------------
# --home
#    |
#    +-- libs
#    |    +-- repl.json
#    |    +-- venice-1.9.30.jar
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

while true; do
  java \
    -server \
    -Xmx4G \
    -XX:-OmitStackTraceInFastThrow \
    -Djava.io.tmpdir=tmp \
    -cp "libs:libs/*" \
    com.github.jlangch.venice.Launcher \
    -loadpath "scripts" \
    -restartable \
    -colors

  # if the REPL exits with exit code  99 restart the REPL otherwise
  # exit the shell
  if [ $? -ne 99 ]; then exit 0; fi
done
