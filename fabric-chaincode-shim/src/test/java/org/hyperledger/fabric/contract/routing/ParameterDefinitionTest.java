/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.lang.reflect.Parameter;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.impl.ParameterDefinitionImpl;
import org.junit.jupiter.api.Test;

public class ParameterDefinitionTest {
    @Test
    public void constructor() throws NoSuchMethodException, SecurityException {
        final Parameter[] params =
                String.class.getMethod("concat", String.class).getParameters();
        final ParameterDefinition pd = new ParameterDefinitionImpl("test", String.class, new TypeSchema(), params[0]);
        assertThat(pd.toString(), equalTo("test-class java.lang.String-{}-java.lang.String arg0"));
        assertThat(pd.getTypeClass(), equalTo(String.class));
        assertThat(pd.getParameter(), equalTo(params[0]));
    }
}
