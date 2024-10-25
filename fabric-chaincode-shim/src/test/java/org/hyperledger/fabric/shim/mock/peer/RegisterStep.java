/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.mock.peer;

import java.util.ArrayList;
import java.util.List;
import org.hyperledger.fabric.protos.peer.ChaincodeMessage;

/**
 * Simulates chaincode registration after start Waits for REGISTER message from chaincode Sends back pair of messages:
 * REGISTERED and READY
 */
public final class RegisterStep implements ScenarioStep {

    private ChaincodeMessage orgMsg;

    @Override
    public boolean expected(final ChaincodeMessage msg) {
        orgMsg = msg;
        return msg.getType() == ChaincodeMessage.Type.REGISTER;
    }

    @Override
    public List<ChaincodeMessage> next() {
        final List<ChaincodeMessage> list = new ArrayList<>();
        list.add(ChaincodeMessage.newBuilder()
                .setType(ChaincodeMessage.Type.REGISTERED)
                .build());
        list.add(ChaincodeMessage.newBuilder()
                .setType(ChaincodeMessage.Type.READY)
                .build());
        return list;
    }
}
