/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract.execution;

import org.hyperledger.fabric.contract.execution.impl.ContractExecutionService;
import org.hyperledger.fabric.contract.execution.impl.ContractInvocationRequest;
import org.hyperledger.fabric.contract.routing.impl.SerializerRegistryImpl;
import org.hyperledger.fabric.shim.ChaincodeStub;

public class ExecutionFactory {
    private static ExecutionFactory rf;

    /** @return ExecutionFactory */
    public static ExecutionFactory getInstance() {
        if (rf == null) {
            rf = new ExecutionFactory();
        }
        return rf;
    }

    /**
     * @param context Chaincode Context
     * @return Invocation request
     */
    public InvocationRequest createRequest(final ChaincodeStub context) {
        return new ContractInvocationRequest(context);
    }

    /**
     * @param serializers Instance of the serializer
     * @return Execution Service
     */
    public ExecutionService createExecutionService(final SerializerRegistryImpl serializers) {
        return new ContractExecutionService(serializers);
    }
}
