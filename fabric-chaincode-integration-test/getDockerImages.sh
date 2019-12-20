#!/bin/bash -e
set -euo pipefail

echo "======== PULL DOCKER IMAGES ========"

###############################################################
# Pull and Tag the fabric and fabric-ca images from Artifactory
###############################################################
echo "Fetching images from Artifactory"
ARTIFACTORY_URL=hyperledger-fabric.jfrog.io
ORG_NAME="hyperledger"

VERSION=2.0.0
ARCH="amd64"
: ${STABLE_VERSION:=$VERSION-stable}
STABLE_TAG=$ARCH-$STABLE_VERSION
MASTER_TAG=$ARCH-master

echo "---------> STABLE_VERSION:" $STABLE_VERSION

dockerTag() {
  for IMAGE in peer orderer ca tools orderer ccenv; do
    echo "Images: $IMAGE"
    echo
    docker pull $ARTIFACTORY_URL/fabric-$IMAGE:$STABLE_TAG
          if [[ $? != 0 ]]; then
             echo  "FAILED: Docker Pull Failed on $IMAGE"
             exit 1
          fi
    docker tag $ARTIFACTORY_URL/fabric-$IMAGE:$STABLE_TAG $ORG_NAME/fabric-$IMAGE
    docker tag $ARTIFACTORY_URL/fabric-$IMAGE:$STABLE_TAG $ORG_NAME/fabric-$IMAGE:$MASTER_TAG
    docker tag $ARTIFACTORY_URL/fabric-$IMAGE:$STABLE_TAG $ORG_NAME/fabric-$IMAGE:$VERSION
    echo "$ORG_NAME-$IMAGE:$MASTER_TAG"
    echo "Deleting Artifactory docker images: $IMAGE"
    docker rmi -f $ARTIFACTORY_URL/fabric-$IMAGE:$STABLE_TAG
  done
}

dockerTag

echo
docker images | grep "hyperledger*"
echo
