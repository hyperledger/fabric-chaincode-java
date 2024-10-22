/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import org.hyperledger.fabric.shim.ChaincodeException;
import org.junit.jupiter.api.Test;

public class ContractInterfaceTest {
    @Test
    public void createContext() {
        assertThat(
                (new ContractInterface() {}).createContext(new ChaincodeStubNaiveImpl()),
                is(instanceOf(Context.class)));
    }

    @Test
    public void unknownTransaction() {
        final ContractInterface c = new ContractInterface() {};

        assertThatThrownBy(() -> c.unknownTransaction(c.createContext(new ChaincodeStubNaiveImpl())))
                .isInstanceOf(ChaincodeException.class)
                .hasMessage("Undefined contract method called");
    }

    @Test
    public void beforeTransaction() {
        final ContractInterface c = new ContractInterface() {};

        c.beforeTransaction(c.createContext(new ChaincodeStubNaiveImpl()));
    }

    @Test
    public void afterTransaction() {
        final ContractInterface c = new ContractInterface() {};
        c.afterTransaction(c.createContext(new ChaincodeStubNaiveImpl()), "ReturnValue");
    }
}
