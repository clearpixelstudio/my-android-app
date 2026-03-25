#!/usr/bin/env sh
# -----------------------------------------------------------------------
# Placeholder gradlew — replaced by CI before the build runs.
# If you see this message the CI wrapper-regeneration step was skipped.
# -----------------------------------------------------------------------
set -e
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
JAR="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$JAR" ]; then
  echo "ERROR: gradle-wrapper.jar not found at $JAR"
  echo "The CI must run the Regenerate Gradle wrapper step before building."
  exit 1
fi
exec "${JAVA_HOME:-java}" -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
