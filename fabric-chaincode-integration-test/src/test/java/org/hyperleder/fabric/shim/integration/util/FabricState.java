/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.hyperleder.fabric.shim.integration.util.Bash.BashBuilder;

public final class FabricState {

    private static FabricState state;

    private static final Map<String, Boolean> channelStarted = new HashMap<>();

    // sempaphore to protect access
    private static final Semaphore flag = new Semaphore(1);

    public static FabricState getState() {
        if (state == null) {
            state = new FabricState();
        }

        return state;
    }

    private boolean started = false;

    public synchronized void start() {

        if (!this.started) {
            BashBuilder bashBuilder = new Bash.BashBuilder().cmd("src/test/resources/scripts/mfsetup.sh");
            bashBuilder.build().run();
            this.started = true;
        } else {
            System.out.println("Fabric already started....");
        }
    }

    public Map<String, String> orgEnv(String org) {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();

        Map<String, String> env = new HashMap<>();

        env.put(
                "CORE_PEER_MSPCONFIGPATH",
                Paths.get(s, "src/test/resources/_cfg/_msp/" + org, org + "admin/msp")
                        .toString());
        env.put("CORE_PEER_LOCALMSPID", org + "MSP");
        env.put("CORE_PEER_ADDRESS", org + "peer-api.127-0-0-1.nip.io:8080");

        System.out.println(env);
        return env;
    }
}
