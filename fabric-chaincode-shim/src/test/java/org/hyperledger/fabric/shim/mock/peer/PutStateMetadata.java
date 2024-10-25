/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.mock.peer;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.List;
import org.hyperledger.fabric.protos.peer.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.MetaDataKeys;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;
import org.hyperledger.fabric.shim.ext.sbe.impl.StateBasedEndorsementFactory;

/**
 * * Simulates Handler.putStateMetadata() invocation from chaincode side * Waits for PUT_STATE_METADATA message from
 * chaincode, including metadata entry with validation metadata and sends back response with empty payload
 */
public final class PutStateMetadata implements ScenarioStep {
    private ChaincodeMessage orgMsg;
    private final StateBasedEndorsement val;

    public PutStateMetadata(final StateBasedEndorsement sbe) {
        val = sbe;
    }

    /**
     * Check incoming message If message type is PUT_STATE_METADATA and payload match to passed in constructor
     *
     * @param msg message from chaincode
     * @return
     */
    @Override
    public boolean expected(final ChaincodeMessage msg) {
        orgMsg = msg;
        org.hyperledger.fabric.protos.peer.PutStateMetadata psm;
        try {
            psm = org.hyperledger.fabric.protos.peer.PutStateMetadata.parseFrom(msg.getPayload());
        } catch (final InvalidProtocolBufferException e) {
            return false;
        }
        final StateBasedEndorsement msgSbe = StateBasedEndorsementFactory.getInstance()
                .newStateBasedEndorsement(psm.getMetadata().getValue().toByteArray());
        return msg.getType() == ChaincodeMessage.Type.PUT_STATE_METADATA
                && MetaDataKeys.VALIDATION_PARAMETER
                        .toString()
                        .equals(psm.getMetadata().getMetakey())
                && (msgSbe.listOrgs().size() == val.listOrgs().size());
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
