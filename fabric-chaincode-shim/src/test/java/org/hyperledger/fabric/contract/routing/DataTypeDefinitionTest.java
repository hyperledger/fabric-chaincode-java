/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import org.hyperledger.fabric.contract.MyType2;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.impl.DataTypeDefinitionImpl;
import org.junit.jupiter.api.Test;

final class DataTypeDefinitionTest {
    @Test
    void constructor() {
        final DataTypeDefinitionImpl dtd = new DataTypeDefinitionImpl(MyType2.class);
        assertThat(dtd.getTypeClass()).isEqualTo(MyType2.class);
        assertThat(dtd.getName()).isEqualTo("org.hyperledger.fabric.contract.MyType2");
        assertThat(dtd.getSimpleName()).isEqualTo("MyType2");

        final Map<String, PropertyDefinition> properties = dtd.getProperties();
        assertThat(properties.size()).isEqualTo(2);
        assertThat(properties).containsKey("value");
        assertThat(properties).containsKey("constrainedValue");

        final PropertyDefinition pd = properties.get("constrainedValue");
        final TypeSchema ts = pd.getSchema();

        assertThat(ts)
                .contains(
                        entry("title", "MrProperty"),
                        entry("Pattern", "[a-z]"),
                        entry("uniqueItems", false),
                        entry("required", new String[] {"true", "false"}),
                        entry("enum", new String[] {"a", "bee", "cee", "dee"}),
                        entry("minimum", 42));
    }
}
