/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Contact;

/**
 * Class level annotation that identifies this class as being an info object. Can
 * supply additional information about the contract, including title, description,
 * version, license and contact information.
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Info {

    String title() default "";

    String description() default "";

    String version() default "";

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
