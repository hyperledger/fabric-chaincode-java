/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Collection;
import org.hyperledger.fabric.contract.routing.impl.DataTypeDefinitionImpl;
import org.hyperledger.fabric.contract.routing.impl.TypeRegistryImpl;
import org.junit.jupiter.api.Test;

public class TypeRegistryTest {
    @Test
    public void addDataType() {
        final TypeRegistryImpl tr = new TypeRegistryImpl();
        tr.addDataType(String.class);

        final DataTypeDefinition drt = tr.getDataType("String");
        assertThat(drt.getName(), equalTo("java.lang.String"));
    }

    @Test
    public void addDataTypeDefinition() {
        final DataTypeDefinitionImpl dtd = new DataTypeDefinitionImpl(String.class);
        final TypeRegistryImpl tr = new TypeRegistryImpl();
        tr.addDataType(dtd);

        final DataTypeDefinition drt = tr.getDataType("java.lang.String");
        assertThat(drt.getName(), equalTo("java.lang.String"));
    }

    @Test
    public void getAllDataTypes() {

        final TypeRegistryImpl tr = new TypeRegistryImpl();
        tr.addDataType(String.class);
        tr.addDataType(Integer.class);
        tr.addDataType(Float.class);

        final Collection<DataTypeDefinition> c = tr.getAllDataTypes();
        assertThat(c.size(), equalTo(3));
    }
}
