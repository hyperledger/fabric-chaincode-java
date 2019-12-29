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
 * Class level annotation that defines the serializer that should be used to
 * convert objects to and from the wire format.
 *
 * This should annotate a class that implements the Serializer interface
 */
@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.TYPE_USE})
public @interface Serializer {
    /**
     * What is this serializer able to target?
     *
     */
    enum TARGET {
        TRANSACTION, ALL
    }

    /**
     *
     * @return Target of the serializer
     */
    TARGET target() default Serializer.TARGET.ALL;
}
