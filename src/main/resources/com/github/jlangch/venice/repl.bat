REM # -------------------------------------------------------------------------
REM # Starts a Venice REPL
REM # -------------------------------------------------------------------------
REM # --home
REM #    |
REM #    +-- libs
REM #    |    +-- venice-1.7.25.jar
REM #    |    +-- jansi-1.18.jar
REM #    |
REM #    +-- repl.json
REM #    |
REM #    +-- repl.bat
REM # -------------------------------------------------------------------------

java.exe ^
  -server ^
  -Xmx2G ^
  -XX:-OmitStackTraceInFastThrow ^
  -cp "libs\*" ^
  com.github.jlangch.venice.Launcher ^
  -colors
