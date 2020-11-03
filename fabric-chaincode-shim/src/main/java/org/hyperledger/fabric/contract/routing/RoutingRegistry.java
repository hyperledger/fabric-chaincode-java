/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing;

import java.util.Collection;

import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.execution.InvocationRequest;

public interface RoutingRegistry {

    /**
     * Add a new contract definition based on the class object located.
     *
     * @param clz Class Object to process into a ContractDefinition
     * @return ContractDefinition Instance
     */
    ContractDefinition addNewContract(Class<ContractInterface> clz);

    /**
     * Based on the Invocation Request, can we create a route for this?
     *
     * @param request
     * @return ture/false
     */
    boolean containsRoute(InvocationRequest request);

    /**
     * Get the route for invocation request.
     *
     * @param request
     * @return Routing obect
     */
    TxFunction.Routing getRoute(InvocationRequest request);

    /**
     * Get the txFunction that matches the routing request.
     *
     * @param request
     * @return Transaction Function
     */
    TxFunction getTxFn(InvocationRequest request);

    /**
     * Get the contract that matches the supplied name.
     *
     * @param name
     * @return Contract Definition
     */
    ContractDefinition getContract(String name);

    /**
     * Returns all the ContractDefinitions for this registry.
     *
     * @return Collection of all definitions
     */
    Collection<ContractDefinition> getAllDefinitions();

    /**
     * Locate all the contracts in this chaincode.
     *
     * @param typeRegistry
     */
    void findAndSetContracts(TypeRegistry typeRegistry);

}
