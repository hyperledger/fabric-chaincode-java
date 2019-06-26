/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
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
        ContractRouter r = new ContractRouter(new String[] { "-a", "127.0.0.1:7052", "-i", "testId" });
        r.findAllContracts();
        ChaincodeStub s = new ChaincodeStubNaiveImpl();

        // Test Transaction routing
        List<String> args = new ArrayList<>();
        args.add("samplecontract:t1");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);
        InvocationRequest request = ExecutionFactory.getInstance().createRequest(s);
        assertThat(request.getNamespace(), is(equalTo(SampleContract.class.getAnnotation(Contract.class).name())));
        assertThat(request.getMethod(), is(equalTo("t1")));
        assertThat(request.getRequestName(), is(equalTo("samplecontract:t1")));
        assertThat(request.getArgs(), is(contains(s.getArgs().get(1))));
    }

    @Test
    public void testInit() {
        ContractRouter r = new ContractRouter(new String[] { "-a", "127.0.0.1:7052", "-i", "testId" });
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

        Chaincode.Response response = r.init(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.SUCCESS));
        assertThat(response.getStringPayload(), is(equalTo("asdf")));
        assertThat(SampleContract.beforeInvoked, is(1));
        assertThat(SampleContract.afterInvoked, is(1));
        assertThat(SampleContract.doWorkInvoked, is(1));
        assertThat(SampleContract.t1Invoked, is(1));
    }

    @Test
    public void testInvokeTxnThatExists() {
        ContractRouter r = new ContractRouter(new String[] { "-a", "127.0.0.1:7052", "-i", "testId" });
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
        assertThat(response.getStatus(), is(Chaincode.Response.Status.SUCCESS));
        assertThat(response.getStringPayload(), is(equalTo("asdf")));
        assertThat(SampleContract.beforeInvoked, is(1));
        assertThat(SampleContract.afterInvoked, is(1));
        assertThat(SampleContract.doWorkInvoked, is(1));
        assertThat(SampleContract.t1Invoked, is(1));
    }

    @Test
    public void testInvokeTxnWithDefinedName() {
        ContractRouter r = new ContractRouter(new String[] { "-a", "127.0.0.1:7052", "-i", "testId" });
        r.findAllContracts();
        ChaincodeStub s = new ChaincodeStubNaiveImpl();

        List<String> args = new ArrayList<>();
        args.add("samplecontract:t4");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.beforeInvoked = 0;
        SampleContract.afterInvoked = 0;
        SampleContract.doWorkInvoked = 0;
        SampleContract.t1Invoked = 0;

        Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.SUCCESS));
        assertThat(response.getStringPayload(), is(equalTo("Transaction 4")));
        assertThat(SampleContract.beforeInvoked, is(1));
        assertThat(SampleContract.afterInvoked, is(1));
        assertThat(SampleContract.doWorkInvoked, is(0));
        assertThat(SampleContract.t1Invoked, is(0));
    }

    @Test
    public void testInvokeTxnWithDefinedNameUsingMethodName() {
        ContractRouter r = new ContractRouter(new String[] { "-a", "127.0.0.1:7052", "-i", "testId" });
        r.findAllContracts();
        ChaincodeStub s = new ChaincodeStubNaiveImpl();

        List<String> args = new ArrayList<>();
        args.add("samplecontract:tFour");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.beforeInvoked = 0;
        SampleContract.afterInvoked = 0;
        SampleContract.doWorkInvoked = 0;
        SampleContract.t1Invoked = 0;

        Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.INTERNAL_SERVER_ERROR));
        assertThat(response.getStringPayload(), is(startsWith("java.lang.IllegalStateException")));
        assertThat(SampleContract.beforeInvoked, is(1));
        assertThat(SampleContract.afterInvoked, is(0));
        assertThat(SampleContract.doWorkInvoked, is(0));
        assertThat(SampleContract.t1Invoked, is(0));
    }

    @Test
    public void testInvokeTxnThatDoesNotExist() {
        ContractRouter r = new ContractRouter(new String[] { "-a", "127.0.0.1:7052", "-i", "testId" });
        r.findAllContracts();
        ChaincodeStub s = new ChaincodeStubNaiveImpl();

        List<String> args = new ArrayList<>();
        args.add("samplecontract:notsupposedtoexist");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.beforeInvoked = 0;
        SampleContract.afterInvoked = 0;
        SampleContract.doWorkInvoked = 0;
        SampleContract.t1Invoked = 0;

        Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.INTERNAL_SERVER_ERROR));
        assertThat(response.getStringPayload(), is(startsWith("java.lang.IllegalStateException")));
        assertThat(SampleContract.beforeInvoked, is(1));
        assertThat(SampleContract.afterInvoked, is(0));
        assertThat(SampleContract.doWorkInvoked, is(0));
        assertThat(SampleContract.t1Invoked, is(0));
    }

    @Test
    public void testInvokeTxnThatThrowsAnException() {
        ContractRouter r = new ContractRouter(new String[] { "-a", "127.0.0.1:7052", "-i", "testId" });
        r.findAllContracts();
        ChaincodeStub s = new ChaincodeStubNaiveImpl();

        List<String> args = new ArrayList<>();
        args.add("samplecontract:t3");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.beforeInvoked = 0;
        SampleContract.afterInvoked = 0;
        SampleContract.doWorkInvoked = 0;

        Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.INTERNAL_SERVER_ERROR));
        assertThat(response.getStringPayload(), is(startsWith("java.lang.RuntimeException: T3 fail!")));
        assertThat(SampleContract.beforeInvoked, is(1));
        assertThat(SampleContract.afterInvoked, is(0));
        assertThat(SampleContract.doWorkInvoked, is(0));
    }

    @Test
    public void exceptions() {
        ContractRuntimeException cre1 = new ContractRuntimeException("failure");
        ContractRuntimeException cre2 = new ContractRuntimeException("another failure", cre1);
        ContractRuntimeException cre3 = new ContractRuntimeException(new Exception("cause"));
    }
}
