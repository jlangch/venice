REM # -------------------------------------------------------------------------
REM # Starts a Venice REPL
REM # -------------------------------------------------------------------------
REM # --home
REM #    |
REM #    +-- libs
REM #    |    +-- venice-1.7.24.jar
REM #    |
REM #    +-- scripts
REM #    |    +-- script-1.venice
REM #    |    +-- script-2.venice
REM #    |
REM #    +-- repl.json
REM #    |
REM #    +-- repl.bat
REM # -------------------------------------------------------------------------

java.exe ^
  -server ^
  -Xmx4G ^
  -XX:-OmitStackTraceInFastThrow ^
  -cp "libs\*" ^
  com.github.jlangch.venice.Launcher ^
  -loadpath "scripts" ^
  -colors
