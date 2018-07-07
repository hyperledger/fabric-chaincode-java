/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.mock.peer;

import com.google.protobuf.ByteString;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulates getState
 * Waits for GET_STATE message
 * Returns response message with value as payload
 */
public class GetValueStep implements ScenarioStep {
    ChaincodeShim.ChaincodeMessage orgMsg;
    String val;

    /**
     *
     * @param val value to return
     */
    public GetValueStep(String val) {
        this.val = val;
    }

    @Override
    public boolean expected(ChaincodeShim.ChaincodeMessage msg) {
        orgMsg = msg;
        return msg.getType() == ChaincodeShim.ChaincodeMessage.Type.GET_STATE;
    }

    @Override
    public List<ChaincodeShim.ChaincodeMessage> next() {
        ByteString getPayload = ByteString.copyFromUtf8(val);
        List<ChaincodeShim.ChaincodeMessage> list = new ArrayList<>();
        list.add(ChaincodeShim.ChaincodeMessage.newBuilder()
                .setType(ChaincodeShim.ChaincodeMessage.Type.RESPONSE)
                .setChannelId(orgMsg.getChannelId())
                .setTxid(orgMsg.getTxid())
                .setPayload(getPayload)
                .build());
        return list;
    }
}
