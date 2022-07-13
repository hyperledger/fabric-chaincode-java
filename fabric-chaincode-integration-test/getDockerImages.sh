#!/bin/bash
#
# Copyright IBM Corp. All Rights Reserved.
#
# SPDX-License-Identifier: Apache-2.0
#
set -euo pipefail

version=${FABRIC_VERSION:-latest}
artifactory_url=hyperledger-fabric.jfrog.io

for image in peer orderer ca tools; do
    artifactory_image="${artifactory_url}/fabric-${image}:amd64-${version}"
    docker pull -q "${artifactory_image}"
    docker tag "${artifactory_image}" "hyperledger/fabric-${image}"
    docker rmi -f "${artifactory_image}" >/dev/null
done

docker pull -q hyperledger/fabric-couchdb
