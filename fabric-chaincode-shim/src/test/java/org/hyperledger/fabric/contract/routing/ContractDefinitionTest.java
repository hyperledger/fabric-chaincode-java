/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.security.Permission;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.routing.impl.ContractDefinitionImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import contract.SampleContract;
import io.swagger.v3.oas.annotations.info.Info;

public class ContractDefinitionTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void beforeEach() {
    }

    @Test
    public void constructor() throws NoSuchMethodException, SecurityException {

        ContractDefinition cf = new ContractDefinitionImpl(SampleContract.class);
        assertThat(cf.toString(), startsWith("samplecontract:"));
    }

    @Contract(name = "", info = @Info())
    public class FailureTestObject {

    }

    public boolean fail;
    public int step = 1;

    @Test
    public void unkownRoute() {

        SecurityManager tmp = new SecurityManager() {
            int count = 0;

            @Override
            public void checkPackageAccess(String pkg) {

                if (pkg.startsWith("org.hyperledger.fabric.contract")) {
                    if (count >= step) {
                        throw new SecurityException("Sorry I can't do that");
                    }
                    count++;
                }
                super.checkPackageAccess(pkg);
            }

            @Override
            public void checkPermission(Permission perm) {
                return;
            }
        };

        try {
            ContractDefinition cf = new ContractDefinitionImpl(SampleContract.class);
            System.setSecurityManager(tmp);
            this.fail = true;

            cf.getUnkownRoute();
        } catch (Exception e) {
            assertThat(e.getMessage(), equalTo("Failure to find unknownTranction method"));
        } finally {
            System.setSecurityManager(null);
        }
    }

    @Test
    public void duplicateTransaction() throws NoSuchMethodException, SecurityException {
        ContractDefinition cf = new ContractDefinitionImpl(SampleContract.class);

        ContractInterface contract = new SampleContract();
        Method m = contract.getClass().getMethod("t2", new Class[] { Context.class });

        thrown.expect(ContractRuntimeException.class);
        thrown.expectMessage("Duplicate transaction method t2");

        cf.addTxFunction(m);
        cf.addTxFunction(m);
    }
}