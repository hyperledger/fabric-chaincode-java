/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract;

import org.hyperledger.fabric.shim.ChaincodeStub;

/**
 * Factory to create {@link Context} from {@link ChaincodeStub} by wrapping stub
 * with dynamic proxy.
 */
public class ContextFactory {
    private static ContextFactory cf;

    static synchronized public ContextFactory getInstance() {
        if (cf == null) {
            cf = new ContextFactory();
        }
        return cf;
    }

    public Context createContext(final ChaincodeStub stub) {
        Context newContext = new Context(stub);
        return newContext;
    }

}
