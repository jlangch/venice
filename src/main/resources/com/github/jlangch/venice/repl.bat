@ECHO OFF
REM # -------------------------------------------------------------------------
REM # Starts a Venice REPL
REM # -------------------------------------------------------------------------
REM # --home
REM #    |
REM #    +-- libs
REM #    |    +-- repl.json
REM #    |    +-- venice-1.9.24.jar
REM #    |
REM #    +-- scripts
REM #    |    +-- script-1.venice
REM #    |    +-- script-2.venice
REM #    |
REM #    +-- tmp
REM #    |
REM #    +-- repl.sh
REM # ------------------------------------------------------------------------------

cd C:/Users/foo/venice

:start

java.exe ^
  -server ^
  -Xmx2G ^
  -XX:-OmitStackTraceInFastThrow ^
  -Djava.io.tmpdir=tmp ^
  -cp "libs;libs/*" com.github.jlangch.venice.Launcher ^
  -loadpath "scripts" ^
  -restartable ^
  -colors-darkmode
  
REM # if the REPL exits with exit code 99 restart the REPL otherwise
REM # exit the shell
if %errorlevel% equ 99 goto start
