#!/bin/bash -e
set -o pipefail

echo "======== PULL DOCKER IMAGES ========"

##########################################################
# Pull and Tag the fabric and fabric-ca images from Nexus
##########################################################
echo "Fetching images from Nexus"
NEXUS_URL=nexus3.hyperledger.org:10001
ORG_NAME="hyperledger/fabric"

VERSION=2.0.0
ARCH="amd64"
: ${STABLE_VERSION:=$VERSION-stable}
STABLE_TAG=$ARCH-$STABLE_VERSION
MASTER_TAG=$ARCH-master

echo "---------> STABLE_VERSION:" $STABLE_VERSION

dockerTag() {
  for IMAGES in peer orderer ca tools orderer ccenv; do
    echo "Images: $IMAGES"
    echo
    docker pull $NEXUS_URL/$ORG_NAME-$IMAGES:$STABLE_TAG
          if [ $? != 0 ]; then
             echo  "FAILED: Docker Pull Failed on $IMAGES"
             exit 1
          fi
    docker tag $NEXUS_URL/$ORG_NAME-$IMAGES:$STABLE_TAG $ORG_NAME-$IMAGES
    docker tag $NEXUS_URL/$ORG_NAME-$IMAGES:$STABLE_TAG $ORG_NAME-$IMAGES:$MASTER_TAG
    docker tag $NEXUS_URL/$ORG_NAME-$IMAGES:$STABLE_TAG $ORG_NAME-$IMAGES:$VERSION
    echo "$ORG_NAME-$IMAGES:$MASTER_TAG"
    echo "Deleting Nexus docker images: $IMAGES"
    docker rmi -f $NEXUS_URL/$ORG_NAME-$IMAGES:$STABLE_TAG
  done
}

dockerTag

echo
docker images | grep "hyperledger*"
echo