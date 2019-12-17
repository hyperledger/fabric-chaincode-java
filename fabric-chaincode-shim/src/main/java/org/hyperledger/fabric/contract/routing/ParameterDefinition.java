/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing;

import java.lang.reflect.Parameter;

import org.hyperledger.fabric.contract.metadata.TypeSchema;

public interface ParameterDefinition {

    /**
     * @return Class type of the parameter
     */
    Class<?> getTypeClass();

    /**
     * @return TypeSchema of the parameter
     */
    TypeSchema getSchema();

    /**
     * @return Parameter
     */
    Parameter getParameter();

    /**
     * @return name of the parameter
     */
    String getName();

}
