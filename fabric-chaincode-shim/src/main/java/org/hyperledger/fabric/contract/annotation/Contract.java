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
 * Class level annotation that identifies this class as being a contract. Can
 * supply information and an alternative name for the contract rather than the
 * classname
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Contract {

    /**
     * The Info object can be supplied to provide additional information about the
     * contract.
     *
     * Including title, description, version and license
     *
     * @return Info object
     */
    Info info() default @Info();

    /**
     * Contract name.
     *
     * Normally the name of the class is used to refer to the contract (name without
     * package). This can be altered if wished.
     *
     * @return Name of the contract to be used instead of the Classname
     */
    String name() default "";

    /**
     * Transaction Serializer Classname.
     *
     * Fully Qualified Classname of the TRANSACTION serializer that should be used
     * with this contract.
     *
     * This is the serializer that is used to parse incoming transaction request
     * parameters and convert the return type
     *
     * @return Default serializer classname
     */
    String transactionSerializer() default "org.hyperledger.fabric.contract.execution.JSONTransactionSerializer";

}
