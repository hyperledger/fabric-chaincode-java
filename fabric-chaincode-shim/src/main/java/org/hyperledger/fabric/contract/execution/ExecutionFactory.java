/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.execution;

import org.hyperledger.fabric.contract.execution.impl.ContractExecutionService;
import org.hyperledger.fabric.contract.execution.impl.ContractInvocationRequest;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.hyperledger.fabric.shim.ChaincodeStub;

public class ExecutionFactory {
    private static ExecutionFactory rf;
    private static ExecutionService es;

    public static ExecutionFactory getInstance() {
        if (rf == null) {
            rf = new ExecutionFactory();
        }
        return rf;
    }

    public InvocationRequest createRequest(ChaincodeStub context) {
        return new ContractInvocationRequest(context);
    }

    public ExecutionService createExecutionService(TypeRegistry typeRegistry) {
        if (es == null) {
            es = new ContractExecutionService(typeRegistry);
        }
        return es;
    }
}
