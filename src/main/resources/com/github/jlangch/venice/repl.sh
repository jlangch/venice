#!/bin/sh
# ------------------------------------------------------------------------------
# Starts a Venice REPL
# ------------------------------------------------------------------------------
# --home
#    |
#    +-- libs
#    |    +-- venice-1.7.25.jar
#    |
#    +-- scripts
#    |    +-- script-1.venice
#    |    +-- script-2.venice
#    |
#    +-- repl.json
#    |
#    +-- repl.sh
# ------------------------------------------------------------------------------


cd /Users/foo/venice/

java \
  -server \
  -Xmx4G \
  -XX:-OmitStackTraceInFastThrow \
  -cp "libs/*" \
  com.github.jlangch.venice.Launcher \
  -loadpath "scripts" \
  -colors
