/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.DataTypeDefinition;
import org.hyperledger.fabric.contract.routing.TypeRegistry;

/**
 * Registry to hold the complex data types as defined in the contract.
 *
 */
public final class TypeRegistryImpl implements TypeRegistry {

    private static TypeRegistryImpl singletonInstance;

    /**
     * Get the TypeRegistry singleton instance.
     *
     * @return TypeRegistry
     */
    public static TypeRegistry getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new TypeRegistryImpl();
        }

        return singletonInstance;
    }

    private final Map<String, DataTypeDefinition> components = new HashMap<>();

    /*
     * (non-Javadoc)
     *
     * @see
     * org.hyperledger.fabric.contract.routing.TypeRegistry#addDataType(java.lang.
     * Class)
     */
    @Override
    public void addDataType(final Class<?> cl) {
        final DataTypeDefinitionImpl type = new DataTypeDefinitionImpl(cl);
        components.put(type.getSimpleName(), type);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.hyperledger.fabric.contract.routing.TypeRegistry#getAllDataTypes()
     */
    @Override
    public Collection<DataTypeDefinition> getAllDataTypes() {
        return components.values();
    }

    @Override
    public void addDataType(final DataTypeDefinition type) {
        components.put(type.getName(), type);
    }

    @Override
    public DataTypeDefinition getDataType(final String name) {
        return this.components.get(name);
    }

    @Override
    public DataTypeDefinition getDataType(final TypeSchema schema) {
        final String ref = schema.getRef();
        final String format = ref.substring(ref.lastIndexOf("/") + 1);
        return getDataType(format);
    }

}
