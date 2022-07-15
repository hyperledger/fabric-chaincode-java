#!/bin/bash
#
# SPDX-License-Identifier: Apache-2.0
#

# Exit on first error, print all commands.
set -e
set -o pipefail
WORKSPACE="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

function abort {
    echo "!! Exiting shell script"
    echo "!!" "$1"
    exit -1
}

for binary in shim protos; do
echo "Pushing fabric-chaincode-$binary.$PUSH_VERSION.jar to maven releases.."
cp $WORKSPACE/fabric-chaincode-$binary/build/libs/fabric-chaincode-$binary-$PUSH_VERSION.jar $WORKSPACE/fabric-chaincode-$binary/build/libs/fabric-chaincode-$binary.$PUSH_VERSION.jar
mvn org.apache.maven.plugins:maven-deploy-plugin:deploy-file \
    -DupdateReleaseInfo=true \
    -Dfile=$WORKSPACE/fabric-chaincode-$binary/build/libs/fabric-chaincode-$binary.$PUSH_VERSION.jar \
    -DpomFile=$WORKSPACE/fabric-chaincode-$binary/build/publications/"$binary"Jar/pom-default.xml \
    -DrepositoryId=hyperledger-releases \
    -Durl=https://nexus.hyperledger.org/content/repositories/releases/ \
    -DgroupId=org.hyperledger.fabric-chaincode-java \
    -Dversion=$PUSH_VERSION \
    -DartifactId=fabric-chaincode-$binary \
    -DgeneratePom=false \
    -DuniqueVersion=false \
    -Dpackaging=jar \
    -gs $GLOBAL_SETTINGS_FILE -s $SETTINGS_FILE
done