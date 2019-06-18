/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.ext.sbe.impl;

import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;

/**
 * Factory for {@link StateBasedEndorsement} objects
 */
public class StateBasedEndorsementFactory {
    static StateBasedEndorsementFactory instance;
    public static synchronized StateBasedEndorsementFactory getInstance() {
        if (instance == null) {
            instance = new StateBasedEndorsementFactory();
        }
        return instance;
    }

    /**
     * Constructs a state-based endorsement policy from a given
     * serialized EP byte array. If the byte array is empty, a new EP is created.
     *
     * @param ep
     * @return New StateBasedEndorsement instance
     */
    public StateBasedEndorsement newStateBasedEndorsement(byte[] ep) {
        return new StateBasedEndorsementImpl(ep);
    }
}
