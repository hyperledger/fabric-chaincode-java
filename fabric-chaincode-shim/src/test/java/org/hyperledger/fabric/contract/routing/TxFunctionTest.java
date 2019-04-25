/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.routing.impl.TxFunctionImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class TxFunctionTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();


    class TestObject implements ContractInterface{

    	@Transaction()
    	public void testMethod1() {

    	}

    	@Transaction()
    	public void testMethod2() {

    	}
    }

    @Before
    public void beforeEach() {
    }

    @Test
    public void constructor() throws NoSuchMethodException, SecurityException {
    	TestObject test = new TestObject();
    	ContractDefinition cd = mock(ContractDefinition.class);
    	when(cd.getContractImpl()).thenReturn(test);
    	TxFunction txfn = new TxFunctionImpl(test.getClass().getMethod("testMethod1", null), cd );
    	String name = txfn.getName();
    	assertEquals(name, "testMethod1");

    	assertThat(txfn.toString(),startsWith("testMethod1"));
    }
}