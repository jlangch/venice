# -----------------------------------------------------------------------------
#   __    __         _
#   \ \  / /__ _ __ (_) ___ ___
#    \ \/ / _ \ '_ \| |/ __/ _ \
#     \  /  __/ | | | | (_|  __/
#      \/ \___|_| |_|_|\___\___|
#
#
# Copyright 2017-2023 Venice
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
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
