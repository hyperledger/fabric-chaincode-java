/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.ext.sbe.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StateBasedEndorsementFactoryTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void getInstance() {
        assertNotNull(StateBasedEndorsementFactory.getInstance());
        assertTrue(StateBasedEndorsementFactory.getInstance() instanceof StateBasedEndorsementFactory);
    }

    @Test
    public void newStateBasedEndorsement() {
        assertNotNull(StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(new byte[]{}));
        thrown.expect(IllegalArgumentException.class);
        StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(new byte[]{0});
    }
}