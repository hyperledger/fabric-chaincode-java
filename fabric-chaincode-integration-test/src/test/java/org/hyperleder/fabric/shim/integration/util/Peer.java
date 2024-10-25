/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/** Represents the 'peer' cli command */
public final class Peer extends Command {

    public static PeerBuilder newBuilder() {
        return new PeerBuilder();
    }

    public static final class PeerBuilder extends Command.Builder<Peer> {
        String tlsArgs;
        String orderer;
        String channel;
        String ccname;
        boolean evaluate = false;
        int waitForEventTimeout;
        List<String> args = new ArrayList<String>();
        Map<String, String> transientData;

        public PeerBuilder duplicate() {
            try {
                return (PeerBuilder) this.clone();
            } catch (CloneNotSupportedException e) {

                e.printStackTrace();
                return null;
            }
        }

        public PeerBuilder tlsArgs(String tlsArgs) {
            this.tlsArgs = tlsArgs;
            return this;
        }

        public PeerBuilder orderer(String orderer) {
            this.orderer = orderer;
            return this;
        }

        public PeerBuilder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public PeerBuilder ccname(String ccname) {
            this.ccname = ccname;
            return this;
        }

        public PeerBuilder evaluate() {
            this.evaluate = true;
            return this;
        }

        public PeerBuilder invoke() {
            this.evaluate = false;
            return this;
        }

        public PeerBuilder argsTx(List<String> args) {
            this.args = args;
            return this;
        }

        public PeerBuilder argsTx(String[] argsArray) {
            this.args = Arrays.asList(argsArray);
            return this;
        }

        public PeerBuilder transientData(Map<String, String> transientData) {
            this.transientData = transientData;
            return this;
        }

        public PeerBuilder waitForEvent(int seconds) {
            this.waitForEventTimeout = seconds;
            return this;
        }

        public PeerBuilder waitForEvent() {
            this.waitForEvent(0);
            return this;
        }

        private String transientToString() {
            JSONObject json = new JSONObject(this.transientData);
            return "'" + json.toString() + "'";
        }

        private String argsToString() {
            JSONArray array = new JSONArray(this.args);
            JSONObject json = new JSONObject();
            json.put("Args", array);
            return json.toString();
        }

        public Peer build(Map<String, String> additionalEnv) {

            ArrayList<String> list = new ArrayList<>();
            list.add("peer");
            list.add("chaincode");
            list.add(evaluate ? "query" : "invoke");
            if (tlsArgs != null && !tlsArgs.isEmpty()) {
                list.add(tlsArgs);
            }

            if (channel == null || channel.isEmpty()) {
                throw new RuntimeException("Channel should be set");
            }
            list.add("-C");
            list.add(channel);

            if (ccname == null || ccname.isEmpty()) {
                throw new RuntimeException("Chaincode name should be set");
            }
            list.add("-n");
            list.add(ccname);

            if (args == null || args.isEmpty()) {
                throw new RuntimeException("Args should be set");
            }
            list.add("-c");
            list.add(argsToString());

            if (transientData != null && !transientData.isEmpty()) {
                list.add("--transient");
                list.add(transientToString());
            }

            if (waitForEventTimeout > 0) {
                list.add("--waitForEvent --waitForEventTimeout");
                list.add(waitForEventTimeout + "s");
            } else if (waitForEventTimeout == 0) {
                list.add("--waitForEvent");
            }

            list.add("--orderer");
            list.add("orderer-api.127-0-0-1.nip.io:8080");
            list.add("--peerAddresses");
            list.add("org1peer-api.127-0-0-1.nip.io:8080");
            list.add("--peerAddresses");
            list.add("org2peer-api.127-0-0-1.nip.io:8080");

            return new Peer(list, additionalEnv);
        }
    }

    Peer(List<String> cmd, Map<String, String> additionalEnv) {
        super(cmd, additionalEnv);
    }
}
