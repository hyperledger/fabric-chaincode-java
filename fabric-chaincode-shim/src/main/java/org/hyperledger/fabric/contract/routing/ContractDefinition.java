/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.routing;

import java.lang.reflect.Method;
import java.util.Collection;

import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;

/**
 * Definition of the Contract
 *
 * A data structure that represents the contract that will be executed in the chaincode.
 * Primarily has
 *
 * Name - either defined by the Contract annotation or the Class name (can be referred to as Namespace)
 * Default - is the default contract (defined by the Default annotation) TxFunctions in this contract do not need the name prefix when invoked
 * TxFunctions - the transaction functions defined in this contract
 *
 * Will embedded the ContgractInterface instance, as well as the annotation itself, and the routing for any tx function that is unkown
 *
 */
public interface ContractDefinition {

	String getName();

	Collection<TxFunction> getTxFunctions();

	ContractInterface getContractImpl();

	TxFunction addTxFunction(Method m);

	boolean isDefault();

	TxFunction getTxFunction(String method);

	boolean hasTxFunction(String method);

	TxFunction.Routing getUnkownRoute();

	Contract getAnnotation();
}
