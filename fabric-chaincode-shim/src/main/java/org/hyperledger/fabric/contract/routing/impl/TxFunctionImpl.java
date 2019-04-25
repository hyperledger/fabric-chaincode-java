/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing.impl;

import java.lang.reflect.Method;

import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Init;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.routing.ContractDefinition;
import org.hyperledger.fabric.contract.routing.TransactionType;
import org.hyperledger.fabric.contract.routing.TxFunction;

public class TxFunctionImpl implements TxFunction {
	private static Logger logger = Logger.getLogger(TxFunctionImpl.class);

	private Method method;
	private ContractDefinition contract;
	private TransactionType type;
	private Routing routing;

	public class RoutingImpl implements Routing {
	    ContractInterface contract;
	    Method method;
	    Class clazz;


	    public RoutingImpl(Method method, ContractInterface contract) {
	    	this.method = method;
	    	this.contract = contract;
	        clazz = contract.getClass();
	    }

	    @Override
	    public ContractInterface getContractObject() {
	        return contract;
	    }

	    @Override
	    public Method getMethod() {
	        return method;
	    }

	    @Override
	    public Class getContractClass() {
	        return clazz;
	    }

	    @Override
	    public String toString() {
	    	return method.getName()+":"+clazz.getCanonicalName()+":"+contract.getClass().getCanonicalName();
	    }
	}

	/**
	 * New TxFunction Definition Impl
	 *
	 * @param m   Reflect method object
	 * @param contract   ContractDefinition this is part of
	 */
	public TxFunctionImpl(Method m, ContractDefinition contract) {

        this.method = m;
        this.contract = contract;

        if (m.getAnnotation(Transaction.class) != null) {
            logger.debug("Found Transaction method: " + m.getName());
            if (m.getAnnotation(Transaction.class).submit()) {
                this.type = TransactionType.INVOKE;
            } else {
                this.type = TransactionType.QUERY;
            }

        }
        if (m.getAnnotation(Init.class) != null) {
            this.type = TransactionType.INIT;
            logger.debug(()-> "Found Init method: " + m.getName());
        }

        this.routing = new RoutingImpl(m,contract.getContractImpl());

	}

	@Override
	public String getName() {
		return this.method.getName();
	}

	@Override
	public Routing getRouting() {
		return this.routing;
	}

	@Override
	public Class<?> getReturnType() {
		return method.getReturnType();
	}


	@Override
	public java.lang.reflect.Parameter[] getParameters() {
		return method.getParameters();
	}

	@Override
	public TransactionType getType() {
		return this.type;
	}

	@Override
	public String toString() {
		return this.method.getName() + " @" + Integer.toHexString(System.identityHashCode(this));
	}



}
