/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.lang.reflect.Field;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.impl.PropertyDefinitionImpl;
import org.junit.jupiter.api.Test;

public class PropertyDefinitionTest {
    @Test
    public void constructor() throws NoSuchMethodException, SecurityException {
        final Field[] props = String.class.getFields();
        final TypeSchema ts = new TypeSchema();
        final PropertyDefinition pd = new PropertyDefinitionImpl("test", String.class, ts, props[0]);

        assertThat(pd.getTypeClass(), equalTo(String.class));
        assertThat(pd.getField(), equalTo(props[0]));
        assertThat(pd.getSchema(), equalTo(ts));
        assertThat(pd.getName(), equalTo("test"));
    }
}
