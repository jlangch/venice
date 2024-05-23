@ECHO OFF
REM # -------------------------------------------------------------------------
REM # Starts a Venice REPL
REM # -------------------------------------------------------------------------
REM # --home
REM #    |
REM #    +-- libs
REM #    |    +-- repl.json
REM #    |    +-- venice-1.12.24.jar
REM #    |
REM #    +-- scripts
REM #    |    +-- script-1.venice
REM #    |    +-- script-2.venice
REM #    |
REM #    +-- tmp
REM #    |
REM #    +-- repl.bat
REM # -------------------------------------------------------------------------

set REPL_HOME={{INSTALL_PATH}}

if not exist %REPL_HOME% (
  echo The REPL home dir %REPL_HOME% does not exist!
  timeout /t 10
  exit 2
)

if not exist %REPL_HOME%\tmp mkdir %REPL_HOME%\tmp
if not exist %REPL_HOME%\scripts mkdir %REPL_HOME%\scripts


cd %REPL_HOME%


:start

REM # load environment variables
if exist %REPL_HOME%/repl.env.bat (
  call %REPL_HOME%/repl.env.bat
)

java.exe ^
  -server ^
  -Xmx2G ^
  -XX:-OmitStackTraceInFastThrow ^
  -Djava.io.tmpdir=%REPL_HOME%\tmp ^
  -Dvenice.repl.home=%REPL_HOME% ^
  -cp "libs;libs/*" com.github.jlangch.venice.Launcher ^
  -restartable ^
  -colors-darkmode
  
REM # if the REPL exits with exit code 99 restart the REPL otherwise
REM # exit the shell
if %errorlevel% equ 99 goto start
