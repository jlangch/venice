@ECHO OFF
REM ############################################################################
REM # Venice shell                                                             #
REM # -------------------------------------------------------------------------#
REM # Starts a Venice shell (custom REPL), loads 'venice.venice' and runs it.  #
REM #                                                                          #
REM # Layout:                                                                  #
REM #    VENICE_SHELL_HOME                                                     #
REM #      +--libs                                                             #
REM #      |   +-- venice-x.y.z.jar                                            #
REM #      |   +-- jansi-2.4.1.jar                                             #
REM #      +--venice.bat                                                       #
REM #      +--venice.venice                                                    #
REM ############################################################################

set VENICE_SHELL_HOME=C:\Users\juerg\Desktop\scripts
set VENICE_PROJECT_HOME=C:\Users\juerg\Documents\workspace\venice
set VENICE_REPL_HOME=C:\Users\juerg\Desktop\venice


if not exist %VENICE_SHELL_HOME% (
  echo Error: The Venice shell home dir %VENICE_SHELL_HOME% does not exist!
  timeout /t 5
  exit 2
)

if not exist %VENICE_SHELL_HOME%\libs (
  echo Error: The Venice shell libs dir %VENICE_SHELL_HOME%\libs does not exist!
  timeout /t 5
  exit 2
)

if not exist %JAVA_8_HOME%\libs (
  echo Error: The Java 8 home dir %JAVA_8_HOME% does not exist!
  timeout /t 5
  exit 2
)


cd %VENICE_SHELL_HOME%

"%JAVA_8_HOME%\bin\java.exe" ^
  -server ^
  -Xmx2G ^
  -XX:-OmitStackTraceInFastThrow ^
  -cp "libs;libs/*" ^
  com.github.jlangch.venice.Launcher ^
  -Dvenice.repl.home=%VENICE_SHELL_HOME% ^
  -colors-darkmode ^
  -app-repl venice.venice
 