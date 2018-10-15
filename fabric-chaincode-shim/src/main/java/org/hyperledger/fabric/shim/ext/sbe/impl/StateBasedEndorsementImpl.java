/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.ext.sbe.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.protos.common.MspPrincipal.MSPPrincipal;
import org.hyperledger.fabric.protos.common.MspPrincipal.MSPPrincipal.Classification;
import org.hyperledger.fabric.protos.common.MspPrincipal.MSPRole;
import org.hyperledger.fabric.protos.common.MspPrincipal.MSPRole.MSPRoleType;
import org.hyperledger.fabric.protos.common.Policies.SignaturePolicy;
import org.hyperledger.fabric.protos.common.Policies.SignaturePolicyEnvelope;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;

import java.util.*;

/**
 * Implements {@link StateBasedEndorsement}
 */
public class StateBasedEndorsementImpl implements StateBasedEndorsement {
    private static Log logger = LogFactory.getLog(StateBasedEndorsementImpl.class);

    private Map<String, MSPRoleType> orgs = new HashMap<>();

    StateBasedEndorsementImpl(byte[] ep) {
        if (ep == null) {
            ep = new byte[]{};
        }
        try {
            SignaturePolicyEnvelope spe = SignaturePolicyEnvelope.parseFrom(ep);
            setMSPIDsFromSP(spe);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException("error unmarshaling endorsement policy bytes", e);
        }

    }

    @Override
    public byte[] policy() {
        SignaturePolicyEnvelope spe = policyFromMSPIDs();
        return spe.toByteArray();
    }

    @Override
    public void addOrgs(RoleType role, String... organizations) {
        MSPRoleType mspRole;
        if (RoleType.RoleTypeMember.equals(role)) {
            mspRole = MSPRoleType.MEMBER;
        } else {
            mspRole = MSPRoleType.PEER;
        }
        for (String neworg : organizations) {
            orgs.put(neworg, mspRole);
        }
    }

    @Override
    public void delOrgs(String... organizations) {
        for (String delorg : organizations) {
            orgs.remove(delorg);
        }
    }

    @Override
    public List<String> listOrgs() {
        List<String> res = new ArrayList<>();
        res.addAll(orgs.keySet());
        return res;
    }

    private void setMSPIDsFromSP(SignaturePolicyEnvelope spe) {
        spe.getIdentitiesList().stream().filter(identity -> Classification.ROLE.equals(identity.getPrincipalClassification())).forEach(this::addOrg);
    }

    private void addOrg(MSPPrincipal identity) {
        try {
            MSPRole mspRole = MSPRole.parseFrom(identity.getPrincipal());
            orgs.put(mspRole.getMspIdentifier(), mspRole.getRole());
        } catch (InvalidProtocolBufferException e) {
            logger.warn("error unmarshaling msp principal");
            throw new IllegalArgumentException("error unmarshaling msp principal", e);
        }
    }


    private SignaturePolicyEnvelope policyFromMSPIDs() {
        List<String> mspids = listOrgs();

        mspids.sort(Comparator.naturalOrder());
        List<MSPPrincipal> principals = new ArrayList<>();
        List<SignaturePolicy> sigpolicy = new ArrayList<>();
        for (int i = 0; i < mspids.size(); i++) {
            String mspid = mspids.get(i);
            principals.add(MSPPrincipal
                    .newBuilder()
                    .setPrincipalClassification(Classification.ROLE)
                    .setPrincipal(MSPRole
                            .newBuilder()
                            .setMspIdentifier(mspid)
                            .setRole(orgs.get(mspid))
                            .build().toByteString())
                    .build());

            sigpolicy.add(StateBasedEndorsementUtils.signedBy(i));
        }

        // create the policy: it requires exactly 1 signature from all of the principals
        return SignaturePolicyEnvelope
                .newBuilder()
                .setVersion(0)
                .setRule(StateBasedEndorsementUtils.nOutOf(mspids.size(), sigpolicy))
                .addAllIdentities(principals)
                .build();
    }



}
