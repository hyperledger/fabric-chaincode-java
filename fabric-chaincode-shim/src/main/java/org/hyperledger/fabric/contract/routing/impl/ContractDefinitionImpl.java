/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.routing.ContractDefinition;
import org.hyperledger.fabric.contract.routing.TxFunction;

/**
 * Implementation of the ContractDefinition
 *
 * Contains information about the contract, including transaction functions and unknown transaction routing
 *
 */
public class ContractDefinitionImpl implements ContractDefinition {
	private static Logger logger = Logger.getLogger(ContractDefinitionImpl.class);

	private Map<String, TxFunction> txFunctions = new HashMap<>();
	private String name;
	private boolean isDefault;
	private ContractInterface contract;
	private Contract contractAnnotation;
	private TxFunction unknownTx;

	public ContractDefinitionImpl(Class<?> cl)  {

		Contract annotation = cl.getAnnotation(Contract.class);
		logger.debug(()->"Class Contract Annodation: "+annotation);

		String annotationName = annotation.namespace();
		if (annotationName == null || annotationName.isEmpty()) {
			this.name = cl.getSimpleName();
		} else {
			this.name = annotationName;
		}

		isDefault = (cl.getAnnotation(Default.class) != null);
		contractAnnotation = cl.getAnnotation(Contract.class);
		try {
			contract = (ContractInterface) cl.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			ContractRuntimeException cre = new ContractRuntimeException("Unable to create instance of contract",e);
			logger.error(()->logger.formatError(cre));
			throw cre;
		}

		try {
			Method m = cl.getMethod("unknownTransaction", new Class<?>[] {});
			unknownTx = new TxFunctionImpl(m,this);
		} catch (NoSuchMethodException | SecurityException e) {
			ContractRuntimeException cre = new ContractRuntimeException("Failure to find unknownTranction method",e);
			logger.severe(()->logger.formatError(cre));
			throw cre;
		}

		logger.info(()->"Found class: " + cl.getCanonicalName());
		logger.debug(()->"Namespace: " + this.name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<TxFunction> getTxFunctions() {
		return txFunctions.values();
	}

	@Override
	public ContractInterface getContractImpl() {
		return contract;
	}

	@Override
	public TxFunction addTxFunction(Method m) {
		logger.debug(()->"Adding method " + m.getName());
		TxFunction txFn = new TxFunctionImpl(m, this);
		txFunctions.put(txFn.getName(), txFn);
		return txFn;
	}

	@Override
	public boolean isDefault() {
		return isDefault;
	}

	@Override
	public TxFunction getTxFunction(String method) {
		return txFunctions.get(method);
	}

	@Override
	public boolean hasTxFunction(String method) {
		return txFunctions.containsKey(method);
	}

	@Override
	public TxFunction.Routing getUnkownRoute() {
		return unknownTx.getRouting();
	}

	@Override
	public Contract getAnnotation() {
		return this.contractAnnotation;
	}

	@Override
	public String toString() {
		return name + ":" + txFunctions + " @" + Integer.toHexString(System.identityHashCode(this));
	}
}
