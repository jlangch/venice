@ECHO OFF

REM https://ss64.com/nt/cmd.html

rebuild () {
  ./gradlew --warning-mode all clean shadowJar
  rm ${REPL_HOME}/libs/venice-*.jar
  cp build/libs/venice-*.jar ${REPL_HOME}/libs
  echo "Starting new REPL..."
  start
}

REM %programfiles(x86)%
set JAVA_HOME=%programfiles%\zulu\zulu-8
set REPL_HOME=%homedrive%%homepath%\Desktop\venice
set WORKSPACE_HOME=%homedrive%%homepath%\workspace\venice

set PATH=%JAVA_HOME%\bin:%PATH%

cd %WORKSPACE_HOME%

:start 
%REPL_HOME%\repl.bat
EXIT /B 0

:rebuild
gradlew --warning-mode all clean shadowJar
del %REPL_HOME%\libs\venice-*.jar
copy build\libs\venice-*.jar %REPL_HOME%\libs
EXIT /B 0

/bin/sh
