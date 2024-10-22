/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.mock.peer;

import com.google.protobuf.InvalidProtocolBufferException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.hyperledger.fabric.protos.peer.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.PutState;

/**
 * Simulates putState() invocation in chaincode Waits for PUT_STATE message from chaincode, including value and sends
 * back response with empty payload
 */
public final class PutValueStep implements ScenarioStep {
    private ChaincodeMessage orgMsg;
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
     * Check incoming message If message type is PUT_STATE and payload equal to passed in constructor
     *
     * @param msg message from chaincode
     * @return
     */
    @Override
    public boolean expected(final ChaincodeMessage msg) {
        orgMsg = msg;
        PutState putMsg = null;
        try {
            putMsg = PutState.parseFrom(msg.getPayload());
        } catch (final InvalidProtocolBufferException e) {
            return false;
        }
        return val.equals(new String(putMsg.getValue().toByteArray(), StandardCharsets.UTF_8))
                && msg.getType() == ChaincodeMessage.Type.PUT_STATE;
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
