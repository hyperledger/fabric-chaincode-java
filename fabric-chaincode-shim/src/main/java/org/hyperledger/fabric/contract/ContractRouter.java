/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract;

import org.hyperledger.fabric.contract.execution.ExecutionService;
import org.hyperledger.fabric.contract.routing.ContractScanner;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

/**
 * Router class routes Init/Invoke requests to contracts.
 * Implements {@link org.hyperledger.fabric.shim.Chaincode} interface.
 */
public class ContractRouter extends ChaincodeBase {

    private ContractScanner scanner;
    private ExecutionService executor;

    public ContractRouter() {
    }

    public void findAllContracts() {

    }

    public void startRouting(String[] args) {
        start(args);
    }

    @Override
    public Response init(ChaincodeStub stub) {
        return newErrorResponse();
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        return newErrorResponse();
    }

    public static void main(String[] args) {
        ContractRouter cfc = new ContractRouter();
        cfc.findAllContracts();
        cfc.startRouting(args);
    }
}
