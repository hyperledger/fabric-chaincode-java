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
 * Simulate last query (close) step. Happens after passing over all query result Waits for QUERY_STATE_CLOSE Sends back
 * response with empty payload
 */
public final class QueryCloseStep implements ScenarioStep {
    private ChaincodeMessage orgMsg;

    @Override
    public boolean expected(final ChaincodeMessage msg) {
        orgMsg = msg;
        return msg.getType() == ChaincodeMessage.Type.QUERY_STATE_CLOSE;
    }

    /** @return RESPONSE message with empty payload */
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
