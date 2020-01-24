/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.mock.peer;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.protos.peer.TransactionPackage;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;

import com.google.protobuf.ByteString;

/**
 * simulates Handler.getStateMetadata Waits for GET_STATE_METADATA message
 * Returns response message with stored metadata
 */
public final class GetStateMetadata implements ScenarioStep {
    private ChaincodeShim.ChaincodeMessage orgMsg;
    private final byte[] val;

    /**
     * @param sbe StateBasedEndorsement to return as one and only one metadata entry
     */
    public GetStateMetadata(final StateBasedEndorsement sbe) {
        val = sbe.policy();
    }

    @Override
    public boolean expected(final ChaincodeShim.ChaincodeMessage msg) {
        orgMsg = msg;
        return msg.getType() == ChaincodeShim.ChaincodeMessage.Type.GET_STATE_METADATA;
    }

    @Override
    public List<ChaincodeShim.ChaincodeMessage> next() {
        final List<ChaincodeShim.StateMetadata> entriesList = new ArrayList<>();
        final ChaincodeShim.StateMetadata validationValue = ChaincodeShim.StateMetadata.newBuilder()
                .setMetakey(TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString())
                .setValue(ByteString.copyFrom(val))
                .build();
        entriesList.add(validationValue);
        final ChaincodeShim.StateMetadataResult stateMetadataResult = ChaincodeShim.StateMetadataResult.newBuilder()
                .addAllEntries(entriesList)
                .build();
        final List<ChaincodeShim.ChaincodeMessage> list = new ArrayList<>();
        list.add(ChaincodeShim.ChaincodeMessage.newBuilder()
                .setType(ChaincodeShim.ChaincodeMessage.Type.RESPONSE)
                .setChannelId(orgMsg.getChannelId())
                .setTxid(orgMsg.getTxid())
                .setPayload(stateMetadataResult.toByteString())
                .build());
        return list;
    }
}
