/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ContractInterfaceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createContext() {
        assertThat((new ContractInterface() {
        }).createContext(new ChaincodeStubNaiveImpl()), is(instanceOf(Context.class)));
    }

    @Test
    public void unknownTransaction() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Undefined contract method called");

        ContractInterface c = new ContractInterface() {
        };
        c.unknownTransaction(c.createContext(new ChaincodeStubNaiveImpl()));
    }

    @Test
    public void beforeTransaction() {
        ContractInterface c = new ContractInterface() {
        };

        c.beforeTransaction(c.createContext(new ChaincodeStubNaiveImpl()));
    }

    @Test
    public void afterTransaction() {
        ContractInterface c = new ContractInterface() {
        };
        c.afterTransaction(c.createContext(new ChaincodeStubNaiveImpl()), "ReturnValue");
    }
}