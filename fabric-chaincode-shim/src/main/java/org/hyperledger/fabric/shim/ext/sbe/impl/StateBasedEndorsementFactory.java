/*
 * Copyright 2019 IBM DTCC All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.ext.sbe.impl;

import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;

/** Factory for {@link StateBasedEndorsement} objects. */
public class StateBasedEndorsementFactory {
    private static final StateBasedEndorsementFactory INSTANCE = new StateBasedEndorsementFactory();

    /** @return Endorsement Factory */
    public static StateBasedEndorsementFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Constructs a state-based endorsement policy from a given serialized EP byte array. If the byte array is empty, a
     * new EP is created.
     *
     * @param ep serialized endorsement policy
     * @return New StateBasedEndorsement instance
     */
    public StateBasedEndorsement newStateBasedEndorsement(final byte[] ep) {
        return new StateBasedEndorsementImpl(ep);
    }
}
