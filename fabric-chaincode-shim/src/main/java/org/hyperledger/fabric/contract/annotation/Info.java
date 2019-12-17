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
 * Info Details
 *
 *
 * Class level annotation that identifies this class as being an info object.
 * Can supply additional information about the contract, including title,
 * description, version, license and contact information.
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Info {

    /**
     * @return String
     */
    String title() default "";

    /**
     * @return String
     */
    String description() default "";

    /**
     * @return String
     */
    String version() default "";

    /**
     * @return String
     */
    String termsOfService() default "";

    /**
     * License object that can be populated to include name and url.
     *
     * @return License object
     *
     */
    License license() default @License();

    /**
     * Contact object that can be populated with email, name and url fields.
     *
     * @return Contact object
     *
     */
    Contact contact() default @Contact();

}
