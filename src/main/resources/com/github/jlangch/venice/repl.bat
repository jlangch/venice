@ECHO OFF
REM # -------------------------------------------------------------------------
REM # Starts a Venice REPL
REM # -------------------------------------------------------------------------
REM # --home
REM #    |
REM #    +-- libs
REM #    |    +-- repl.json
REM #    |    +-- venice-1.9.0.jar
REM #    |    +-- jansi-1.18.jar
REM #    |
REM #    +-- repl.bat
REM # -------------------------------------------------------------------------

cd C:/Users/foo/venice

java.exe ^
  -server ^
  -Xmx2G ^
  -XX:-OmitStackTraceInFastThrow ^
  -cp "libs;libs/*" com.github.jlangch.venice.Launcher ^
  -loadpath "scripts" ^
  -colors-darkmode

