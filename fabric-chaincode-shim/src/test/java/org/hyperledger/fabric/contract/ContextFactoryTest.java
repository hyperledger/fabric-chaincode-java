/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract;

import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ContextFactoryTest {

    @Test
    public void getInstance() {
        ContextFactory f1 = ContextFactory.getInstance();
        ContextFactory f2 = ContextFactory.getInstance();
        assertThat(f1, sameInstance(f2));
    }

    @Test
    public void createContext() {
        ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        Context ctx = ContextFactory.getInstance().createContext(stub);

        assertThat(stub.getArgs(), is(equalTo(ctx.getArgs())));
        assertThat(stub.getStringArgs(), is(equalTo(ctx.getStringArgs())));
        assertThat(stub.getFunction(), is(equalTo(ctx.getFunction())));
        assertThat(stub.getParameters(), is(equalTo(ctx.getParameters())));
        assertThat(stub.getTxId(), is(equalTo(ctx.getTxId())));
        assertThat(stub.getChannelId(), is(equalTo(ctx.getChannelId())));
        assertThat(stub.invokeChaincode("cc", Collections.emptyList(), "ch0"), is(equalTo(ctx.invokeChaincode("cc", Collections.emptyList(), "ch0"))));

        assertThat(stub.getState("a"), is(equalTo(ctx.getState("a"))));
        ctx.putState("b", "sdfg".getBytes());
        assertThat(stub.getStringState("b"), is(equalTo(ctx.getStringState("b"))));
    }
}