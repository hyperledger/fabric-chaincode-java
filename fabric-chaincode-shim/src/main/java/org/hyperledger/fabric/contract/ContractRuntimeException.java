/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract;

import org.hyperledger.fabric.shim.ChaincodeException;

/**
 * Specific RuntimeException for events that occur in the calling and handling
 * of the Contracts, NOT within the contract logic itself.
 * <p>
 * <B>FUTURE</b> At some future point we wish to add more diagnostic information
 * into this, for example current tx id
 *
 */
public class ContractRuntimeException extends ChaincodeException {

    /**
     *
     * @param string
     */
    public ContractRuntimeException(final String string) {
        super(string);
    }

    /**
     *
     * @param string
     * @param cause
     */
    public ContractRuntimeException(final String string, final Throwable cause) {
        super(string, cause);
    }

    /**
     *
     * @param cause
     */
    public ContractRuntimeException(final Throwable cause) {
        super(cause);
    }

    /**
     * Generated serial version id.
     */
    private static final long serialVersionUID = -884373036398750450L;

}
