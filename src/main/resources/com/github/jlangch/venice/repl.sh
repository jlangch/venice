#!/bin/sh
# ------------------------------------------------------------------------------
# Starts a Venice REPL
# ------------------------------------------------------------------------------
# --home
#    |
#    +-- libs
#    |    +-- repl.json
#    |    +-- venice-1.8.9.jar
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

java \
  -server \
  -Xmx4G \
  -XX:-OmitStackTraceInFastThrow \
  -Djava.io.tmpdir=tmp \
  -cp "libs:libs/*" \
  com.github.jlangch.venice.Launcher \
  -loadpath "scripts" \
  -colors
