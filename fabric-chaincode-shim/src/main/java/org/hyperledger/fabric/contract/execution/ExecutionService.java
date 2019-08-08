/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.execution;

import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.contract.routing.TxFunction.Routing;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;

/**
 * Service that executes {@link InvocationRequest} (wrapped Init/Invoke + extra data) using routing information {@link Routing}
 */
public interface ExecutionService {

    Chaincode.Response executeRequest(TxFunction txFn, InvocationRequest req, ChaincodeStub stub);
}
