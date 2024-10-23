#!/bin/bash
set -x

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}" )"/.. && pwd )"
export CFG="${DIR}/_cfg"
mkdir -p "${CFG}"


# ---
# Setup up Microfab image
# using the IBM tagged version until labs workflow is updated
docker rm -f microfab || true

export MICROFAB_CONFIG='{"couchdb":false,"endorsing_organizations":[{"name":"org1"},{"name":"org2"}],"channels":[{"name":"sachannel","endorsing_organizations":["org1","org2"]}],"capability_level":"V2_5"}'

docker run  --name microfab \
            -d \
            -p 8080:8080 \
            --add-host host.docker.internal:host-gateway \
            --rm \
            -e MICROFAB_CONFIG="${MICROFAB_CONFIG}" \
            -e FABRIC_LOGGING_SPEC=info \
            ghcr.io/hyperledger-labs/microfab


sleep 10

curl -sSL http://console.localho.st:8080/ak/api/v1/components > $CFG/cfg.json 
npx @hyperledger-labs/weft microfab -w $CFG/_wallets -p $CFG/_gateways -m $CFG/_msp -f --config $CFG/cfg.json

# bring in the helper bash scripts
. $DIR/scripts/ccutils.sh


function deployCC() {
## package the chaincode
packageChaincode

## Install chaincode on peer0.org1 and peer0.org2
infoln "Installing chaincode on peer0.org1..."
installChaincode 1
infoln "Install chaincode on peer0.org2..."
installChaincode 2

## query whether the chaincode is installed
queryInstalled 1

## approve the definition for org1
approveForMyOrg 1

## check whether the chaincode definition is ready to be committed
## expect org1 to have approved and org2 not to
checkCommitReadiness 1 "\"org1MSP\": true" "\"org2MSP\": false"
checkCommitReadiness 2 "\"org1MSP\": true" "\"org2MSP\": false"

## now approve also for org2
approveForMyOrg 2

## check whether the chaincode definition is ready to be committed
## expect them both to have approved
checkCommitReadiness 1 "\"org1MSP\": true" "\"org2MSP\": true"
checkCommitReadiness 2 "\"org1MSP\": true" "\"org2MSP\": true"

## now that we know for sure both orgs have approved, commit the definition
commitChaincodeDefinition 1 2

## query on both orgs to see that the definition committed successfully
queryCommitted 1
queryCommitted 2
}
#./gradlew -I ./chaincode-init.gradle  -PchaincodeRepoDir=$(realpath ./fabric-chaincode-integration-test/src/contracts/fabric-ledger-api/repository) publishShimPublicationToFabricRepository

export CC_SRC_PATH="${DIR}/../../contracts/fabric-ledger-api"
export CC_NAME="ledgercc"
export CC_RUNTIME_LANGUAGE=java
export CC_VERSION=1
export CC_SEQUENCE=1
export CHANNEL_NAME=sachannel
export COLLECTIONS_CFG=""
deployCC

export CC_SRC_PATH="${DIR}/../../contracts/bare-gradle"
export CC_NAME="baregradlecc"
deployCC

export CC_SRC_PATH="${DIR}/../../contracts/bare-maven"
export CC_NAME="baremaven"
deployCC

export CC_SRC_PATH="${DIR}/../../contracts/wrapper-maven"
export CC_NAME="wrappermaven"
deployCC

export COLLECTIONS_CFG="--collections-config ${CFG}/../scripts/collection_config.json"
export CC_SRC_PATH="${DIR}/../../contracts/fabric-shim-api"
export CC_NAME="shimcc"
deployCC
