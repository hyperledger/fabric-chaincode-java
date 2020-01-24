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
 * Field and parameter level annotation defining a property of the class.
 *
 * (identified by {@link DataType}) Can also be used on the parameters of
 * transaction functions
 * <p>
 * Example of using this annotation
 *
 * <pre>
 *
 * // max 15 character string, a-z with spaces
 * &#64;Property(schema = {"pattern", "^[a-zA-Z\\s]{0,15}$"})
 * private String text;
 *
 * // How friendly is this on a scale of 1-5, 1 being formal, 5 being familiar
 * &#64;Property(schema = {"minimum", "1", "maximum", "5"})
 * private int friendliness = 1;
 *
 * </pre>
 */
@Retention(RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Property {

    /**
     * Allows each property to be defined a detail set of rules to determine the
     * valid types of this data. The format follows the syntax of the OpenAPI Schema
     * object.
     *
     * @return String array of the key-value pairs of the schema
     */
    String[] schema() default {};
}
