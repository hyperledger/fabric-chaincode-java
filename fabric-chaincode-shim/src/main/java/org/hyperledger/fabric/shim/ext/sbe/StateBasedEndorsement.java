/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.ext.sbe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StateBasedEndorsement provides a set of convenience methods to create and
 * modify a state-based endorsement policy. Endorsement policies created by
 * this convenience layer will always be a logical AND of "ORG.peer"
 * principals for one or more ORGs specified by the caller.
 */
public interface StateBasedEndorsement {
    /**
     * Returns the endorsement policy as bytes
     * @return byte[]
     */
    byte[] policy();

    /**
     * Adds the specified orgs to the list of orgs that are required
     * to endorse. All orgs MSP role types will be set to the role that is
     * specified in the first parameter. Among other aspects the desired role
     * depends on the channel's configuration: if it supports node OUs, it is
     * likely going to be the PEER role, while the MEMBER role is the suited
     * one if it does not.
     *
     * @param roleType
     * @param organizations
     */
    void addOrgs(RoleType roleType, String... organizations);

    /**
     * deletes the specified channel orgs from the existing key-level endorsement
     * policy for this KVS key.
     * @param organizations
     */
    void delOrgs(String... organizations);

    /**
     * Returns an array of channel orgs that are required to endorse changes
     *
     * @return List of organizations
     */
    List<String> listOrgs();

    /**
     * RoleType of an endorsement policy's identity
     */
    public enum RoleType {
        /**
         * RoleTypeMember identifies an org's member identity
         */
        RoleTypeMember("MEMBER"),
        /**
         * RoleTypePeer identifies an org's peer identity
         */
        RoleTypePeer("PEER");

        private String val;

        RoleType(String val) {
            this.val = val;
        }

        public String getVal() {
            return val;
        }

        static Map<String, RoleType> reverseLookup = new HashMap<>();

        static {
            for (RoleType item : RoleType.values()) {
                reverseLookup.put(item.getVal(), item);
            }
        }

        public static RoleType forVal(String val) {
            if (!reverseLookup.containsKey(val)) {
                throw new IllegalArgumentException("role type "+ val + " does not exist");
            }
            return reverseLookup.get(val);
        }
    }
}
