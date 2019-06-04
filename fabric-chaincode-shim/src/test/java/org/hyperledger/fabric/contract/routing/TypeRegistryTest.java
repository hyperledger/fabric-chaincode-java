/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.hyperledger.fabric.contract.routing.impl.DataTypeDefinitionImpl;
import org.hyperledger.fabric.contract.routing.impl.TypeRegistryImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TypeRegistryTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void beforeEach() {
	}

	@Test
    public void addDataType() {
		TypeRegistryImpl tr = new TypeRegistryImpl();
		tr.addDataType(String.class);

		DataTypeDefinition drt = tr.getDataType("String");
		assertThat(drt.getName(), equalTo("java.lang.String"));
	}

	@Test
	public void addDataTypeDefinition() {
		DataTypeDefinitionImpl dtd = new DataTypeDefinitionImpl(String.class);
		TypeRegistryImpl tr = new TypeRegistryImpl();
		tr.addDataType(dtd);

		DataTypeDefinition drt = tr.getDataType("java.lang.String");
		assertThat(drt.getName(), equalTo("java.lang.String"));
	}

	@Test
	public void getAllDataTypes() {

		TypeRegistryImpl tr = new TypeRegistryImpl();
		tr.addDataType(String.class);
		tr.addDataType(Integer.class);
		tr.addDataType(Float.class);

		Collection c = tr.getAllDataTypes();
		assertThat(c.size(), equalTo(3));
	}

}
