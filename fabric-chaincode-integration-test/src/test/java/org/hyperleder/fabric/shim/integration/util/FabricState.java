/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.hyperleder.fabric.shim.integration.util.Docker.DockerBuilder;
import org.hyperleder.fabric.shim.integration.util.DockerCompose.DockerComposeBuilder;

public class FabricState {

    private static FabricState state;

    private static Map<String, Boolean> channelStarted = new HashMap<>();

    // sempaphore to protect access
    private static Semaphore flag = new Semaphore(1);

    public static FabricState getState() {
        if (state == null) {
            state = new FabricState();
        }

        return state;
    }

    private boolean started = false;

    public synchronized void start() {
        if (!this.started) {

            // create the docker-compose command
            DockerComposeBuilder composebuilder = DockerCompose.newBuilder()
                    .file("src/test/resources/first-network/docker-compose-cli.yaml");

            // close down anything running...
            composebuilder.duplicate().down().build().run();

            // ...and bring up
            DockerCompose compose = composebuilder.up().detach().build();
            compose.run();

            this.started = true;
        } else {
            System.out.println("Fabric already started....");
        }
    }

    public void startChannel(String channelName) {
        try {
            flag.acquire();
            if (channelStarted.getOrDefault(channelName, false)) {
                return;
            }

            // the cli container contains a script that does the channel create, joing
            // and chaincode install/instantiate
            DockerBuilder dockerBuilder = new Docker.DockerBuilder();
            Docker docker = dockerBuilder.exec().container("cli").script("./scripts/script.sh").channel(channelName)
                    .build();
            docker.run();
            channelStarted.put(channelName, true);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            flag.release();
        }
    }

}