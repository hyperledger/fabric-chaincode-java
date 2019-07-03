
/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.impl.PropertyDefinitionImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PropertyDefinitionTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void beforeEach() {
    }

    @Test
    public void constructor() throws NoSuchMethodException, SecurityException {
        Field props[] = String.class.getFields();
        TypeSchema ts = new TypeSchema();
        PropertyDefinition pd = new PropertyDefinitionImpl("test", String.class, ts, props[0]);

        assertThat(pd.getTypeClass(), equalTo(String.class));
        assertThat(pd.getField(), equalTo(props[0]));
        assertThat(pd.getSchema(), equalTo(ts));
        assertThat(pd.getName(), equalTo("test"));
    };
}