/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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
        final ContractRouter r = new ContractRouter(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        final ChaincodeStub s = new ChaincodeStubNaiveImpl();

        // Test Transaction routing
        final List<String> args = new ArrayList<>();
        args.add("samplecontract:t1");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);
        final InvocationRequest request = ExecutionFactory.getInstance().createRequest(s);
        assertThat(request.getNamespace(), is(equalTo(SampleContract.class.getAnnotation(Contract.class).name())));
        assertThat(request.getMethod(), is(equalTo("t1")));
        assertThat(request.getRequestName(), is(equalTo("samplecontract:t1")));
        assertThat(request.getArgs(), is(contains(s.getArgs().get(1))));
    }

    @Test
    public void testInit() {
        final ContractRouter r = new ContractRouter(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        final ChaincodeStub s = new ChaincodeStubNaiveImpl();

        final List<String> args = new ArrayList<>();
        args.add("samplecontract:t1");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.setBeforeInvoked(0);
        SampleContract.setAfterInvoked(0);
        SampleContract.setDoWorkInvoked(0);
        SampleContract.setT1Invoked(0);

        final Chaincode.Response response = r.init(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.SUCCESS));
        assertThat(response.getMessage(), is(nullValue()));
        assertThat(response.getStringPayload(), is(equalTo("asdf")));
        assertThat(SampleContract.getBeforeInvoked(), is(1));
        assertThat(SampleContract.getAfterInvoked(), is(1));
        assertThat(SampleContract.getDoWorkInvoked(), is(1));
        assertThat(SampleContract.getT1Invoked(), is(1));
    }

    /**
     * Test invoking two transaction functions in a contract via fully qualified
     * name
     */
    @Test
    public void testInvokeTwoTxnsThatExist() {
        final ContractRouter r = new ContractRouter(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        final ChaincodeStub s = new ChaincodeStubNaiveImpl();

        final List<String> args = new ArrayList<>();
        args.add("samplecontract:t1");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.setBeforeInvoked(0);
        SampleContract.setAfterInvoked(0);
        SampleContract.setDoWorkInvoked(0);
        SampleContract.setT1Invoked(0);

        final Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.SUCCESS));
        assertThat(response.getMessage(), is(nullValue()));
        assertThat(response.getStringPayload(), is(equalTo("asdf")));
        assertThat(SampleContract.getBeforeInvoked(), is(1));
        assertThat(SampleContract.getAfterInvoked(), is(1));
        assertThat(SampleContract.getDoWorkInvoked(), is(1));
        assertThat(SampleContract.getT1Invoked(), is(1));

        args.clear();
        args.add("samplecontract:t5");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.setBeforeInvoked(0);
        SampleContract.setAfterInvoked(0);
        SampleContract.setDoWorkInvoked(0);
        SampleContract.setT1Invoked(0);

        final Chaincode.Response secondResponse = r.invoke(s);
        assertThat(secondResponse, is(notNullValue()));
        assertThat(secondResponse.getStatus(), is(Chaincode.Response.Status.SUCCESS));
        assertThat(secondResponse.getMessage(), is(nullValue()));
        assertThat(secondResponse.getStringPayload(), is(nullValue()));
        assertThat(SampleContract.getBeforeInvoked(), is(1));
        assertThat(SampleContract.getAfterInvoked(), is(1));
        assertThat(SampleContract.getDoWorkInvoked(), is(1));
        assertThat(SampleContract.getT1Invoked(), is(0));
    }

    @Test
    public void testInvokeTxnWithDefinedName() {
        final ContractRouter r = new ContractRouter(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        final ChaincodeStub s = new ChaincodeStubNaiveImpl();

        final List<String> args = new ArrayList<>();
        args.add("samplecontract:t4");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.setBeforeInvoked(0);
        SampleContract.setAfterInvoked(0);
        SampleContract.setDoWorkInvoked(0);
        SampleContract.setT1Invoked(0);

        final Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.SUCCESS));
        assertThat(response.getMessage(), is(nullValue()));
        assertThat(response.getStringPayload(), is(equalTo("Transaction 4")));
        assertThat(SampleContract.getBeforeInvoked(), is(1));
        assertThat(SampleContract.getAfterInvoked(), is(1));
        assertThat(SampleContract.getDoWorkInvoked(), is(0));
        assertThat(SampleContract.getT1Invoked(), is(0));
    }

    /**
     * Test invoking two transaction functions in a contract via default name name
     */
    @Test
    public void testInvokeTwoTxnsWithDefaultNamespace() {
        final ContractRouter r = new ContractRouter(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        final ChaincodeStub s = new ChaincodeStubNaiveImpl();

        final List<String> args = new ArrayList<>();
        args.add("t1");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.setBeforeInvoked(0);
        SampleContract.setAfterInvoked(0);
        SampleContract.setDoWorkInvoked(0);
        SampleContract.setT1Invoked(0);

        final Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.SUCCESS));
        assertThat(response.getMessage(), is(nullValue()));
        assertThat(response.getStringPayload(), is(equalTo("asdf")));
        assertThat(SampleContract.getBeforeInvoked(), is(1));
        assertThat(SampleContract.getAfterInvoked(), is(1));
        assertThat(SampleContract.getDoWorkInvoked(), is(1));
        assertThat(SampleContract.getT1Invoked(), is(1));

        args.clear();
        args.add("t5");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.setBeforeInvoked(0);
        SampleContract.setAfterInvoked(0);
        SampleContract.setDoWorkInvoked(0);
        SampleContract.setT1Invoked(0);

        final Chaincode.Response secondResponse = r.invoke(s);
        assertThat(secondResponse, is(notNullValue()));
        assertThat(secondResponse.getStatus(), is(Chaincode.Response.Status.SUCCESS));
        assertThat(secondResponse.getMessage(), is(nullValue()));
        assertThat(secondResponse.getStringPayload(), is(nullValue()));
        assertThat(SampleContract.getBeforeInvoked(), is(1));
        assertThat(SampleContract.getAfterInvoked(), is(1));
        assertThat(SampleContract.getDoWorkInvoked(), is(1));
        assertThat(SampleContract.getT1Invoked(), is(0));
    }

    @Test
    public void testInvokeTxnWithDefinedNameUsingMethodName() {
        final ContractRouter r = new ContractRouter(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        final ChaincodeStub s = new ChaincodeStubNaiveImpl();

        final List<String> args = new ArrayList<>();
        args.add("samplecontract:tFour");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.setBeforeInvoked(0);
        SampleContract.setAfterInvoked(0);
        SampleContract.setDoWorkInvoked(0);
        SampleContract.setT1Invoked(0);

        final Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.INTERNAL_SERVER_ERROR));
        assertThat(response.getMessage(), is(equalTo("Undefined contract method called")));
        assertThat(response.getStringPayload(), is(nullValue()));
        assertThat(SampleContract.getBeforeInvoked(), is(1));
        assertThat(SampleContract.getAfterInvoked(), is(0));
        assertThat(SampleContract.getDoWorkInvoked(), is(0));
        assertThat(SampleContract.getT1Invoked(), is(0));
    }

    @Test
    public void testInvokeContractThatDoesNotExist() {
        final ContractRouter r = new ContractRouter(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        final ChaincodeStub s = new ChaincodeStubNaiveImpl();

        final List<String> args = new ArrayList<>();
        args.add("thereisnocontract:t1");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.setBeforeInvoked(0);
        SampleContract.setAfterInvoked(0);
        SampleContract.setDoWorkInvoked(0);
        SampleContract.setT1Invoked(0);

        final Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.INTERNAL_SERVER_ERROR));
        assertThat(response.getMessage(), is(equalTo("Undefined contract called")));
        assertThat(response.getStringPayload(), is(nullValue()));
        assertThat(SampleContract.getBeforeInvoked(), is(0));
        assertThat(SampleContract.getAfterInvoked(), is(0));
        assertThat(SampleContract.getDoWorkInvoked(), is(0));
        assertThat(SampleContract.getT1Invoked(), is(0));
    }

    @Test
    public void testInvokeTxnThatDoesNotExist() {
        final ContractRouter r = new ContractRouter(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        final ChaincodeStub s = new ChaincodeStubNaiveImpl();

        final List<String> args = new ArrayList<>();
        args.add("samplecontract:notsupposedtoexist");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.setBeforeInvoked(0);
        SampleContract.setAfterInvoked(0);
        SampleContract.setDoWorkInvoked(0);
        SampleContract.setT1Invoked(0);

        final Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.INTERNAL_SERVER_ERROR));
        assertThat(response.getMessage(), is(equalTo("Undefined contract method called")));
        assertThat(response.getStringPayload(), is(nullValue()));
        assertThat(SampleContract.getBeforeInvoked(), is(1));
        assertThat(SampleContract.getAfterInvoked(), is(0));
        assertThat(SampleContract.getDoWorkInvoked(), is(0));
        assertThat(SampleContract.getT1Invoked(), is(0));
    }

    @Test
    public void testInvokeTxnThatReturnsNullString() {
        final ContractRouter r = new ContractRouter(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        final ChaincodeStub s = new ChaincodeStubNaiveImpl();

        final List<String> args = new ArrayList<>();
        args.add("samplecontract:t5");
        args.add("asdf");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.setBeforeInvoked(0);
        SampleContract.setAfterInvoked(0);
        SampleContract.setDoWorkInvoked(0);
        SampleContract.setT1Invoked(0);

        final Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.SUCCESS));
        assertThat(response.getMessage(), is(nullValue()));
        assertThat(response.getStringPayload(), is(nullValue()));
        assertThat(SampleContract.getBeforeInvoked(), is(1));
        assertThat(SampleContract.getAfterInvoked(), is(1));
        assertThat(SampleContract.getDoWorkInvoked(), is(1));
        assertThat(SampleContract.getT1Invoked(), is(0));
    }

    @Test
    public void testInvokeTxnThatThrowsAnException() {
        final ContractRouter r = new ContractRouter(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        final ChaincodeStub s = new ChaincodeStubNaiveImpl();

        final List<String> args = new ArrayList<>();
        args.add("samplecontract:t3");
        args.add("RuntimeException");
        args.add("T3 fail!");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.setBeforeInvoked(0);
        SampleContract.setAfterInvoked(0);
        SampleContract.setDoWorkInvoked(0);


        final Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.INTERNAL_SERVER_ERROR));
        assertThat(response.getMessage(), is(equalTo("Error during contract method execution")));
        assertThat(response.getStringPayload(), is(nullValue()));
        assertThat(SampleContract.getBeforeInvoked(), is(1));
        assertThat(SampleContract.getAfterInvoked(), is(0));
        assertThat(SampleContract.getDoWorkInvoked(), is(0));
    }

    @Test
    public void testInvokeTxnThatThrowsAChaincodeException() {
        final ContractRouter r = new ContractRouter(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});
        r.findAllContracts();
        final ChaincodeStub s = new ChaincodeStubNaiveImpl();

        final List<String> args = new ArrayList<>();
        args.add("samplecontract:t3");
        args.add("TransactionException");
        args.add("T3 fail!");
        ((ChaincodeStubNaiveImpl) s).setStringArgs(args);

        SampleContract.setBeforeInvoked(0);
        SampleContract.setAfterInvoked(0);
        SampleContract.setDoWorkInvoked(0);


        final Chaincode.Response response = r.invoke(s);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(Chaincode.Response.Status.INTERNAL_SERVER_ERROR));
        assertThat(response.getMessage(), is(equalTo("T3 fail!")));
        assertThat(response.getStringPayload(), is("T3ERR1"));
        assertThat(SampleContract.getBeforeInvoked(), is(1));
        assertThat(SampleContract.getAfterInvoked(), is(0));
        assertThat(SampleContract.getDoWorkInvoked(), is(0));
    }

    /**
     * Test confirming ContractRuntimeExceptions can be created.
     */
    @Test
    public void createContractRuntimeExceptions() {
        final ContractRuntimeException cre1 = new ContractRuntimeException("failure");
        new ContractRuntimeException("another failure", cre1);
        new ContractRuntimeException(new Exception("cause"));
    }
}
