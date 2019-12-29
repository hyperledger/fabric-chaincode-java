/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.mock.peer;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.protos.peer.ChaincodeShim;

import com.google.protobuf.ByteString;

/**
 * Simulates getState Waits for GET_STATE message Returns response message with
 * value as payload
 */
public final class GetValueStep implements ScenarioStep {
    private ChaincodeShim.ChaincodeMessage orgMsg;
    private final String val;

    /**
     *
     * @param val value to return
     */
    public GetValueStep(final String val) {
        this.val = val;
    }

    @Override
    public boolean expected(final ChaincodeShim.ChaincodeMessage msg) {
        orgMsg = msg;
        return msg.getType() == ChaincodeShim.ChaincodeMessage.Type.GET_STATE;
    }

    @Override
    public List<ChaincodeShim.ChaincodeMessage> next() {
        final ByteString getPayload = ByteString.copyFromUtf8(val);
        final List<ChaincodeShim.ChaincodeMessage> list = new ArrayList<>();
        list.add(ChaincodeShim.ChaincodeMessage.newBuilder()
                .setType(ChaincodeShim.ChaincodeMessage.Type.RESPONSE)
                .setChannelId(orgMsg.getChannelId())
                .setTxid(orgMsg.getTxid())
                .setPayload(getPayload)
                .build());
        return list;
    }
}
