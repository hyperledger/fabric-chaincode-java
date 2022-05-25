/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.mock.peer;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.protos.peer.ChaincodeShim;

/**
 * Simulates purgePrivateData() invocation in chaincode Waits for PURGE_PRIVATE_DATA message from
 * chaincode and sends back response with empty payload
 */
public final class PurgeValueStep implements ScenarioStep {
    private ChaincodeShim.ChaincodeMessage orgMsg;

    @Override
    public boolean expected(final ChaincodeShim.ChaincodeMessage msg) {
        orgMsg = msg;
        return msg.getType() == ChaincodeShim.ChaincodeMessage.Type.PURGE_PRIVATE_DATA;
    }

    @Override
    public List<ChaincodeShim.ChaincodeMessage> next() {
        final List<ChaincodeShim.ChaincodeMessage> list = new ArrayList<>();
        list.add(ChaincodeShim.ChaincodeMessage.newBuilder()
                .setType(ChaincodeShim.ChaincodeMessage.Type.RESPONSE)
                .setChannelId(orgMsg.getChannelId())
                .setTxid(orgMsg.getTxid())
                .build());
        return list;
    }
}
