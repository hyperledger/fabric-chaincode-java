/*
 * Copyright 2019 IBM DTCC All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.ext.sbe;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StateBasedEndorsementTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testRoleType() {
        assertEquals("Wrong role", StateBasedEndorsement.RoleType.RoleTypeMember, StateBasedEndorsement.RoleType.forVal("MEMBER"));
        assertEquals("Wrong role", StateBasedEndorsement.RoleType.RoleTypePeer, StateBasedEndorsement.RoleType.forVal("PEER"));

        thrown.expect(IllegalArgumentException.class);
        StateBasedEndorsement.RoleType.forVal("NONEXIST");
    }

}
