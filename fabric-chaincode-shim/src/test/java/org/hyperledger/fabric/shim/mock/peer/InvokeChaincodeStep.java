/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.mock.peer;

import com.google.protobuf.ByteString;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.protos.peer.ProposalResponsePackage;
import org.hyperledger.fabric.shim.Chaincode;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulates another chaincode invocation
 * Waits for INVOKE_CHAINCODE
 * Sends back RESPONSE message with chaincode response inside
 */
public class InvokeChaincodeStep implements ScenarioStep {
    ChaincodeShim.ChaincodeMessage orgMsg;

    @Override
    public boolean expected(ChaincodeShim.ChaincodeMessage msg) {
        orgMsg = msg;
        return msg.getType() == ChaincodeShim.ChaincodeMessage.Type.INVOKE_CHAINCODE;
    }

    /**
     *
     * @return Chaincode response packed as payload inside COMPLETE message packed as payload inside RESPONSE message
     */
    @Override
    public List<ChaincodeShim.ChaincodeMessage> next() {
        ByteString chaincodeResponse = ProposalResponsePackage.Response.newBuilder()
                .setStatus(Chaincode.Response.Status.SUCCESS.getCode())
                .setMessage("OK")
                .build().toByteString();
        ByteString completePayload = ChaincodeShim.ChaincodeMessage.newBuilder()
                .setType(ChaincodeShim.ChaincodeMessage.Type.COMPLETED)
                .setChannelId(orgMsg.getChannelId())
                .setTxid(orgMsg.getTxid())
                .setPayload(chaincodeResponse)
                .build().toByteString();
        List<ChaincodeShim.ChaincodeMessage> list = new ArrayList<>();
        list.add(ChaincodeShim.ChaincodeMessage.newBuilder()
                .setType(ChaincodeShim.ChaincodeMessage.Type.RESPONSE)
                .setChannelId(orgMsg.getChannelId())
                .setTxid(orgMsg.getTxid())
                .setPayload(completePayload)
                .build());
        return list;
    }
}
