/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.execution;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.execution.impl.ContractExecutionService;
import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.hyperledger.fabric.contract.routing.impl.TypeRegistryImpl;
import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import contract.SampleContract;

public class ContractExecutionServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @SuppressWarnings("rawtypes")
    @Test
    public void noReturnValue()
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException {
        TypeRegistry typeRegistry = new TypeRegistryImpl();

        ContractExecutionService ces = new ContractExecutionService(typeRegistry);

        ContractInterface contract = spy(new SampleContract());
        TxFunction txFn = mock(TxFunction.class);
        InvocationRequest req = mock(InvocationRequest.class);
        TxFunction.Routing routing = mock(TxFunction.Routing.class);

        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(txFn.getRouting()).thenReturn(routing);
        when(req.getArgs()).thenReturn(new ArrayList() {
        });
        when(routing.getMethod()).thenReturn(SampleContract.class.getMethod("noReturn", new Class[] { Context.class }));
        when(routing.getContractInstance()).thenReturn(contract);
        ces.executeRequest(txFn, req, stub);

        verify(contract).beforeTransaction(any());

    }

    @SuppressWarnings("rawtypes")
    @Test()
    public void failureToInvoke()
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException {
        TypeRegistry typeRegistry = new TypeRegistryImpl();

        ContractExecutionService ces = new ContractExecutionService(typeRegistry);

        ContractInterface contract = spy(new SampleContract());
        TxFunction txFn = mock(TxFunction.class);
        InvocationRequest req = mock(InvocationRequest.class);
        TxFunction.Routing routing = mock(TxFunction.Routing.class);

        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(txFn.getRouting()).thenReturn(routing);
        when(req.getArgs()).thenReturn(new ArrayList() {
        });

//        when(routing.getMethod()).thenThrow(IllegalAccessException.class);
        when(routing.getContractInstance()).thenThrow(IllegalAccessException.class);
        Response resp = ces.executeRequest(txFn, req, stub);
        assertThat(resp.getStatusCode(), equalTo(500));
    }

}
