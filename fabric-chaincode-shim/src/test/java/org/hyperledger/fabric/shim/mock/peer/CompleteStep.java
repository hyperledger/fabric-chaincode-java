/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.mock.peer;

import java.util.Collections;
import java.util.List;

import org.hyperledger.fabric.protos.peer.ChaincodeShim;

/**
 * Waits for COMPLETED message, sends nothing back
 */
public final class CompleteStep implements ScenarioStep {
    @Override
    public boolean expected(final ChaincodeShim.ChaincodeMessage msg) {
        return msg.getType() == ChaincodeShim.ChaincodeMessage.Type.COMPLETED;
    }

    @Override
    public List<ChaincodeShim.ChaincodeMessage> next() {
        return Collections.emptyList();
    }
}
