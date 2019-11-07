/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.execution;

import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;

/**
 * Service that executes {@link InvocationRequest} (wrapped Init/Invoke + extra
 * data) using routing information {@link Routing}.
 */
public interface ExecutionService {

    /**
     *
     * @param txFn Transaction Function to executre
     * @param req  Details of the request
     * @param stub Chaincode Stub instance to use
     * @return Chaincode.Response protobuf
     */
    Chaincode.Response executeRequest(TxFunction txFn, InvocationRequest req, ChaincodeStub stub);
}
