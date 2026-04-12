@ECHO OFF
REM # -------------------------------------------------------------------------
REM # Starts a Venice REPL
REM # -------------------------------------------------------------------------
REM # REPL_HOME
REM #    |
REM #    +-- libs
REM #    |    +-- repl.json
REM #    |    +-- venice-1.12.89.jar
REM #    |
REM #    +-- scripts
REM #    |    +-- script-1.venice
REM #    |    +-- script-2.venice
REM #    |
REM #    +-- tmp
REM #    |
REM #    +-- repl.env.bat
REM #    +-- repl.bat
REM # -------------------------------------------------------------------------

REM # Do not set custom variables in this script. Instead put them into the
REM # repl.env.bat in the REPL_HOME to keep your customizations separate.


set REPL_HOME={{INSTALL_PATH}}


if not exist "%REPL_HOME%" (
  echo The REPL home dir %REPL_HOME% does not exist!
  timeout /t 5
  exit 2
)

if not exist "%REPL_HOME%\tmp" mkdir "%REPL_HOME%\tmp"
if not exist "%REPL_HOME%\scripts" mkdir "%REPL_HOME%\scripts"


cd "%REPL_HOME%"


:start

REM # load REPL environment variables
if exist "%REPL_HOME%/repl.env.bat" (
  call "%REPL_HOME%/repl.env.bat"
)

if "%JAVA_HOME%" == "" goto :error

"%JAVA_HOME%\bin\java.exe" ^
  -server ^
  -Xmx2G ^
  -XX:-OmitStackTraceInFastThrow ^
  -Djava.io.tmpdir="%REPL_HOME%\tmp" ^
  -Dvenice.repl.home="%REPL_HOME%" ^
  -cp "libs;libs/*" com.github.jlangch.venice.Launcher ^
  -restartable ^
  -colors-darkmode
  
REM # if the REPL exits with exit code 99 restart the REPL otherwise
REM # exit the shell
if %errorlevel% equ 99 goto start

goto :end

:error
echo ERROR: JAVA_HOME not found in your environment.
echo Please, set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.
echo.
echo Alternatively set JAVA_HOME in the 'repl.env.bat' file. 
timeout /t 5
exit 2

:end