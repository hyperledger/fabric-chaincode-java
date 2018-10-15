/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.ext.sbe.impl;

import org.hyperledger.fabric.protos.common.MspPrincipal.MSPRole.MSPRoleType;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement.RoleType;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class StateBasedEndorsementImplTest {

    @Test
    public void addOrgs() {
        // add an org
        StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(null);
        ep.addOrgs(RoleType.RoleTypePeer, "Org1");

        byte[] epBytes = ep.policy();
        assertThat(epBytes, is(not(nullValue())));
        assertTrue(epBytes.length > 0);
        byte[] expectedEPBytes = StateBasedEndorsementUtils.signedByFabricEntity("Org1", MSPRoleType.PEER).toByteString().toByteArray();
        assertArrayEquals(expectedEPBytes, epBytes);
    }

    @Test
    public void delOrgs() {

        byte[] initEPBytes = StateBasedEndorsementUtils.signedByFabricEntity("Org1", MSPRoleType.PEER).toByteString().toByteArray();
        StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(initEPBytes);
        List<String> listOrgs = ep.listOrgs();

        assertThat(listOrgs, is(not(nullValue())));
        assertThat(listOrgs, contains("Org1"));
        assertThat(listOrgs, hasSize(1));

        ep.addOrgs(RoleType.RoleTypeMember, "Org2");
        ep.delOrgs("Org1");

        byte[] epBytes = ep.policy();

        assertThat(epBytes, is(not(nullValue())));
        assertTrue(epBytes.length > 0);
        byte[] expectedEPBytes = StateBasedEndorsementUtils.signedByFabricEntity("Org2", MSPRoleType.MEMBER).toByteString().toByteArray();
        assertArrayEquals(expectedEPBytes, epBytes);
    }

    @Test
    public void listOrgs() {
        byte[] initEPBytes = StateBasedEndorsementUtils.signedByFabricEntity("Org1", MSPRoleType.PEER).toByteString().toByteArray();
        StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(initEPBytes);
        List<String> listOrgs = ep.listOrgs();

        assertThat(listOrgs, is(not(nullValue())));
        assertThat(listOrgs, hasSize(1));
        assertThat(listOrgs, contains("Org1"));
    }
}