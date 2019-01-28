/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Property level annotation defining a property of the class (identified by {@link @DataType})
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface Property {
}
