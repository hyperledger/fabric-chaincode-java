#!/bin/bash
echo ">>  Preparing to setup Channel, and deploy chaincode"

CHANNEL_NAME="$1"
DELAY="$2"
LANGUAGE="$3"
TIMEOUT="$4"
VERBOSE="$5"
: ${CHANNEL_NAME:="testchannel"}
: ${DELAY:="10"}
: ${LANGUAGE:="java"}
: ${TIMEOUT:="10"}
: ${VERBOSE:="false"}
LANGUAGE=`echo "$LANGUAGE" | tr [:upper:] [:lower:]`
COUNTER=1
MAX_RETRY=10
echo "Channel name : "$CHANNEL_NAME

# import utils
. scripts/utils.sh

## Create channel
echo "Creating channel..."
createChannel

## Join all the peers to the channel
echo "Having all peers join the channel..."
joinChannel

## Set the anchor peers for each org in the channel
echo "Updating anchor peers for org1..."
updateAnchorPeers 0 1
echo "Updating anchor peers for org2..."
updateAnchorPeers 0 2

## There are several chaincodes that need packaging and installing
## Currently using the old lifecyle
CC_ROOT_PATH_IN_DOCKER="/opt/gopath/src/github.com/hyperledger/fabric/peer/chaincodes"
CC_SRC_PATH="${CC_ROOT_PATH_IN_DOCKER}/fabric-shim-api"
CC_NAME="shimcc"
COLLECTIONS_CFG=$(realpath scripts/collection_config.json)

echo "Installing chaincode on peer 0, org 1"
installChaincode 0 1
echo "Installing chaincode on peer 0, org 2"
installChaincode 0 2
echo "Instantiating chaincode on peer 0, org 1"
instantiateChaincode 0 1

#
CC_SRC_PATH="${CC_ROOT_PATH_IN_DOCKER}/fabric-ledger-api"
CC_NAME="ledgercc"
unset COLLECTIONS_CFG

echo "Installing chaincode on peer 0, org 1"
installChaincode 0 1
echo "Installing chaincode on peer 0, org 2"
installChaincode 0 2
echo "Instantiating chaincode on peer 0, org 1"
instantiateChaincode 0 1

#
CC_SRC_PATH="${CC_ROOT_PATH_IN_DOCKER}/bare-gradle"
CC_NAME="baregradlecc"
unset COLLECTIONS_CFG

echo "Installing chaincode on peer 0, org 1"
installChaincode 0 1
echo "Installing chaincode on peer 0, org 2"
installChaincode 0 2
echo "Instantiating chaincode"
instantiateChaincode 0 1

#
CC_SRC_PATH="${CC_ROOT_PATH_IN_DOCKER}/bare-maven"
CC_NAME="baremaven"
unset COLLECTIONS_CFG

echo "Installing chaincode on peer 0, org 1"
installChaincode 0 1
echo "Installing chaincode on peer 0, org 2"
installChaincode 0 2
echo "Instantiating chaincode"
instantiateChaincode 0 1

#
CC_SRC_PATH="${CC_ROOT_PATH_IN_DOCKER}/wrapper-maven"
CC_NAME="wrappermaven"
unset COLLECTIONS_CFG

echo "Installing chaincode on peer 0, org 1"
installChaincode 0 1
echo "Installing chaincode on peer 0, org 2"
installChaincode 0 2
echo "Instantiating chaincode"
instantiateChaincode 0 1

echo "<< DONE"
# exit 0
