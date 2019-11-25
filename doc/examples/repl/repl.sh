
cd /Users/juerg/Desktop/venice/

${JAVA_11_HOME}/bin/java \
  -server \
  -Xmx2G \
  -XX:-OmitStackTraceInFastThrow \
  -cp "libs/*" \
  com.github.jlangch.venice.Launcher \
  -loadpath /Users/juerg/Desktop/venice/scripts \
  -colors
