/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.annotation;

import io.swagger.v3.oas.annotations.info.Info;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Class level annotation that identifies this class as being a contract.
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Contract {
    Info info();
    String namespace() default "";
}
