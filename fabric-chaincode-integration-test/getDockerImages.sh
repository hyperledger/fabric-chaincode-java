#!/bin/bash -e
set -euo pipefail

echo "======== PULL DOCKER IMAGES ========"

###############################################################
# Pull and Tag the fabric and fabric-ca images from Artifactory
###############################################################
echo "Fetching images from Artifactory"
ARTIFACTORY_URL=hyperledger-fabric.jfrog.io
ORG_NAME="hyperledger"
VERSION=2.1
ARCH=amd64
STABLE_TAG=$VERSION-stable

dockerTag() {
	for IMAGE in peer orderer ca tools ccenv; do
		docker pull $ARTIFACTORY_URL/fabric-$IMAGE:$ARCH-$STABLE_TAG
		docker tag $ARTIFACTORY_URL/fabric-$IMAGE:$ARCH-$STABLE_TAG $ORG_NAME/fabric-$IMAGE
		docker tag $ARTIFACTORY_URL/fabric-$IMAGE:$ARCH-$STABLE_TAG $ORG_NAME/fabric-$IMAGE:$VERSION
	done
}

dockerTag

echo
docker images | grep "hyperledger*"
echo
