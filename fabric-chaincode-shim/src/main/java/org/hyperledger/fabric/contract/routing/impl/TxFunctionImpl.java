/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class TxFunctionImpl implements TxFunction {
    private static Logger logger = Logger.getLogger(TxFunctionImpl.class);

    private Method method;
    private String name;
    private TransactionType type;
    private Routing routing;
    private TypeSchema returnSchema;
    private List<ParameterDefinition> paramsList = new ArrayList<>();
    private boolean isUnknownTx;

    public class RoutingImpl implements Routing {

        Method method;
        Class<? extends ContractInterface> clazz;

        public RoutingImpl(Method method, Class<? extends ContractInterface> clazz) {
            this.method = method;
            this.clazz = clazz;
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
        public ContractInterface getContractInstance() throws InstantiationException, IllegalAccessException {

            return clazz.newInstance();

        }

        @Override
        public String toString() {
            return method.getName() + ":" + clazz.getCanonicalName();
        }
    }

    /**
     * New TxFunction Definition Impl
     *
     * @param m        Reflect method object
     * @param contract ContractDefinition this is part of
     */
    public TxFunctionImpl(Method m, ContractDefinition contract) {

        this.method = m;
        if (m.getAnnotation(Transaction.class) != null) {
            logger.debug("Found Transaction method: " + m.getName());
            if (m.getAnnotation(Transaction.class).submit()) {
                this.type = TransactionType.INVOKE;
            } else {
                this.type = TransactionType.QUERY;
            }

            String txnName = m.getAnnotation(Transaction.class).name();
            if (!txnName.isEmpty()) {
                this.name = txnName;
            }
        }

        if (name == null) {
            this.name = m.getName();
        }

        this.routing = new RoutingImpl(m, contract.getContractImpl());

        // set the return schema
        this.returnSchema = TypeSchema.typeConvert(m.getReturnType());

        // parameter processing
        List<java.lang.reflect.Parameter> params = new ArrayList<java.lang.reflect.Parameter>(
                Arrays.asList(method.getParameters()));

        // validate the first one is a context object
        if (!Context.class.isAssignableFrom(params.get(0).getType())) {
            throw new ContractRuntimeException(
                    "First argument should be of type Context " + method.getName() + " " + params.get(0).getType());
        } else {

            params.remove(0);
        }

        // FUTURE: if ever the method of creating the instance where to change,
        // the routing could be changed here, a different implementation could be made
        // here encapsulating the change. eg use an annotation to define where the
        // context goes

        for (java.lang.reflect.Parameter parameter : params) {
            TypeSchema paramMap = new TypeSchema();
            TypeSchema schema = TypeSchema.typeConvert(parameter.getType());

            Property annotation = parameter.getAnnotation(org.hyperledger.fabric.contract.annotation.Property.class);
            if (annotation != null) {
                String[] userSupplied = annotation.schema();
                for (int i = 0; i < userSupplied.length; i += 2) {
                    schema.put(userSupplied[i], userSupplied[i + 1]);
                }
            }

            paramMap.put("name", parameter.getName());
            paramMap.put("schema", schema);
            ParameterDefinition pd = new ParameterDefinitionImpl(parameter.getName(), parameter.getClass(), paramMap,
                    parameter);
            paramsList.add(pd);
        }
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
    public java.lang.reflect.Parameter[] getParameters() {
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
    public void setReturnSchema(TypeSchema returnSchema) {
        this.returnSchema = returnSchema;
    }

    @Override
    public List<ParameterDefinition> getParamsList() {
        return paramsList;
    }

    public void setParamsList(ArrayList<ParameterDefinition> paramsList) {
        this.paramsList = paramsList;
    }

    @Override
    public TypeSchema getReturnSchema() {
        return returnSchema;
    }

    @Override
    public void setParameterDefinitions(List<ParameterDefinition> list) {
        this.paramsList = list;

    }

    @Override
    public boolean isUnknownTx() {
        return isUnknownTx;
    }

    @Override
    public void setUnknownTx(boolean unknown) {
        this.isUnknownTx = unknown;
    }

}
