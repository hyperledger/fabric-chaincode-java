/*
 * Copyright 2019 IBM DTCC All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.ext.sbe.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.protos.common.MspPrincipal.MSPPrincipal;
import org.hyperledger.fabric.protos.common.MspPrincipal.MSPPrincipal.Classification;
import org.hyperledger.fabric.protos.common.MspPrincipal.MSPRole;
import org.hyperledger.fabric.protos.common.MspPrincipal.MSPRole.MSPRoleType;
import org.hyperledger.fabric.protos.common.Policies.SignaturePolicy;
import org.hyperledger.fabric.protos.common.Policies.SignaturePolicyEnvelope;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements {@link StateBasedEndorsement}.
 */
public final class StateBasedEndorsementImpl implements StateBasedEndorsement {
    private static Log logger = LogFactory.getLog(StateBasedEndorsementImpl.class);

    private final Map<String, MSPRoleType> orgs = new HashMap<>();

    StateBasedEndorsementImpl(final byte[] ep) {
        byte[] sbe;
        if (ep == null) {
            sbe = new byte[] {};
        } else {
            sbe = ep;
        }
        try {
            final SignaturePolicyEnvelope spe = SignaturePolicyEnvelope.parseFrom(sbe);
            setMSPIDsFromSP(spe);
        } catch (final InvalidProtocolBufferException e) {
            throw new IllegalArgumentException("error unmarshalling endorsement policy bytes", e);
        }

    }

    @Override
    public byte[] policy() {
        final SignaturePolicyEnvelope spe = policyFromMSPIDs();
        return spe.toByteArray();
    }

    @Override
    public void addOrgs(final RoleType role, final String... organizations) {
        MSPRoleType mspRole;
        if (RoleType.RoleTypeMember.equals(role)) {
            mspRole = MSPRoleType.MEMBER;
        } else {
            mspRole = MSPRoleType.PEER;
        }
        for (final String neworg : organizations) {
            orgs.put(neworg, mspRole);
        }
    }

    @Override
    public void delOrgs(final String... organizations) {
        for (final String delorg : organizations) {
            orgs.remove(delorg);
        }
    }

    @Override
    public List<String> listOrgs() {
        final List<String> res = new ArrayList<>();
        res.addAll(orgs.keySet());
        return res;
    }

    private void setMSPIDsFromSP(final SignaturePolicyEnvelope spe) {
        spe.getIdentitiesList().stream().filter(identity -> Classification.ROLE.equals(identity.getPrincipalClassification())).forEach(this::addOrg);
    }

    private void addOrg(final MSPPrincipal identity) {
        try {
            final MSPRole mspRole = MSPRole.parseFrom(identity.getPrincipal());
            orgs.put(mspRole.getMspIdentifier(), mspRole.getRole());
        } catch (final InvalidProtocolBufferException e) {
            logger.warn("error unmarshalling msp principal");
            throw new IllegalArgumentException("error unmarshalling msp principal", e);
        }
    }

    private SignaturePolicyEnvelope policyFromMSPIDs() {
        final List<String> mspids = listOrgs();

        mspids.sort(Comparator.naturalOrder());
        final List<MSPPrincipal> principals = new ArrayList<>();
        final List<SignaturePolicy> sigpolicy = new ArrayList<>();
        for (int i = 0; i < mspids.size(); i++) {
            final String mspid = mspids.get(i);
            principals.add(MSPPrincipal.newBuilder().setPrincipalClassification(Classification.ROLE)
                    .setPrincipal(MSPRole.newBuilder().setMspIdentifier(mspid).setRole(orgs.get(mspid)).build().toByteString()).build());

            sigpolicy.add(StateBasedEndorsementUtils.signedBy(i));
        }

        // create the policy: it requires exactly 1 signature from all of the principals
        return SignaturePolicyEnvelope.newBuilder().setVersion(0).setRule(StateBasedEndorsementUtils.nOutOf(mspids.size(), sigpolicy))
                .addAllIdentities(principals).build();
    }

}
