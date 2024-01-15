@ECHO OFF
REM ############################################################################
REM # Venice management script                                                 #
REM # -------------------------------------------------------------------------#
REM # Starts a Venice REPL, loads 'venice.venice' and runs it.                 #
REM #                                                                          #
REM # Layout:                                                                  #
REM #    scripts                                                               #
REM #      +--libs                                                             #
REM #      |   +-- venice-x.y.z.jar                                            #
REM #      |   +-- jansi-2.4.1.jar                                             #
REM #      +--venice.bat                                                       #
REM #      +--venice.venice                                                    #
REM ############################################################################

set VENICE_CONSOLE_HOME=C:\Users\juerg\Desktop\scripts
set VENICE_PROJECT_HOME=C:\Users\juerg\Documents\workspace\venice
set VENICE_REPL_HOME=C:\Users\juerg\Desktop\venice


if not exist %VENICE_CONSOLE_HOME% (
  echo Error: The Venice console home dir %VENICE_CONSOLE_HOME% does not exist!
  timeout /t 10
  exit 2
)

if not exist %VENICE_CONSOLE_HOME%\libs (
  echo Error: The Venice console libs dir %VENICE_CONSOLE_HOME%\libs does not exist!
  timeout /t 10
  exit 2
)

if not exist %JAVA_8_HOME%\libs (
  echo Error: The Java 8 home dir %JAVA_8_HOME% does not exist!
  timeout /t 10
  exit 2
)


cd %VENICE_CONSOLE_HOME%

"%JAVA_8_HOME%\bin\java.exe" ^
  -server ^
  -Xmx2G ^
  -XX:-OmitStackTraceInFastThrow ^
  -cp "libs;libs/*" ^
  com.github.jlangch.venice.Launcher ^
  -Dvenice.repl.home=%VENICE_CONSOLE_HOME% ^
  -colors-darkmode ^
  -macroexpand ^
  -app-repl venice.venice
 