#!/bin/bash

# common defensive idiom
set -euo pipefail

cleanUp() {
  mvn clean test
  rm -rf ./tomcat/webapps
  mkdir -p ./tomcat/webapps
  echo "🔥 cleanUp() completed."
}

build() {
  mvn -DskipTests=true package
  echo "🌟 build() completed."
}

deploy() {
  cp ./target/ROOT.war ./tomcat/webapps
  echo "🌟 deploy() completed."
}

# Execute pipeline
cleanUp
build
deploy

echo "✅ Done."
