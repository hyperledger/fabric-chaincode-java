/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ContractInterfaceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void getContext() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("getContext default implementation can't be directly invoked");
        new ContractInterface() {}.getContext();
    }

    @Test
    public void createContext() {
        assertThat((new ContractInterface(){}).createContext(new ChaincodeStubNaiveImpl()), is(instanceOf(Context.class)));
    }

    @Test
    public void unknownTransaction() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Undefined contract method called");
        new ContractInterface() {}.unknownTransaction();
    }

    @Test
    public void beforeTransaction() {
        new ContractInterface() {}.beforeTransaction();
    }

    @Test
    public void afterTransaction() {
        new ContractInterface() {}.afterTransaction();
    }
}