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
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.routing.ContractDefinition;
import org.hyperledger.fabric.contract.routing.TxFunction;

/**
 * Implementation of the ContractDefinition
 *
 * Contains information about the contract, including transaction functions and
 * unknown transaction routing
 *
 */
public class ContractDefinitionImpl implements ContractDefinition {
    private static Logger logger = Logger.getLogger(ContractDefinitionImpl.class);

    private Map<String, TxFunction> txFunctions = new HashMap<>();
    private String name;
    private boolean isDefault;
    private Class<? extends ContractInterface> contractClz;
    private Contract contractAnnotation;
    private TxFunction unknownTx;

    public ContractDefinitionImpl(Class<? extends ContractInterface> cl) {

        Contract annotation = cl.getAnnotation(Contract.class);
        logger.debug(() -> "Class Contract Annotation: " + annotation);

        String annotationName = annotation.name();

        if (annotationName == null || annotationName.isEmpty()) {
            this.name = cl.getSimpleName();
        } else {
            this.name = annotationName;
        }

        isDefault = (cl.getAnnotation(Default.class) != null);
        contractAnnotation = cl.getAnnotation(Contract.class);
        contractClz = cl;

        try {
            Method m = cl.getMethod("unknownTransaction", new Class<?>[] { Context.class });
            unknownTx = new TxFunctionImpl(m, this);
            unknownTx.setUnknownTx(true);
        } catch (NoSuchMethodException | SecurityException e) {
            ContractRuntimeException cre = new ContractRuntimeException("Failure to find unknownTransaction method", e);
            logger.severe(() -> logger.formatError(cre));
            throw cre;
        }

        logger.info(() -> "Found class: " + cl.getCanonicalName());
        logger.debug(() -> "Namespace: " + this.name);
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
    public Class<? extends ContractInterface> getContractImpl() {
        return contractClz;
    }

    @Override
    public TxFunction addTxFunction(Method m) {
        logger.debug(() -> "Adding method " + m.getName());
        TxFunction txFn = new TxFunctionImpl(m, this);
        TxFunction previousTxnFn = txFunctions.put(txFn.getName(), txFn);
        if (previousTxnFn != null) {
            String message = String.format("Duplicate transaction method %s", previousTxnFn.getName());
            ContractRuntimeException cre = new ContractRuntimeException(message);
            logger.severe(() -> logger.formatError(cre));
            throw cre;
        }
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
    public TxFunction getUnkownRoute() {
        return unknownTx;
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
