/*
 * Copyright 2019 IBM DTCC All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.ext.sbe.impl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.hyperledger.fabric.protos.common.MspPrincipal.MSPRole.MSPRoleType;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement.RoleType;
import org.junit.Test;

public class StateBasedEndorsementImplTest {

    @Test
    public void addOrgs() {
        // add an org
        final StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(null);
        ep.addOrgs(RoleType.RoleTypePeer, "Org1");

        final byte[] epBytes = ep.policy();
        assertThat(epBytes, is(not(nullValue())));
        assertTrue(epBytes.length > 0);
        final byte[] expectedEPBytes = StateBasedEndorsementUtils.signedByFabricEntity("Org1", MSPRoleType.PEER).toByteString().toByteArray();
        assertArrayEquals(expectedEPBytes, epBytes);
    }

    @Test
    public void delOrgs() {

        final byte[] initEPBytes = StateBasedEndorsementUtils.signedByFabricEntity("Org1", MSPRoleType.PEER).toByteString().toByteArray();
        final StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(initEPBytes);
        final List<String> listOrgs = ep.listOrgs();

        assertThat(listOrgs, is(not(nullValue())));
        assertThat(listOrgs, contains("Org1"));
        assertThat(listOrgs, hasSize(1));

        ep.addOrgs(RoleType.RoleTypeMember, "Org2");
        ep.delOrgs("Org1");

        final byte[] epBytes = ep.policy();

        assertThat(epBytes, is(not(nullValue())));
        assertTrue(epBytes.length > 0);
        final byte[] expectedEPBytes = StateBasedEndorsementUtils.signedByFabricEntity("Org2", MSPRoleType.MEMBER).toByteString().toByteArray();
        assertArrayEquals(expectedEPBytes, epBytes);
    }

    @Test
    public void listOrgs() {
        final byte[] initEPBytes = StateBasedEndorsementUtils.signedByFabricEntity("Org1", MSPRoleType.PEER).toByteString().toByteArray();
        final StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(initEPBytes);
        final List<String> listOrgs = ep.listOrgs();

        assertThat(listOrgs, is(not(nullValue())));
        assertThat(listOrgs, hasSize(1));
        assertThat(listOrgs, contains("Org1"));
    }
}
