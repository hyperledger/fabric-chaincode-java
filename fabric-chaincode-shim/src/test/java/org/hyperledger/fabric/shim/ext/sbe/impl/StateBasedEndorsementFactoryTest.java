/*
 * Copyright 2019 IBM DTCC All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.ext.sbe.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class StateBasedEndorsementFactoryTest {
    @Test
    public void getInstance() {
        assertNotNull(StateBasedEndorsementFactory.getInstance());
        assertInstanceOf(StateBasedEndorsementFactory.class, StateBasedEndorsementFactory.getInstance());
    }

    @Test
    public void newStateBasedEndorsement() {
        assertNotNull(StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(new byte[] {}));
        assertThatThrownBy(() -> StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(new byte[] {0}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
