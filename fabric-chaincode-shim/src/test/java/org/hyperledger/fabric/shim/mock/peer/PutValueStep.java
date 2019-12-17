/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.mock.peer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.protos.peer.ChaincodeShim;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Simulates putState() invocation in chaincode Waits for PUT_STATE message from
 * chaincode, including value and sends back response with empty payload
 */
public final class PutValueStep implements ScenarioStep {
    private ChaincodeShim.ChaincodeMessage orgMsg;
    private final String val;

    /**
     * Initiate step
     *
     * @param val
     */
    public PutValueStep(final String val) {
        this.val = val;
    }

    /**
     * Check incoming message If message type is PUT_STATE and payload equal to
     * passed in constructor
     *
     * @param msg message from chaincode
     * @return
     */
    @Override
    public boolean expected(final ChaincodeShim.ChaincodeMessage msg) {
        orgMsg = msg;
        ChaincodeShim.PutState putMsg = null;
        try {
            putMsg = ChaincodeShim.PutState.parseFrom(msg.getPayload());
        } catch (final InvalidProtocolBufferException e) {
            return false;
        }
        return val.equals(new String(putMsg.getValue().toByteArray(), StandardCharsets.UTF_8))
                && msg.getType() == ChaincodeShim.ChaincodeMessage.Type.PUT_STATE;
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
