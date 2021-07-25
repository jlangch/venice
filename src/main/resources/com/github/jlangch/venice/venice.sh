#!/bin/sh

help () {
  echo "-------------------------------------------------------------------------"
  echo "./gradlew --warning-mode all clean shadowJar"
  echo "./gradlew test"
  echo "./gradlew cheatsheet"
  echo "./gradlew updateReleaseVersion"
  echo "./gradlew -Dorg.gradle.internal.publish.checksums.insecure=true \\"
  echo "          -Dorg.gradle.internal.http.socketTimeout=60000 \\"
  echo "          -Dorg.gradle.internal.http.connectionTimeout=60000 \\"
  echo "          --warning-mode all \\"
  echo "          -Psigning.gnupg.keyName=${PGP_KEY} \\"
  echo "          -PsonatypeUsername=${SONATYPE_USER} \\"
  echo "          -PsonatypePassword=\"123\" \\"
  echo "          clean shadowJar publish"
  echo "./gradlew jmh -Pinclude=\".*PrecompileBenchmark\""
  echo "./gradlew -Dorg.gradle.java.home=\${JAVA_11_ZULU_HOME} jmh -Pinclude=\".*PrecompileBenchmark\""
  echo "cp build/libs/venice-*.jar ~/Desktop/venice/libs"
  echo "open /Users/juerg/Desktop/venice/repl.sh"
  echo "-------------------------------------------------------------------------"
  echo
}

publish () {
  ./gradlew -Dorg.gradle.internal.publish.checksums.insecure=true \
            -Dorg.gradle.internal.http.socketTimeout=60000 \
            -Dorg.gradle.internal.http.connectionTimeout=60000 \
            --warning-mode all \
            -Psigning.gnupg.keyName=${PGP_KEY} \
            -PsonatypeUsername=${SONATYPE_USER} \
            -PsonatypePassword="$1" \
            clean shadowJar publish
}

rebuild () {
  ./gradlew --warning-mode all clean shadowJar
  cp build/libs/venice-*.jar ${REPL_HOME}/libs
  echo "Starting new REPL..."
  open ${REPL_HOME}/repl.sh
}

cheatsheet () {
  ./gradlew cheatsheet
}


export JAVA_HOME=${JAVA_8_OPENJDK_HOME}
export REPL_HOME=~/Desktop/venice
export WORKSPACE_HOME=~/Documents/workspace-omni/venice

export PGP_KEY=111111AA
export SONATYPE_USER=xxxxxx

export -f rebuild
export -f cheatsheet
export -f help

cd ${WORKSPACE_HOME}

/bin/sh
