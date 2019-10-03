#!/bin/bash

echo
echo "Install Instantiate the SBE Chaincode"
echo
CHANNEL_NAME="$1"
DELAY="$2"
LANGUAGE="$3"
TIMEOUT="$4"
VERBOSE="$5"
: ${CHANNEL_NAME:="mychannel"}
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

CC_SRC_PATH="/opt/gopath/src/github.com/hyperledger/fabric/peer/chaincodes/sbe"
CC_NAME="sbecc"
COLLECTIONS_CFG=$(realpath scripts/collection_config.json)

echo "Installing chaincode on peer 0, org 1"
installChaincode 0 1
echo "Installing chaincode on peer 0, org 2"
installChaincode 0 2

echo "Instantiating chaincode on peer 0, org 1"
instantiateChaincodeSBE 0 1


# exit 0
