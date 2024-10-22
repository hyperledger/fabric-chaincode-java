/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.mock.peer;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import org.hyperledger.fabric.protos.peer.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.Response;
import org.hyperledger.fabric.shim.Chaincode;

/**
 * Simulates another chaincode invocation Waits for INVOKE_CHAINCODE Sends back RESPONSE message with chaincode response
 * inside
 */
public final class InvokeChaincodeStep implements ScenarioStep {
    private ChaincodeMessage orgMsg;

    @Override
    public boolean expected(final ChaincodeMessage msg) {
        orgMsg = msg;
        return msg.getType() == ChaincodeMessage.Type.INVOKE_CHAINCODE;
    }

    /**
     * @return Chaincode response packed as payload inside COMPLETE message packed as payload inside RESPONSE message
     */
    @Override
    public List<ChaincodeMessage> next() {
        final ByteString chaincodeResponse = Response.newBuilder()
                .setStatus(Chaincode.Response.Status.SUCCESS.getCode())
                .setMessage("OK")
                .build()
                .toByteString();
        final ByteString completePayload = ChaincodeMessage.newBuilder()
                .setType(ChaincodeMessage.Type.COMPLETED)
                .setChannelId(orgMsg.getChannelId())
                .setTxid(orgMsg.getTxid())
                .setPayload(chaincodeResponse)
                .build()
                .toByteString();
        final List<ChaincodeMessage> list = new ArrayList<>();
        list.add(ChaincodeMessage.newBuilder()
                .setType(ChaincodeMessage.Type.RESPONSE)
                .setChannelId(orgMsg.getChannelId())
                .setTxid(orgMsg.getTxid())
                .setPayload(completePayload)
                .build());
        return list;
    }
}
