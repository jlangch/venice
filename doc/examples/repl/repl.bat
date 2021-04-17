REM # -------------------------------------------------------------------------
REM # Starts a Venice REPL
REM # -------------------------------------------------------------------------
REM # --home
REM #    |
REM #    +-- libs
REM #    |    +-- venice-1.9.0.jar
REM #    |    +-- jansi-1.18.jar
REM #    |
REM #    +-- repl.json
REM #    |
REM #    +-- repl.bat
REM # -------------------------------------------------------------------------

cd C:/Users/foo/venice

:start

java.exe ^
  -server ^
  -Xmx2G ^
  -XX:-OmitStackTraceInFastThrow ^
  -cp "libs\*" ^
  com.github.jlangch.venice.Launcher ^
  -loadpath "scripts" ^
  -restartable ^
  -colors
  
REM # if the REPL exits with exit code 99 restart the REPL otherwise
REM # exit the shell
if %errorlevel% equ 99 goto start
