#!/bin/bash

if [ $# != 1 ]; then
  echo "invalid number of args."
  exit 1
fi

echo "execute $(pwd)/$0"

cleanUp() {
  mvn -P $1 clean test
  rm -rf ./tomcat/webapps
  mkdir -p ./tomcat/webapps
  echo "cleanUp() completed."
}

build() {
  mvn -P $1 -DskipTests=true package
  echo "build() completed."
}

deploy() {
  cp ./target/ROOT.war ./tomcat/webapps
  echo "deploy() completed."
}

PROFILES=("develop" "production")

for PROFILE in ${PROFILES[@]}; do
  if [ $PROFILE != $1 ]; then
    continue;
  fi

  cleanUp $1
  build $1
  deploy

  exit 0
done

echo "invalid args."
exit 1
