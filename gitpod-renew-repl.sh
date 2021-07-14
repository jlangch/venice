# -----------------------------------------------------------------------------
# Renews the Gitpod REPL
# -----------------------------------------------------------------------------

if [ `pwd` = "/workspace/venice" ]; then
  ./gradlew --warning-mode all clean shadowJar

  rm -f ../repl/libs/venice-*.jar

  cp ./build/libs/venice-*.jar ../repl/libs

  echo "The new Venice JAR is ready to be used"
else
  echo "This script is not running within a Gitpod instance!"
fi
