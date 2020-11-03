#!/bin/bash
echo ">>  Preparing to setup Channel, and deploy chaincode"

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

if [ "$CHANNEL_NAME" = "sachannel" ]; then
  CC_SRC_PATH="/opt/gopath/src/github.com/hyperledger/fabric/peer/chaincodes/fabric-shim-api"
  CC_NAME="shimcc"
  COLLECTIONS_CFG=$(realpath scripts/collection_config.json)
elif [ "$CHANNEL_NAME" = "sbechannel" ]; then
  CC_SRC_PATH="/opt/gopath/src/github.com/hyperledger/fabric/peer/chaincodes/fabric-ledger-api"
  CC_NAME="ledgercc"
fi

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

echo "Installing chaincode on peer 0, org 1"
installChaincode 0 1
echo "Installing chaincode on peer 0, org 2"
installChaincode 0 2

echo "Instantiating chaincode on peer 0, org 1"
instantiateChaincode 0 1

echo "<< DONE"
# exit 0
