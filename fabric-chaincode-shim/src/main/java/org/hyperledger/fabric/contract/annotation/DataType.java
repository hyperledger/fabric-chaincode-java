/*
Copyright IBM Corp.  All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Class level annotation indicating this class represents one of the complex
 * types that can be returned or passed to the transaction functions.
 *
 * This will appear within the metadata
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface DataType {
    String namespace() default "";
}
