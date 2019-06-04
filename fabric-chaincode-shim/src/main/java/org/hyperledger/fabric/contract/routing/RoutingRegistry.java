/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing;

import java.util.Collection;

import org.hyperledger.fabric.contract.execution.InvocationRequest;

public interface RoutingRegistry {

	/**
	 * Add a new contract definition based on the class object located
	 *
	 * @param clz Class Object to process into a ContractDefinition
	 * @return ContractDefinition Instance
	 */
	ContractDefinition addNewContract(Class<?> clz);

	/**
	 * Based on the Invocation Request, can we create a route for this?
	 *
	 * @param request
	 * @return
	 */
	boolean containsRoute(InvocationRequest request);

	/**
	 * Get the route for invocation request
	 *
	 * @param request
	 * @return
	 */
	TxFunction.Routing getRoute(InvocationRequest request);

	/**
	 * Get the txFunction that matches the routing request
	 *
	 * @param request
	 * @return
	 */
	TxFunction getTxFn(InvocationRequest request);

	/**
	 * Get the contract that matches the supplied name
	 *
	 * @param name
	 * @return
	 */
	ContractDefinition getContract(String name);

	/**
	 * Returns all the ContractDefinitions for this registry
	 *
	 * @return
	 */
	Collection<ContractDefinition> getAllDefinitions();

	/**
	 * Locate all the contracts in this chaincode
	 *
	 * @param typeRegistry
	 */
	void findAndSetContracts(TypeRegistry typeRegistry);

}