#!/bin/bash
# Exit on first error, print all commands.
set -e
set -o pipefail
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

function abort {
	echo "!! Exiting shell script"
	echo "!!" "$1"
	exit -1
}

VERSION=$(cat build.gradle | sed -n "s/version = '\(.*\)-SNAPSHOT'/\1/p")
echo Version is ${VERSION}

# Remove the snapshot from the main build.gradle
for GRADLE_FILE in "${DIR}/build.gradle" "${DIR}/fabric-chaincode-example-sbe/build.gradle" "${DIR}/fabric-contract-example/gradle/build.gradle" "${DIR}/fabric-chaincode-example-gradle/build.gradle" "${DIR}/fabric-chaincode-example-sacc/build.gradle"
do
  sed -i "s/version:\(.*\)-SNAPSHOT/version:\1/" "${GRADLE_FILE}"
done

for MAVEN_FILE in "${DIR}/build.gradle" "${DIR}/fabric-chaincode-example-maven/pom.xml"  "${DIR}/fabric-contract-example/maven/pom.xml"
do
  sed -i "s/<fabric-chaincode-java.version>\(.*\)-SNAPSHOT/<fabric-chaincode-java.version>\1/" "${MAVEN_FILE}"
done

if [[ -f "${DIR}/release_notes/v${VERSION// }.txt" ]]; then
    echo "Release notes exist, hope they make sense!"
else
    abort "No releases notes under the file ${DIR}/release_notes/v${VERSION// }.txt exist";
fi

OLD_VERSION=$(cat ./CHANGELOG.md | sed -n 1p | sed -n -e "s/.*v\(.*\)/\1/p")
echo Previous version is v${OLD_VERSION}

echo "Writing change log..."
"${DIR}/scripts/changelog.sh" "v${OLD_VERSION}" "v${VERSION}"
echo "...done"



