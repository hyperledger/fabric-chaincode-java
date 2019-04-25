/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing;

import java.lang.reflect.Method;

import org.hyperledger.fabric.contract.ContractInterface;

public interface TxFunction {

	interface Routing {
	    ContractInterface getContractObject();

	    Method getMethod();

	    Class getContractClass();

	}

	String getName();

    Routing getRouting();

	Class<?> getReturnType();

	java.lang.reflect.Parameter[] getParameters();

	TransactionType getType();

}
