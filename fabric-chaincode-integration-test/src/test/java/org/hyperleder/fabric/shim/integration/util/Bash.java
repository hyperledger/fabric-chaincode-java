/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Represents the 'peer' cli command */
public final class Bash extends Command {

    public static BashBuilder newBuilder() {
        return new BashBuilder();
    }

    public static class BashBuilder extends Command.Builder<Bash> {
        String cmd;
        String orderer;
        String channel;
        String ccname;
        boolean evaluate = false;
        int waitForEventTimeout;
        List<String> args = new ArrayList<String>();
        Map<String, String> transientData;

        public BashBuilder duplicate() {
            try {
                return (BashBuilder) this.clone();
            } catch (CloneNotSupportedException e) {

                e.printStackTrace();
                return null;
            }
        }

        public BashBuilder cmd(String cmd) {
            this.cmd = cmd;
            return this;
        }

        public BashBuilder cmdargs(String argsArray[]) {
            this.args = Arrays.asList(argsArray);
            return this;
        }

        public Bash build(Map<String, String> additionalEnv) {

            ArrayList<String> list = new ArrayList<>();
            list.add(cmd);

            return new Bash(list, additionalEnv);
        }

        public Bash build() {

            ArrayList<String> list = new ArrayList<>();
            list.add(cmd);
            list.addAll(args);

            return new Bash(list);
        }
    }

    Bash(List<String> cmd) {
        super(cmd);
    }

    Bash(List<String> cmd, Map<String, String> env) {
        super(cmd, env);
    }
}
