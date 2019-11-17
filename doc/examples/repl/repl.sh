
cd /Users/juerg/Desktop/venice/

${JAVA_11_HOME}/bin/java \
  -server \
  -cp "libs/*" com.github.jlangch.venice.Launcher \
  -Xmx2G \
  -XX:-OmitStackTraceInFastThrow \
  -loadpath /Users/juerg/Desktop/venice/scripts \
  -colors
