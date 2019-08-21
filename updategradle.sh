#!/bin/bash
set -ev

GRADLE_FILE=/home/matthew/github.com/caliper-benchmarks/src/fabric/api/fixed-asset/java/build.gradle
GATEWAYIP=$(docker network inspect fabricvscodelocalfabric_basic | jq -r '.[0].IPAM.Config[0].Gateway' )
echo ${GATEWAYIP}

sed -i "s/http:\/\/.*:8000/http:\/\/${GATEWAYIP}:8000/" "${GRADLE_FILE}"
