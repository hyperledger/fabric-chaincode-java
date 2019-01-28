/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.routing;

import org.hyperledger.fabric.contract.execution.InvocationRequest;

/**
 * Scan and keep all chaincode requests -> contract routing information
 */
public interface ContractScanner {

    /**
     * Scan classpath for all contracts and build routing information for all contracts
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    void findAndSetContracts() throws IllegalAccessException, InstantiationException;

    /**
     * Get routing information {@link Routing} based on info from {@link InvocationRequest}
     * @param req
     * @return
     */
    Routing getRouting(InvocationRequest req);

    /**
     * In case no specific {@link Routing}, get default {@link Routing}
     * @param req
     * @return
     */
    Routing getDefaultRouting(InvocationRequest req);
}
