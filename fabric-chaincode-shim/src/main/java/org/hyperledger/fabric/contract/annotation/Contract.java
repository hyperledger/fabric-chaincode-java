/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.info.Info;

/**
 * Class level annotation that identifies this class as being a contract. Can
 * supply information and an alternative name for the contract rather than the
 * classname
 *
 * The Info object can be supplied to provide additional information about the
 * contract; the format of this uses the OpenAPI v3 specification of info
 * {@see io.swagger.v3.oas.annotations.info.Info}
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Contract {
    Info info();

    String name() default "";
}
