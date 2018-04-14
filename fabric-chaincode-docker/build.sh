#!/bin/bash

buildGradle() {
    cd "$1" > /dev/null
    echo "Gradle build"
    ls -l
    gradle build shadowJar
    cp build/libs/chaincode.jar $2
    cd "$SAVED" >/dev/null
}

buildMaven() {
    cd "$1" > /dev/null
    echo "Maven build"
    ls -l
    mvn compile package
    cp target/chaincode.jar  $2
    cd "$SAVED" >/dev/null
}

source /root/.sdkman/bin/sdkman-init.sh

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="build.sh"
APP_BASE_NAME=`basename "$0"`

find /chaincode/input/

set -x

if [ -d "/chaincode/output" ]
then
    rm -rf /chaincode/output/*
else
    mkdir -p /chaincode/output/
fi

if [ -d "${APP_HOME}/chaincode/build/out" ]
then
    rm -rf ${APP_HOME}/chaincode/build/out/*
else
    mkdir -p ${APP_HOME}/chaincode/build/out
fi


if [ -f "/chaincode/input/src/build.gradle" ]
then
    buildGradle /chaincode/input/src/ /chaincode/output/
else
    buildMaven /chaincode/input/src/ /chaincode/output/
fi

set +x