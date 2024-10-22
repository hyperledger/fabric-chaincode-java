/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract.execution;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import contract.SampleContract;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import org.hyperledger.fabric.contract.ChaincodeStubNaiveImpl;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.annotation.Serializer;
import org.hyperledger.fabric.contract.execution.impl.ContractExecutionService;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.ParameterDefinition;
import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.contract.routing.impl.ParameterDefinitionImpl;
import org.hyperledger.fabric.contract.routing.impl.SerializerRegistryImpl;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.Test;

public final class ContractExecutionServiceTest {
    @Test
    public void noReturnValue()
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException,
                    SecurityException {
        JSONTransactionSerializer jts = new JSONTransactionSerializer();
        SerializerRegistryImpl serializerRegistry = spy(new SerializerRegistryImpl());
        ContractExecutionService ces = new ContractExecutionService(serializerRegistry);

        ContractInterface contract = spy(new SampleContract());
        TxFunction txFn = mock(TxFunction.class);
        InvocationRequest req = mock(InvocationRequest.class);
        TxFunction.Routing routing = mock(TxFunction.Routing.class);

        ChaincodeStub stub = new ChaincodeStubNaiveImpl();

        when(txFn.getRouting()).thenReturn(routing);
        when(req.getArgs()).thenReturn(new ArrayList<byte[]>());
        when(routing.getMethod())
                .thenReturn(SampleContract.class.getMethod("noReturn", new Class<?>[] {Context.class}));
        when(routing.getContractInstance()).thenReturn(contract);
        when(serializerRegistry.getSerializer(any(), any())).thenReturn(jts);
        ces.executeRequest(txFn, req, stub);

        verify(contract).beforeTransaction(any());
    }

    @Test()
    public void failureToInvoke()
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException,
                    SecurityException {
        JSONTransactionSerializer jts = new JSONTransactionSerializer();
        SerializerRegistryImpl serializerRegistry = spy(new SerializerRegistryImpl());
        ContractExecutionService ces = new ContractExecutionService(serializerRegistry);

        spy(new SampleContract());
        TxFunction txFn = mock(TxFunction.class);
        InvocationRequest req = mock(InvocationRequest.class);
        TxFunction.Routing routing = mock(TxFunction.Routing.class);

        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(txFn.getRouting()).thenReturn(routing);
        when(req.getArgs()).thenReturn(new ArrayList<byte[]>() {});

        when(routing.getContractInstance()).thenThrow(IllegalAccessException.class);
        when(routing.toString()).thenReturn("MockMethodName:MockClassName");
        when(serializerRegistry.getSerializer(any(), any())).thenReturn(jts);

        assertThatThrownBy(() -> ces.executeRequest(txFn, req, stub))
                .isInstanceOf(ContractRuntimeException.class)
                .hasMessage("Could not execute contract method: MockMethodName:MockClassName");
    }

    @Test()
    public void invokeWithDifferentSerializers()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        JSONTransactionSerializer defaultSerializer = spy(new JSONTransactionSerializer());
        SerializerInterface customSerializer = mock(SerializerInterface.class);
        SerializerRegistryImpl serializerRegistry = spy(new SerializerRegistryImpl());
        ExecutionService executionService = ExecutionFactory.getInstance().createExecutionService(serializerRegistry);

        TxFunction txFn = mock(TxFunction.class);
        InvocationRequest req = mock(InvocationRequest.class);
        TxFunction.Routing routing = mock(TxFunction.Routing.class);

        TypeSchema ts = TypeSchema.typeConvert(String.class);
        Method method = SampleContract.class.getMethod("t1", Context.class, String.class);
        Parameter[] params = method.getParameters();
        ParameterDefinition pd = new ParameterDefinitionImpl("arg1", String.class, ts, params[1]);

        byte[] arg = "asdf".getBytes();
        ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        ContractInterface contract = spy(new SampleContract());

        when(req.getArgs()).thenReturn(Collections.singletonList(arg));
        when(txFn.getRouting()).thenReturn(routing);
        when(txFn.getParamsList()).thenReturn(Collections.singletonList(pd));
        when(txFn.getReturnSchema()).thenReturn(ts);
        when(routing.getMethod()).thenReturn(method);
        when(routing.getContractInstance()).thenReturn(contract);

        String defaultSerializerName = defaultSerializer.getClass().getCanonicalName();
        String customSerializerName = "customSerializer";

        // execute transaction with the default serializer
        when(routing.getSerializerName()).thenReturn(defaultSerializerName);
        when(serializerRegistry.getSerializer(defaultSerializerName, Serializer.TARGET.TRANSACTION))
                .thenReturn(defaultSerializer);
        executionService.executeRequest(txFn, req, stub);

        // execute transaction with the custom serializer
        when(routing.getSerializerName()).thenReturn(customSerializerName);
        when(serializerRegistry.getSerializer(customSerializerName, Serializer.TARGET.TRANSACTION))
                .thenReturn(customSerializer);
        executionService.executeRequest(txFn, req, stub);

        verify(defaultSerializer).fromBuffer(arg, ts);
        verify(customSerializer).fromBuffer(arg, ts);
    }
}
