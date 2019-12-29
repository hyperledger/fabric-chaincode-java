/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract.execution;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.hyperledger.fabric.contract.ChaincodeStubNaiveImpl;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.execution.impl.ContractExecutionService;
import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import contract.SampleContract;

public class ContractExecutionServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @SuppressWarnings({  "serial" })
    @Test
    public void noReturnValue()
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {

        JSONTransactionSerializer jts = new JSONTransactionSerializer();

        ContractExecutionService ces = new ContractExecutionService(jts);

        ContractInterface contract = spy(new SampleContract());
        TxFunction txFn = mock(TxFunction.class);
        InvocationRequest req = mock(InvocationRequest.class);
        TxFunction.Routing routing = mock(TxFunction.Routing.class);

        ChaincodeStub stub = new ChaincodeStubNaiveImpl();

        when(txFn.getRouting()).thenReturn(routing);
        when(req.getArgs()).thenReturn(new ArrayList<byte[]>());
        when(routing.getMethod()).thenReturn(SampleContract.class.getMethod("noReturn", new Class<?>[] {Context.class}));
        when(routing.getContractInstance()).thenReturn(contract);
        ces.executeRequest(txFn, req, stub);

        verify(contract).beforeTransaction(any());

    }

    @SuppressWarnings({ "serial" })
    @Test()
    public void failureToInvoke()
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {

        JSONTransactionSerializer jts = new JSONTransactionSerializer();
        ContractExecutionService ces = new ContractExecutionService(jts);

        spy(new SampleContract());
        TxFunction txFn = mock(TxFunction.class);
        InvocationRequest req = mock(InvocationRequest.class);
        TxFunction.Routing routing = mock(TxFunction.Routing.class);

        ChaincodeStub stub = mock(ChaincodeStub.class);


        when(txFn.getRouting()).thenReturn(routing);
        when(req.getArgs()).thenReturn(new ArrayList<byte[]>() {
        });

        when(routing.getContractInstance()).thenThrow(IllegalAccessException.class);
        when(routing.toString()).thenReturn("MockMethodName:MockClassName");

        thrown.expect(ContractRuntimeException.class);
        thrown.expectMessage("Could not execute contract method: MockMethodName:MockClassName");

        Response resp = ces.executeRequest(txFn, req, stub);
        assertThat(resp.getStatusCode(), equalTo(500));
    }

}
