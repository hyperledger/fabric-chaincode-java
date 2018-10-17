/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.mock.peer;

import com.google.protobuf.InvalidProtocolBufferException;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.protos.peer.TransactionPackage;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;
import org.hyperledger.fabric.shim.ext.sbe.impl.StateBasedEndorsementFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *  * Simulates Handler.putStateMetadata() invocation from chaincode side
 *  * Waits for PUT_STATE_METADATA message from chaincode, including metadata entry with validation metadata and sends back response with empty payload
 */
public class PutStateMetadata implements ScenarioStep {
    ChaincodeShim.ChaincodeMessage orgMsg;
    StateBasedEndorsement val;

    public PutStateMetadata(StateBasedEndorsement sbe) {
        val = sbe;
    }

    /**
     * Check incoming message
     * If message type is PUT_STATE_METADATA and payload match to passed in constructor
     * @param msg message from chaincode
     * @return
     */
    @Override
    public boolean expected(ChaincodeShim.ChaincodeMessage msg) {
        orgMsg = msg;
        ChaincodeShim.PutStateMetadata psm;
        try {
            psm = ChaincodeShim.PutStateMetadata.parseFrom(msg.getPayload());
        } catch (InvalidProtocolBufferException e) {
            return false;
        }
        StateBasedEndorsement msgSbe = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(psm.getMetadata().getValue().toByteArray());
        return msg.getType() == ChaincodeShim.ChaincodeMessage.Type.PUT_STATE_METADATA
                && TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString().equals(psm.getMetadata().getMetakey())
                && (msgSbe.listOrgs().size() == val.listOrgs().size());
    }

    @Override
    public List<ChaincodeShim.ChaincodeMessage> next() {
        List<ChaincodeShim.ChaincodeMessage> list = new ArrayList<>();
        list.add(ChaincodeShim.ChaincodeMessage.newBuilder()
                .setType(ChaincodeShim.ChaincodeMessage.Type.RESPONSE)
                .setChannelId(orgMsg.getChannelId())
                .setTxid(orgMsg.getTxid())
                .build());
        return list;
    }
}
