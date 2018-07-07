/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.mock.peer;

import org.hyperledger.fabric.protos.peer.ChaincodeShim;

import java.util.List;

public interface ScenarioStep {
    /** Validate incoming message from chaincode side
     *
     * @param msg message from chaincode
     * @return is incoming message was expected
     */
    boolean expected(ChaincodeShim.ChaincodeMessage msg);

    /**
     * List of messages send from peer to chaincode as response(s)
     * @return
     */
    List<ChaincodeShim.ChaincodeMessage> next();
}
