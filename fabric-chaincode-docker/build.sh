#!/bin/bash

TMP_DIR=$(mktemp -d)

buildGradle() {
    echo "Copying from $1 to ${TMP_DIR}"
    cd $1
    tar cf - . | (cd ${TMP_DIR}; tar xf -)
    cd ${TMP_DIR}
    echo "Gradle build"
    gradle build shadowJar -x test
    retval=$?
    if [ $retval -ne 0 ]; then
      exit $retval
    fi
    cp build/libs/chaincode.jar $2
    retval=$?
    if [ $retval -ne 0 ]; then
      exit $retval
    fi
    cd "$SAVED" >/dev/null
}

buildMaven() {
    echo "Copying from $1 to ${TMP_DIR}"
    cd $1
    tar cf - . | (cd ${TMP_DIR}; tar xf -)
    cd ${TMP_DIR}
    echo "Maven build"
    mvn -B compile package -DskipTests -Dmaven.test.skip=true
    retval=$?
    if [ $retval -ne 0 ]; then
      exit $retval
    fi
    cp target/chaincode.jar $2
    retval=$?
    if [ $retval -ne 0 ]; then
      exit $retval
    fi
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
elif [ -f "/chaincode/input/build.gradle" ]
then
    buildGradle /chaincode/input/ /chaincode/output/
elif [ -f "/chaincode/input/src/pom.xml" ]
then
    buildMaven /chaincode/input/src/ /chaincode/output/
elif [ -f "/chaincode/input/pom.xml" ]
then
    buildMaven /chaincode/input/ /chaincode/output/
else
    >&2 echo "Not build.gralde nor pom.xml found in chaincode source, don't know how to build chaincode"
    >&2 echo "Project folder content:"
    >&2 find /chaincode/input/src/ -name "*" -exec ls -ld '{}' \;
    exit 255
fi
