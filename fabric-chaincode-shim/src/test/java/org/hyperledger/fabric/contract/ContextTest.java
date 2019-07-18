/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.Test;

public class ContextTest {

    /**
     * Test creating a new context returns what we expect
     */
    @Test
    public void getInstance() {
        ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        Context context1 = new Context(stub);
        Context context2 = new Context(stub);
        assertThat(context1.getStub(), sameInstance(context2.getStub()));
    }

    /**
     * Test identity created in Context constructor matches getClientIdentity
     */
    @Test
    public void getSetClientIdentity() {
        ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        Context context = ContextFactory.getInstance().createContext(stub);
        assertThat(context.getClientIdentity(), sameInstance(context.clientIdentity));

    }
}