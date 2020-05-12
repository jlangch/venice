#!/bin/sh
# ------------------------------------------------------------------------------
# Starts a Venice REPL
# ------------------------------------------------------------------------------
# --home
#    |
#    +-- libs
#    |    +-- repl.json
#    |    +-- venice-1.7.25.jar
#    |
#    +-- scripts
#    |    +-- script-1.venice
#    |    +-- script-2.venice
#    |
#    +-- repl.sh
# ------------------------------------------------------------------------------


cd /Users/foo/venice/

java \
  -server \
  -Xmx4G \
  -XX:-OmitStackTraceInFastThrow \
  -cp "libs:libs/*" \
  com.github.jlangch.venice.Launcher \
  -loadpath "scripts" \
  -colors
