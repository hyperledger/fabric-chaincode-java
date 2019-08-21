/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration;

import org.hyperleder.fabric.shim.integration.DockerCompose.DockerComposeBuilder;

public class FabricState {

    private static FabricState state;

    public static FabricState getState(){
        if (state==null){
            state = new FabricState();
        }

        return state;
    }

    private boolean started = false;

    public synchronized void start(){
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


}