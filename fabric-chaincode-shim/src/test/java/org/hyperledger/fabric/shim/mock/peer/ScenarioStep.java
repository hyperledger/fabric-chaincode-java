/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.mock.peer;

import java.util.List;
import org.hyperledger.fabric.protos.peer.ChaincodeMessage;

public interface ScenarioStep {
    /**
     * Validate incoming message from chaincode side
     *
     * @param msg message from chaincode
     * @return is incoming message was expected
     */
    boolean expected(ChaincodeMessage msg);

    /**
     * List of messages send from peer to chaincode as response(s)
     *
     * @return
     */
    List<ChaincodeMessage> next();
}
