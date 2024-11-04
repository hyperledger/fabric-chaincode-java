/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract;

import org.hyperledger.fabric.shim.ChaincodeStub;

/** Factory to create {@link Context} from {@link ChaincodeStub} by wrapping stub with dynamic proxy. */
public final class ContextFactory {
    private static final ContextFactory INSTANCE = new ContextFactory();

    /** @return ContextFactory */
    public static ContextFactory getInstance() {
        return INSTANCE;
    }

    /**
     * @param stub
     * @return Context
     */
    public Context createContext(final ChaincodeStub stub) {
        return new Context(stub);
    }
}
