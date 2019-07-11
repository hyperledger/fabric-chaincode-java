/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Class level annotation that identifies this class as being a contact. Can
 * be populated with email, name and url fields.
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Contact {

    String email() default "";

    String name() default "";

    String url() default "";

}
