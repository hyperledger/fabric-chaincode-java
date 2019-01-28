/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Method level annotation indicating the function to be a callable transaction function
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Transaction {
    /**
     * true indicates that this function is intended to be called with the 'submit' semantics<p>
     * false indicates that this is intended to be called with the evaluate semantics
     * @return
     */
    boolean submit() default true;
}
