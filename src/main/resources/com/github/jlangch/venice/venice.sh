#!/bin/sh

help () {
  echo "-------------------------------------------------------------------------"
  echo "rebuild     rebuild, deploy, and start the Venice REPL"
  echo "start       start the Venice REPL"
  echo "tests       run the unit tests"
  echo "cheatsheet  generate the cheatsheets"
  echo "publish     Publish Venice artefacts to Maven"
  echo ""
  echo "Gradle commands:"
  echo "./gradlew test"
  echo "./gradlew clean shadowJar sourceJar"
  echo "./gradlew cheatsheet"
  echo "./gradlew updateReleaseVersion"
  echo "./gradlew -Dorg.gradle.internal.publish.checksums.insecure=true \\"
  echo "          -Dorg.gradle.internal.http.socketTimeout=60000 \\"
  echo "          -Dorg.gradle.internal.http.connectionTimeout=60000 \\"
  echo "          --warning-mode all \\"
  echo "          -Psigning.gnupg.keyName=XXXXXXXX \\"
  echo "          -PsonatypeUsername=${SONATYPE_USER} \\"
  echo "          -PsonatypePassword=\"password\" \\"
  echo "          clean shadowJar publish"
  echo "./gradlew eclipse"
  echo "./gradlew jmh -Pinclude=\".*PrecompileBenchmark\""
  echo "./gradlew -Dorg.gradle.java.home=\${JAVA_17_ZULU_HOME} clean test\""
  echo "./gradlew -Dorg.gradle.java.home=\${JAVA_11_ZULU_HOME} jmh -Pinclude=\".*PrecompileBenchmark\""
  echo "-------------------------------------------------------------------------"
  echo
}


publish () {
  if [ -z "$1" ]; then
    echo "Please provide the PGP key ID! E.g.: publish 0000AAAA password"
    return
  fi
  if [ -z "$2" ]; then
    echo "Please provide the Sonatype password for user '${SONATYPE_USER}'!  E.g.: publish 0000AAAA password"
    return
  fi

  ./gradlew -Dorg.gradle.internal.publish.checksums.insecure=true \
            -Dorg.gradle.internal.http.socketTimeout=60000 \
            -Dorg.gradle.internal.http.connectionTimeout=60000 \
            --warning-mode all \
            -Psigning.gnupg.keyName=$1 \
            -PsonatypeUsername=${SONATYPE_USER} \
            -PsonatypePassword="$2" \
            clean shadowJar publish
}

start () {
  open ${REPL_HOME}/repl.sh
}

start11 () {
  open ${REPL_HOME}/replJava11.sh
}

start17 () {
  open ${REPL_HOME}/replJava17.sh
}

rebuild () {
  ./gradlew --warning-mode all clean shadowJar
  rm ${REPL_HOME}/libs/venice-*.jar
  cp build/libs/venice-*.jar ${REPL_HOME}/libs
  echo "Starting new REPL..."
  start
}

cheatsheet () {
  ./gradlew cheatsheet
}

tests () {
  ./gradlew clean test
}


export JAVA_HOME=${JAVA_8_ZULU_HOME}
export REPL_HOME=~/Desktop/venice
export WORKSPACE_HOME=~/Documents/workspace-omni/venice

export SONATYPE_USER=jlangch

export -f help
export -f rebuild
export -f start
export -f start11
export -f start17
export -f publish
export -f cheatsheet
export -f tests

cd ${WORKSPACE_HOME}

/bin/sh
