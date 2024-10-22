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
import org.hyperledger.fabric.protos.peer.MetaDataKeys;
import org.hyperledger.fabric.protos.peer.StateMetadata;
import org.hyperledger.fabric.protos.peer.StateMetadataResult;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;

/**
 * simulates Handler.getStateMetadata Waits for GET_STATE_METADATA message Returns response message with stored metadata
 */
public final class GetStateMetadata implements ScenarioStep {
    private ChaincodeMessage orgMsg;
    private final byte[] val;

    /** @param sbe StateBasedEndorsement to return as one and only one metadata entry */
    public GetStateMetadata(final StateBasedEndorsement sbe) {
        val = sbe.policy();
    }

    @Override
    public boolean expected(final ChaincodeMessage msg) {
        orgMsg = msg;
        return msg.getType() == ChaincodeMessage.Type.GET_STATE_METADATA;
    }

    @Override
    public List<ChaincodeMessage> next() {
        final List<StateMetadata> entriesList = new ArrayList<>();
        final StateMetadata validationValue = StateMetadata.newBuilder()
                .setMetakey(MetaDataKeys.VALIDATION_PARAMETER.toString())
                .setValue(ByteString.copyFrom(val))
                .build();
        entriesList.add(validationValue);
        final StateMetadataResult stateMetadataResult =
                StateMetadataResult.newBuilder().addAllEntries(entriesList).build();
        final List<ChaincodeMessage> list = new ArrayList<>();
        list.add(ChaincodeMessage.newBuilder()
                .setType(ChaincodeMessage.Type.RESPONSE)
                .setChannelId(orgMsg.getChannelId())
                .setTxid(orgMsg.getTxid())
                .setPayload(stateMetadataResult.toByteString())
                .build());
        return list;
    }
}
