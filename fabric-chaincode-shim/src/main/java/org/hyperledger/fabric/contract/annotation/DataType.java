/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Class level annotation indicating this class represents one of the complex
 * types that can be returned or passed to the transaction functions.
 * <p>
 * These datatypes are used (within the current implementation) for determining
 * the data flow protocol from the Contracts to the SDK and for permitting a
 * fully formed Interface Definition to be created for the contract.
 * <p>
 * Complex types can appear within this definition, and these are identified
 * using this annotation.
 * <p>
 * <b>FUTURE</b> To take these annotations are also utilize them for leverage
 * storage
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface DataType {
    /**
     * Namespace of the type.
     *
     * @return String
     */
    String namespace() default "";
}
