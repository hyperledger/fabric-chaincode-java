/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.mock.peer;

import java.util.ArrayList;
import java.util.List;
import org.hyperledger.fabric.protos.peer.ChaincodeMessage;

/**
 * Simulates purgePrivateData() invocation in chaincode Waits for PURGE_PRIVATE_DATA message from chaincode and sends
 * back response with empty payload
 */
public final class PurgeValueStep implements ScenarioStep {

    private ChaincodeMessage orgMsg;

    @Override
    public boolean expected(final ChaincodeMessage msg) {
        orgMsg = msg;
        return msg.getType() == ChaincodeMessage.Type.PURGE_PRIVATE_DATA;
    }

    @Override
    public List<ChaincodeMessage> next() {
        final List<ChaincodeMessage> list = new ArrayList<>();
        list.add(ChaincodeMessage.newBuilder()
                .setType(ChaincodeMessage.Type.RESPONSE)
                .setChannelId(orgMsg.getChannelId())
                .setTxid(orgMsg.getTxid())
                .build());
        return list;
    }
}
