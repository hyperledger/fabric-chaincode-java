/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing;

import java.util.Collection;

import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.impl.TypeRegistryImpl;

public interface TypeRegistry {

    /**
     * @return TypeRegistry
     */
    static TypeRegistry getRegistry() {
        return TypeRegistryImpl.getInstance();
    }

    /**
     * @param dtd
     */
    void addDataType(DataTypeDefinition dtd);

    /**
     * @param cl
     */
    void addDataType(Class<?> cl);

    /**
     * @param name
     * @return DataTypeDefinition
     */
    DataTypeDefinition getDataType(String name);

    /**
     * @param schema
     * @return DataTypeDefinition
     */
    DataTypeDefinition getDataType(TypeSchema schema);

    /**
     * @return All datatypes
     */
    Collection<DataTypeDefinition> getAllDataTypes();

}
