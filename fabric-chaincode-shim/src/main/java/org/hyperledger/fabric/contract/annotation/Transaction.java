/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Method level annotation indicating the method to be a callable transaction
 * function.
 * <p>
 * These functions are called in client SDKs by the combination of
 *
 * <pre>
 *  [contractname]:[transactioname]
 * </pre>
 *
 * Unless specified otherwise, the contract name is the class name (without
 * package) and the transaction name is the method name.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Transaction {

    /**
     * SUBMIT or EVALUATE semantics.
     */
    enum TYPE {
        SUBMIT, EVALUATE
    }

    /**
     * Submit semantics.
     *
     * TRUE indicates that this function is intended to be called with the 'submit'
     * semantics
     *
     * FALSE indicates that this is intended to be called with the evaluate
     * semantics
     *
     * @return boolean, default is true
     * @deprecated Please use intent
     */
    @Deprecated
    boolean submit() default true;

    /**
     * What are submit semantics for this transaction.
     *
     * SUBMIT - indicates that this function is intended to be called with the
     * 'submit' semantics EVALUATE - indicates that this is intended to be called
     * with the 'evaluate' semantics
     *
     * @return submit semantics
     */
    TYPE intent() default Transaction.TYPE.SUBMIT;

    /**
     * The name of the callable transaction if it should be different to the method
     * name.
     *
     * @return the transaction name
     */
    String name() default "";
}
