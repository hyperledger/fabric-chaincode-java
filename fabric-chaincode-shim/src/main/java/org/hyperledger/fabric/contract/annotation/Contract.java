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
 * <p>
 * The Info object can be supplied to provide additional information about the
 * contract; the format of this uses the OpenAPI v3 specification of info
 * {@link io.swagger.v3.oas.annotations.info.Info}
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Contract {

    /**
     * The Info object can be supplied to provide additional information about the
     * contract; the format of this uses the  OpenAPI v3 Info format
     *
     *
     * @return OpenAPI v3 specification of info
     *         {@link io.swagger.v3.oas.annotations.info.Info}
     */
    Info info();

    /**
     * Normally the name of the class is used to refer to the contract (name without package).
     * This can be altered if wished.
     *
     * @return Name of the contract to be used instead of the Classname
     */
    String name() default "";
}
