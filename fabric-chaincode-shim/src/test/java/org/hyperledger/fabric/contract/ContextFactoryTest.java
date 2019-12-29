/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.Test;

public class ContextFactoryTest {

    @Test
    public void getInstance() {
        final ContextFactory f1 = ContextFactory.getInstance();
        final ContextFactory f2 = ContextFactory.getInstance();
        assertThat(f1, sameInstance(f2));
    }

    @Test
    public void createContext() {
        final ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        final Context ctx = ContextFactory.getInstance().createContext(stub);

        assertThat(stub.getArgs(), is(equalTo(ctx.getStub().getArgs())));
        assertThat(stub.getStringArgs(), is(equalTo(ctx.getStub().getStringArgs())));
        assertThat(stub.getFunction(), is(equalTo(ctx.getStub().getFunction())));
        assertThat(stub.getParameters(), is(equalTo(ctx.getStub().getParameters())));
        assertThat(stub.getTxId(), is(equalTo(ctx.getStub().getTxId())));
        assertThat(stub.getChannelId(), is(equalTo(ctx.getStub().getChannelId())));
        assertThat(stub.invokeChaincode("cc", Collections.emptyList(), "ch0"),
                is(equalTo(ctx.getStub().invokeChaincode("cc", Collections.emptyList(), "ch0"))));

        assertThat(stub.getState("a"), is(equalTo(ctx.getStub().getState("a"))));
        ctx.getStub().putState("b", "sdfg".getBytes());
        assertThat(stub.getStringState("b"), is(equalTo(ctx.getStub().getStringState("b"))));

        assertThat(ctx.clientIdentity.getMSPID(), is(equalTo("testMSPID")));
        assertThat(ctx.clientIdentity.getId(), is(equalTo(
                "x509::CN=admin, OU=Fabric, O=Hyperledger, ST=North Carolina,"
                + " C=US::CN=example.com, OU=WWW, O=Internet Widgets, L=San Francisco, ST=California, C=US")));
    }
}
