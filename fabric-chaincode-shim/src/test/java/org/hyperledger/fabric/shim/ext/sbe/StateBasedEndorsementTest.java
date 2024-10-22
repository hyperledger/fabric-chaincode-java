/*
 * Copyright 2019 IBM DTCC All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.ext.sbe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class StateBasedEndorsementTest {
    @Test
    public void testRoleType() {
        assertThat(StateBasedEndorsement.RoleType.forVal("MEMBER"))
                .isEqualTo(StateBasedEndorsement.RoleType.RoleTypeMember);
        assertThat(StateBasedEndorsement.RoleType.forVal("PEER"))
                .isEqualTo(StateBasedEndorsement.RoleType.RoleTypePeer);

        assertThatThrownBy(() -> StateBasedEndorsement.RoleType.forVal("NONEXIST"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
