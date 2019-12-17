/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing;

import java.util.Map;

import org.hyperledger.fabric.contract.metadata.TypeSchema;

public interface DataTypeDefinition {

    /**
     * @return String
     */
    String getName();

    /**
     * @return Map of String to PropertyDefinitions
     */
    Map<String, PropertyDefinition> getProperties();

    /**
     * @return String
     */
    String getSimpleName();

    /**
     * @return Class object of the type
     */
    Class<?> getTypeClass();

    /**
     * @return TypeSchema
     */
    TypeSchema getSchema();
}
