# -----------------------------------------------------------------------------
# Initializes the REPL for a new Gitpod instance
#
# This script is run as part of the .gitpod.yaml config
# -----------------------------------------------------------------------------

if [ `pwd` = "/workspace/venice" ]; then
  if [ -d "../repl" ]; then
    cd ../repl;
    ./repl.sh
  else
    cd ..;
    mkdir repl;
    cd repl;
    java -jar ../venice/build/libs/venice-*.jar -setup -colors-darkmode;
    [ -d "./tmp" ] && rm -f ./tmp/*
    ./repl.sh
  fi
fi
