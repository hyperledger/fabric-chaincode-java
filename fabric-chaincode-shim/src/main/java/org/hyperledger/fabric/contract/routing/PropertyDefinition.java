/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing;

import java.lang.reflect.Field;

import org.hyperledger.fabric.contract.metadata.TypeSchema;

public interface PropertyDefinition {

    /**
     * @return Class of the Property
     */
    Class<?> getTypeClass();

    /**
     * @return TypeSchema
     */
    TypeSchema getSchema();

    /**
     * @return Field
     */
    Field getField();

    /**
     * @return Name of the property
     */
    String getName();

}
