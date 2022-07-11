#!/bin/sh
#
# Copyright IBM Corp. All Rights Reserved.
#
# SPDX-License-Identifier: Apache-2.0
#
set -ev

PREVIOUS_TAG=$1
NEW_VERSION=$2

: ${PREVIOUS_TAG:?}
: ${NEW_VERSION:?}

echo "## ${NEW_VERSION}" >> CHANGELOG.new
echo "$(date)" >> CHANGELOG.new
echo "" >> CHANGELOG.new
git log ${PREVIOUS_TAG}..HEAD  --oneline | grep -v Merge | sed -e "s/\[\{0,1\}\(FAB[^0-9]*-[0-9]*\)\]\{0,1\}/\[\1\](https:\/\/jira.hyperledger.org\/browse\/\1\)/" -e "s/\([0-9|a-z]*\)/* \[\1\](https:\/\/github.com\/hyperledger\/fabric-chaincode-java\/commit\/\1)/" >> CHANGELOG.new
echo "" >> CHANGELOG.new
cat CHANGELOG.md >> CHANGELOG.new
mv -f CHANGELOG.new CHANGELOG.md

