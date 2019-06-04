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
 * A data structure that represents the contract that will be executed in the
 * chaincode. Primarily has
 *
 * Name - either defined by the Contract annotation or the Class name (can be
 * referred to as Namespace) Default - is the default contract (defined by the
 * Default annotation) TxFunctions in this contract do not need the name prefix
 * when invoked TxFunctions - the transaction functions defined in this contract
 *
 * Will embedded the ContgractInterface instance, as well as the annotation
 * itself, and the routing for any tx function that is unkown
 *
 */
public interface ContractDefinition {

	/**
	 * @return the fully qualififed name of the Contract
	 */
	String getName();

	/**
	 * @return Complete collection of all the transaction functions in this contract
	 */
	Collection<TxFunction> getTxFunctions();

	/**
	 * @return Object reference to the instantiated object that is 'the contract'
	 */
	ContractInterface getContractImpl();

	/**
	 * @param m The java.lang.reflect object that is the method that is a tx
	 *          function
	 * @return TxFunction object representing this method
	 */
	TxFunction addTxFunction(Method m);

	/**
	 *
	 * @return if this is contract is the default one or not
	 */
	boolean isDefault();

	/**
	 *
	 * @param method name to returned
	 * @return TxFunction that represent this requested method
	 */
	TxFunction getTxFunction(String method);

	/**
	 *
	 * @param method name to be checked
	 * @return true if this txFunction exists or not
	 */
	boolean hasTxFunction(String method);

	/**
	 * @return The TxFunction to be used for this contract in case of unknown
	 *         request
	 */
	TxFunction getUnkownRoute();

	/**
	 * @return Underlying raw annotation
	 */
	Contract getAnnotation();
}
