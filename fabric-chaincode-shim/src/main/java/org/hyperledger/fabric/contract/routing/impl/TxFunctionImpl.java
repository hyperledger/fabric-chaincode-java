/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.annotation.Property;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.ContractDefinition;
import org.hyperledger.fabric.contract.routing.ParameterDefinition;
import org.hyperledger.fabric.contract.routing.TransactionType;
import org.hyperledger.fabric.contract.routing.TxFunction;

public final class TxFunctionImpl implements TxFunction {
    private static final Logger LOGGER = Logger.getLogger(TxFunctionImpl.class);

    private final Method method;
    private String name;
    private TransactionType type;
    private final Routing routing;
    private TypeSchema returnSchema;
    private List<ParameterDefinition> paramsList;
    private boolean isUnknownTx;

    public static final class RoutingImpl implements Routing {

        private final Method method;
        private final Class<? extends ContractInterface> clazz;
        private final String serializerName;

        /**
         * @param method
         * @param contract
         */
        public RoutingImpl(final Method method, final ContractDefinition contract) {
            this.method = method;
            this.clazz = contract.getContractImpl();
            this.serializerName = contract.getAnnotation().transactionSerializer();
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Class<? extends ContractInterface> getContractClass() {
            return clazz;
        }

        @Override
        public ContractInterface getContractInstance()
                throws IllegalAccessException, InstantiationException, InvocationTargetException,
                        NoSuchMethodException {
            return clazz.getDeclaredConstructor().newInstance();
        }

        @Override
        public String toString() {
            return method.getName() + ":" + clazz.getCanonicalName();
        }

        @Override
        public String getSerializerName() {
            return serializerName;
        }
    }

    /**
     * New TxFunction Definition Impl.
     *
     * @param m Reflect method object
     * @param contract ContractDefinition this is part of
     */
    public TxFunctionImpl(final Method m, final ContractDefinition contract) {

        this.method = m;
        if (m.getAnnotation(Transaction.class) != null) {
            LOGGER.debug(() -> "Found Transaction method: " + m.getName());
            if (m.getAnnotation(Transaction.class).intent() == Transaction.TYPE.SUBMIT) {
                this.type = TransactionType.SUBMIT;
            } else {
                this.type = TransactionType.EVALUATE;
            }

            final String txnName = m.getAnnotation(Transaction.class).name();
            if (!txnName.isEmpty()) {
                this.name = txnName;
            }
        }

        if (name == null) {
            this.name = m.getName();
        }

        // create the routing object that defines how to get the data to the transaction
        // function.
        this.routing = new RoutingImpl(m, contract);

        // set the return schema
        this.returnSchema = TypeSchema.typeConvert(m.getReturnType());

        // parameter processing
        this.paramsList = buildParameters(m);
    }

    private List<ParameterDefinition> buildParameters(final Method m) {
        Parameter[] params = m.getParameters();
        // validate the first one is a context object
        if (params.length == 0) {
            throw new ContractRuntimeException("First argument should be of type Context");
        }
        if (!Context.class.isAssignableFrom(params[0].getType())) {
            throw new ContractRuntimeException(
                    "First argument should be of type Context " + m.getName() + " " + params[0].getType());
        }

        // FUTURE: if ever the method of creating the instance where to change,
        // the routing could be changed here, a different implementation could be made
        // here encapsulating the change. eg use an annotation to define where the
        // context goes

        return Arrays.stream(params)
                .skip(1)
                .map(TxFunctionImpl::newParameterDefinition)
                .collect(Collectors.toList());
    }

    private static ParameterDefinitionImpl newParameterDefinition(final Parameter parameter) {
        final TypeSchema paramMap = new TypeSchema();
        final TypeSchema schema = TypeSchema.typeConvert(parameter.getType());

        final Property annotation = parameter.getAnnotation(Property.class);
        if (annotation != null) {
            final String[] userSupplied = annotation.schema();
            for (int i = 0; i < userSupplied.length; i += 2) {
                schema.put(userSupplied[i], userSupplied[i + 1]);
            }
        }

        paramMap.put("name", parameter.getName());
        paramMap.put("schema", schema);
        return new ParameterDefinitionImpl(parameter.getName(), parameter.getClass(), paramMap, parameter);
    }

    @Override
    public String getName() {
        return name;
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
    public Parameter[] getParameters() {
        return method.getParameters();
    }

    @Override
    public TransactionType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return name + " @" + Integer.toHexString(System.identityHashCode(this));
    }

    @Override
    public void setReturnSchema(final TypeSchema returnSchema) {
        this.returnSchema = returnSchema;
    }

    @Override
    public List<ParameterDefinition> getParamsList() {
        return paramsList;
    }

    /** @param paramsList */
    public void setParamsList(final List<ParameterDefinition> paramsList) {
        this.paramsList = paramsList;
    }

    @Override
    public TypeSchema getReturnSchema() {
        return returnSchema;
    }

    @Override
    public void setParameterDefinitions(final List<ParameterDefinition> list) {
        this.paramsList = list;
    }

    @Override
    public boolean isUnknownTx() {
        return isUnknownTx;
    }

    @Override
    public void setUnknownTx(final boolean unknown) {
        this.isUnknownTx = unknown;
    }
}
