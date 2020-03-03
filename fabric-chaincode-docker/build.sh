#!/usr/bin/env bash
set -ex

INPUT_DIR=/chaincode/input
OUTPUT_DIR=/chaincode/output
TMP_DIR=$(mktemp -d)
JARS=$(find ${INPUT_DIR} -name ".jar" | paste -s -d ":" -)
NUM_JARS=$(find ${INPUT_DIR} -name "*.jar" | wc -l)

buildGradle() {
    echo "Copying from $1 to ${TMP_DIR}"
    cd $1
    tar cf - . | (cd ${TMP_DIR}; tar xf -)
    cd ${TMP_DIR}
    echo "Gradle build"
    if [ -f ./gradlew ]; then
      chmod +x ./gradlew
      ./gradlew build shadowJar
    else
      gradle build shadowJar
    fi
    retval=$?
    if [ $retval -ne 0 ]; then
      exit $retval
    fi
    cp build/libs/chaincode.jar $2
    retval=$?
    if [ $retval -ne 0 ]; then
      exit $retval
    fi
    touch $2/.uberjar
    cd "$SAVED" >/dev/null
}

buildMaven() {
    echo "Copying from $1 to ${TMP_DIR}"
    cd $1
    tar cf - . | (cd ${TMP_DIR}; tar xf -)
    cd ${TMP_DIR}
    echo "Maven build"
    mvn compile package
    retval=$?
    if [ $retval -ne 0 ]; then
      exit $retval
    fi
    cp target/chaincode.jar $2
    retval=$?
    if [ $retval -ne 0 ]; then
      exit $retval
    fi
    touch $2/.uberjar
    cd "$SAVED" >/dev/null
}

for DIR in ${INPUT_DIR} ${INPUT_DIR}/src; do
    if [ -f ${DIR}/build.gradle -o -f ${DIR}/build.gradle.kts ]; then
        buildGradle ${DIR} ${OUTPUT_DIR}
        exit 0
    elif [ -f ${DIR}/pom.xml ]; then
        buildMaven ${DIR} ${OUTPUT_DIR}
        exit 0
    fi
done

if [ ${NUM_JARS} -eq 0 ]; then
    >&2 echo "Not build.gradle nor pom.xml found in chaincode source, don't know how to build chaincode"
    >&2 echo "Project folder content:"
    >&2 find ${INPUT_DIR} -name "*" -exec ls -ld '{}' \;
    exit 255
else
    cd ${INPUT_DIR} && tar cf - $(find . -name "*.jar") | (cd ${OUTPUT_DIR} && tar xvf -)
fi