/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract.execution.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.annotation.Serializer;
import org.hyperledger.fabric.contract.execution.ExecutionService;
import org.hyperledger.fabric.contract.execution.InvocationRequest;
import org.hyperledger.fabric.contract.execution.SerializerInterface;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.ParameterDefinition;
import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.contract.routing.impl.SerializerRegistryImpl;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;

public class ContractExecutionService implements ExecutionService {

    private static final Logger LOGGER = Logger.getLogger(ContractExecutionService.class.getName());

    private final SerializerRegistryImpl serializers;

    /** @param serializers */
    public ContractExecutionService(final SerializerRegistryImpl serializers) {
        this.serializers = serializers;
    }

    /** */
    @Override
    public Chaincode.Response executeRequest(
            final TxFunction txFn, final InvocationRequest req, final ChaincodeStub stub) {
        LOGGER.fine(() -> "Routing Request" + txFn);
        final TxFunction.Routing rd = txFn.getRouting();
        Chaincode.Response response;

        try {
            final ContractInterface contractObject = rd.getContractInstance();
            final Context context = contractObject.createContext(stub);

            final List<Object> args = convertArgs(req.getArgs(), txFn);
            args.add(0, context); // force context into 1st position, other elements move up

            contractObject.beforeTransaction(context);
            final Object value = rd.getMethod().invoke(contractObject, args.toArray());
            contractObject.afterTransaction(context, value);

            if (value == null) {
                response = ResponseUtils.newSuccessResponse();
            } else {
                response = ResponseUtils.newSuccessResponse(convertReturn(value, txFn));
            }

        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            final String message = String.format("Could not execute contract method: %s", rd.toString());
            throw new ContractRuntimeException(message, e);
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();

            if (cause instanceof ChaincodeException) {
                response = ResponseUtils.newErrorResponse(cause);
            } else {
                throw new ContractRuntimeException("Error during contract method execution", cause);
            }
        }

        return response;
    }

    private byte[] convertReturn(final Object obj, final TxFunction txFn) {
        final SerializerInterface serializer =
                serializers.getSerializer(txFn.getRouting().getSerializerName(), Serializer.TARGET.TRANSACTION);
        final TypeSchema ts = txFn.getReturnSchema();
        return serializer.toBuffer(obj, ts);
    }

    private List<Object> convertArgs(final List<byte[]> stubArgs, final TxFunction txFn) {
        final SerializerInterface serializer =
                serializers.getSerializer(txFn.getRouting().getSerializerName(), Serializer.TARGET.TRANSACTION);
        final List<ParameterDefinition> schemaParams = txFn.getParamsList();
        final List<Object> args = new ArrayList<>(stubArgs.size() + 1); // allow for context as the first argument
        for (int i = 0; i < schemaParams.size(); i++) {
            args.add(
                    i,
                    serializer.fromBuffer(stubArgs.get(i), schemaParams.get(i).getSchema()));
        }
        return args;
    }
}
