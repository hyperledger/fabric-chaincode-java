#!/bin/bash
#
# SPDX-License-Identifier: Apache-2.0
#

# Exit on first error, print all commands.
set -e
set -o pipefail
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

function abort {
	echo "!! Exiting shell script"
	echo "!!" "$1"
	exit -1
}

VERSION=$(cat build.gradle | sed -n "s/version = '\(.*\)'/\1/p")
VERSION=${VERSION// }
echo Version is :${VERSION}:

NEW_VERSION=$(npx semver --increment "${VERSION}" )-SNAPSHOT
echo New version is :${NEW_VERSION}:


# Remove the snapshot from the main build.gradle
for GRADLE_FILE in "${DIR}/fabric-chaincode-example-sbe/build.gradle" "${DIR}/fabric-contract-example/gradle/build.gradle" "${DIR}/fabric-chaincode-example-gradle/build.gradle" "${DIR}/fabric-chaincode-example-sacc/build.gradle"
do
  sed -i "s/\(.*fabric-chaincode-shim.*version:\).*/\1 '${NEW_VERSION}'" "${GRADLE_FILE}"
done

for MAVEN_FILE in "${DIR}/fabric-chaincode-example-maven/pom.xml"  "${DIR}/fabric-contract-example/maven/pom.xml"
do
  sed -i "s/<fabric-chaincode-java.version>\(.*\)<\/fabric-chaincode-java.version>/<fabric-chaincode-java.version>${NEW_VERSION}<\/fabric-chaincode-java.version>/" "${MAVEN_FILE}"
done

sed -i "s/version = '.*'/version = '${NEW_VERSION}'/" build.gradle

echo "...done"



