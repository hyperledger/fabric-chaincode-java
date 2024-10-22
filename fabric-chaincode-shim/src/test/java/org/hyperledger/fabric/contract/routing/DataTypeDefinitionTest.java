/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;

import java.util.Map;
import org.hyperledger.fabric.contract.MyType2;
import org.hyperledger.fabric.contract.routing.impl.DataTypeDefinitionImpl;
import org.junit.jupiter.api.Test;

public class DataTypeDefinitionTest {
    @Test
    public void constructor() {
        final DataTypeDefinitionImpl dtd = new DataTypeDefinitionImpl(MyType2.class);
        assertThat(dtd.getTypeClass(), equalTo(MyType2.class));
        assertThat(dtd.getName(), equalTo("org.hyperledger.fabric.contract.MyType2"));
        assertThat(dtd.getSimpleName(), equalTo("MyType2"));

        final Map<String, PropertyDefinition> properties = dtd.getProperties();
        assertThat(properties.size(), equalTo(2));
        assertThat(properties, hasKey("value"));
        assertThat(properties, hasKey("constrainedValue"));

        final PropertyDefinition pd = properties.get("constrainedValue");
        final Map<String, ?> ts = pd.getSchema();

        assertThat(ts, hasEntry("title", "MrProperty"));
        assertThat(ts, hasEntry("Pattern", "[a-z]"));
        assertThat(ts, hasEntry("uniqueItems", false));
        assertThat(ts, hasEntry("required", new String[] {"true", "false"}));
        assertThat(ts, hasEntry("enum", new String[] {"a", "bee", "cee", "dee"}));
        assertThat(ts, hasEntry("minimum", 42));
    }
}
