/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import contract.SampleContract;
import java.lang.reflect.Method;
import java.security.Permission;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.routing.impl.ContractDefinitionImpl;
import org.junit.jupiter.api.Test;

public class ContractDefinitionTest {
    @Test
    public void constructor() throws NoSuchMethodException, SecurityException {

        final ContractDefinition cf = new ContractDefinitionImpl(SampleContract.class);
        assertThat(cf.toString(), startsWith("samplecontract:"));
    }

    @Contract(name = "", info = @Info())
    public class FailureTestObject {}

    private boolean fail;
    private final int step = 1;

    @Test
    public void unknownRoute() {

        final SecurityManager tmp = new SecurityManager() {
            private int count = 0;

            @Override
            public void checkPackageAccess(final String pkg) {

                if (pkg.startsWith("org.hyperledger.fabric.contract")) {
                    if (count >= step) {
                        throw new SecurityException("Sorry I can't do that");
                    }
                    count++;
                }
                super.checkPackageAccess(pkg);
            }

            @Override
            public void checkPermission(final Permission perm) {
                return;
            }
        };

        try {
            final ContractDefinition cf = new ContractDefinitionImpl(SampleContract.class);
            System.setSecurityManager(tmp);
            this.fail = true;

            cf.getUnknownRoute();
        } catch (final Exception e) {
            assertThat(e.getMessage(), equalTo("Failure to find unknownTransaction method"));
        } finally {
            System.setSecurityManager(null);
        }
    }

    @Test
    public void duplicateTransaction() throws NoSuchMethodException, SecurityException {
        final ContractDefinition cf = new ContractDefinitionImpl(SampleContract.class);

        final ContractInterface contract = new SampleContract();
        final Method m = contract.getClass().getMethod("t2", new Class<?>[] {Context.class});

        cf.addTxFunction(m);
        assertThatThrownBy(() -> cf.addTxFunction(m))
                .isInstanceOf(ContractRuntimeException.class)
                .hasMessage("Duplicate transaction method t2");
    }
}
