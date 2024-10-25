/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.mock.peer;

import java.util.Collections;
import java.util.List;
import org.hyperledger.fabric.protos.peer.ChaincodeMessage;

/** Waits for COMPLETED message, sends nothing back */
public final class CompleteStep implements ScenarioStep {
    @Override
    public boolean expected(final ChaincodeMessage msg) {
        return msg.getType() == ChaincodeMessage.Type.COMPLETED;
    }

    @Override
    public List<ChaincodeMessage> next() {
        return Collections.emptyList();
    }
}
