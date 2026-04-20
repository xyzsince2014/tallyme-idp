#!/bin/bash

# common defensive idiom
set -euo pipefail

if [ $# -gt 1 ]; then
  echo "⚠️ Invalid number of args. Usage: $0 [profile]"
  exit 1
fi

# default profile = develop
TARGET_PROFILE=${1:-develop}
echo "🚀 Build with profile: ${TARGET_PROFILE}"

cleanUp() {
  mvn -P $1 clean test
  rm -rf ./tomcat/webapps
  mkdir -p ./tomcat/webapps
  echo "🔥 cleanUp() completed."
}

build() {
  mvn -P $1 -DskipTests=true package
  echo "🌟 build() completed."
}

deploy() {
  cp ./target/ROOT.war ./tomcat/webapps
  echo "🌟 deploy() completed."
}

# execute with validation on PROFILE
PROFILES=("develop" "production")
for PROFILE in "${PROFILES[@]}"; do
  if [ "$PROFILE" == "$TARGET_PROFILE" ]; then
    cleanUp "$TARGET_PROFILE"
    build "$TARGET_PROFILE"
    deploy
    echo "✅ Done."
    exit 0
  fi
done

echo "🛑 Invalid profile: ${TARGET_PROFILE}. Allowed profiles: ${PROFILES[*]}"
exit 1
