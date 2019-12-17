/*
 * Copyright 2019 IBM DTCC All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract.execution;

import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;

/**
 * ExecutionService.
 *
 * Service that executes {@link InvocationRequest} (wrapped Init/Invoke + extra
 * data) using routing information
 */
public interface ExecutionService {

    /**
     *
     * @param txFn
     * @param req
     * @param stub
     * @return Chaincode response
     */
    Chaincode.Response executeRequest(TxFunction txFn, InvocationRequest req, ChaincodeStub stub);
}
