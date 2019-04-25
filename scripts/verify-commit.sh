#!/bin/bash -ue

#
# SPDX-License-Identifier: Apache-2.0
##############################################################################
# Copyright (c) 2018 IBM Corporation, The Linux Foundation and others.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Apache License 2.0
# which accompanies this distribution, and is available at
# https://www.apache.org/licenses/LICENSE-2.0
##############################################################################

# This script makes several basic commit message validations.
# This is with the purpose of keeping up with the aesthetics of our code.
# Verify if the commit message contains JIRA URLs.
# its-jira pluggin attempts to process jira links and breaks.

set +ue # Temporarily ignore any errors

set -o pipefail
echo "----> verify-commit.sh"

if git rev-list --format=%B --max-count=1 HEAD | grep -io 'http[s]*://jira\..*' > /dev/null ; then
    echo 'Error: Remove JIRA URLs from commit message'
    echo 'Add jira references as: Issue: <JIRAKEY>-<ISSUE#>, instead of URLs'
    exit 1
fi

# Check for trailing white-space (tab or spaces) in any files that were changed
#commit_files=$(git diff-tree --name-only -r HEAD~2..HEAD)
commit_files=$(find ./fabric-chaincode-shim/src -name *.java)

found_trailing=false
for filename in $commit_files; do
    if [[ $(file -b $filename) == "ASCII text"* ]]; then
        if egrep -q "\s$" $filename; then
            found_trailing=true
            echo "Error: Trailing white spaces found in file: $filename"
        fi
    fi
done

#if $found_trailing; then
#    echo "####  filename:line-num:line  ####"
#    egrep -n  "\s$" $commit_files
#    exit 1
#fi