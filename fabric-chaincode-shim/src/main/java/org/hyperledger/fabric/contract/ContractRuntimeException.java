/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract;

import org.hyperledger.fabric.shim.ChaincodeException;

/**
 * Specific RuntimeException for events that occur in the calling and handling
 * of the Contracts, NOT within the contract logic itself.
 * <p>
 * <B>FUTURE</b> At some future point we wish to add more diagnostic information into this,
 * for example current tx id
 *
 */
public class ContractRuntimeException extends ChaincodeException {

    public ContractRuntimeException(String string) {
        super(string);
    }

    public ContractRuntimeException(String string, Throwable cause) {
        super(string, cause);
    }

    public ContractRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * Generated serial version id
     */
    private static final long serialVersionUID = -884373036398750450L;

}
