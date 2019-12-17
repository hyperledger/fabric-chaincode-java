/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract.routing.impl;

import java.lang.reflect.Parameter;

import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.ParameterDefinition;

public final class ParameterDefinitionImpl implements ParameterDefinition {

    private final Class<?> typeClass;
    private final TypeSchema schema;
    private final Parameter parameter;
    private final String name;

    /**
     *
     * @param name
     * @param typeClass
     * @param schema
     * @param p
     */
    public ParameterDefinitionImpl(final String name, final Class<?> typeClass, final TypeSchema schema, final Parameter p) {
        this.typeClass = typeClass;
        this.schema = schema;
        this.parameter = p;
        this.name = name;
    }

    @Override
    public Class<?> getTypeClass() {
        return this.typeClass;
    }

    @Override
    public TypeSchema getSchema() {
        return this.schema;
    }

    @Override
    public Parameter getParameter() {
        return this.parameter;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name + "-" + this.typeClass + "-" + this.schema + "-" + this.parameter;
    }
}
