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
REM #      +--venice.bat                                                        #
REM #      +--venice.venice                                                    #
REM ############################################################################

set VENICE_HOME={{INSTALL_PATH}}

if not exist %VENICE_HOME% (
  echo Error: The Venice console home dir %VENICE_HOME% does not exist!
  timeout /t 10
  exit 2
)

cd %VENICE_HOME%


java.exe ^
  -server ^
  -Xmx2G ^
  -XX:-OmitStackTraceInFastThrow ^
  -cp "libs;libs/*" ^
  com.github.jlangch.venice.Launcher ^
  -colors-darkmode ^
  -macroexpand ^
  -app-repl venice.venice
 