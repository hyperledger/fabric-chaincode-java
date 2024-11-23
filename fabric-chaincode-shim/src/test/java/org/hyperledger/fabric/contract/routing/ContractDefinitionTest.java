/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import contract.SampleContract;
import java.lang.reflect.Method;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.routing.impl.ContractDefinitionImpl;
import org.junit.jupiter.api.Test;

final class ContractDefinitionTest {
    @Test
    void constructor() throws SecurityException {

        final ContractDefinition cf = new ContractDefinitionImpl(SampleContract.class);
        assertThat(cf.toString()).startsWith("samplecontract:");
    }

    @Test
    void duplicateTransaction() throws NoSuchMethodException, SecurityException {
        final ContractDefinition cf = new ContractDefinitionImpl(SampleContract.class);

        final ContractInterface contract = new SampleContract();
        final Method m = contract.getClass().getMethod("t2", new Class<?>[] {Context.class});

        cf.addTxFunction(m);
        assertThatThrownBy(() -> cf.addTxFunction(m))
                .isInstanceOf(ContractRuntimeException.class)
                .hasMessage("Duplicate transaction method t2");
    }
}
