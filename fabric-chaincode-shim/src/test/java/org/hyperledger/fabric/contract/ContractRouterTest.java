/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.execution.ExecutionFactory;
import org.hyperledger.fabric.contract.execution.InvocationRequest;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import contract.SampleContract;
public class ContractRouterTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void testCreateAndScan() {
        ContractRouter r = new ContractRouter(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        ChaincodeStub s = new ChaincodeStubNaiveImpl();

        // Test Init routing
        List<String> args = new ArrayList<>();
        args.add("samplecontract:i1");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);
        InvocationRequest request = ExecutionFactory.getInstance().createRequest(s);
        assertThat(request.getNamespace(), is(equalTo(SampleContract.class.getAnnotation(Contract.class).namespace())));
        assertThat(request.getMethod(), is(equalTo("i1")));
        assertThat(request.getRequestName(), is(equalTo("samplecontract:i1")));
        assertThat(request.getArgs(), is(empty()));
        org.hyperledger.fabric.contract.routing.TxFunction.Routing routing = r.getRouting(request);
        assertThat(routing.getContractClass().getName(), is(equalTo(SampleContract.class.getName())));
        assertThat(routing.getMethod().getName(), is(equalTo("i1")));
//

        // Test Transaction routing
        args = new ArrayList<>();
        args.add("samplecontract:t1");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);
        request = ExecutionFactory.getInstance().createRequest(s);
        assertThat(request.getNamespace(), is(equalTo(SampleContract.class.getAnnotation(Contract.class).namespace())));
        assertThat(request.getMethod(), is(equalTo("t1")));
        assertThat(request.getRequestName(), is(equalTo("samplecontract:t1")));
        assertThat(request.getArgs(), is(contains(s.getArgs().get(1))));
        routing = r.getRouting(request);
        assertThat(routing.getContractClass().getName(), is(equalTo(SampleContract.class.getName())));
        assertThat(routing.getMethod().getName(), is(equalTo("t1")));

    }

    @Test
    public void testInit() {
        ContractRouter r = new ContractRouter(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        ChaincodeStub s = new ChaincodeStubNaiveImpl();

        List<String> args = new ArrayList<>();
        args.add("samplecontract:i1");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.beforeInvoked = 0;
        SampleContract.afterInvoked = 0;
        SampleContract.i1Invoked = 0;
        Chaincode.Response response = r.init(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStringPayload(), is(equalTo("Init done")));
        assertThat(SampleContract.beforeInvoked, is(1));
        assertThat(SampleContract.afterInvoked, is(1));
        assertThat(SampleContract.i1Invoked, is(1));

        args.set(0, "samplecontract:i2");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        response = r.init(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.INTERNAL_SERVER_ERROR));

        args.set(0, "samplecontract:t1");
        args.add("arg text");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);
        response = r.init(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.SUCCESS));
    }

    @Test
    public void testInvoke() {
        ContractRouter r = new ContractRouter(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        ChaincodeStub s = new ChaincodeStubNaiveImpl();

        List<String> args = new ArrayList<>();
        args.add("samplecontract:t1");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.beforeInvoked = 0;
        SampleContract.afterInvoked = 0;
        SampleContract.doWorkInvoked = 0;
        SampleContract.t1Invoked = 0;

        Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStringPayload(), is(equalTo("asdf")));
        assertThat(SampleContract.beforeInvoked, is(1));
        assertThat(SampleContract.afterInvoked, is(1));
        assertThat(SampleContract.doWorkInvoked, is(1));
        assertThat(SampleContract.t1Invoked, is(1));

        args.set(0, "samplecontract:notsupposedtoexist");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.INTERNAL_SERVER_ERROR));

        args.set(0, "samplecontract:i1");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);
        response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.SUCCESS));
    }

    @Test
    public void exceptions() {
    	ContractRuntimeException cre1 = new ContractRuntimeException("failure");
    	ContractRuntimeException cre2 = new ContractRuntimeException("another failure",cre1);
    	ContractRuntimeException cre3 = new ContractRuntimeException(new Exception("cause"));
    }
}
