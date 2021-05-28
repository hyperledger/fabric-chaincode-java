/*
Copyright 2020 IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;

import org.hyperleder.fabric.shim.integration.util.Docker.DockerBuilder;
import org.hyperleder.fabric.shim.integration.util.DockerCompose.DockerComposeBuilder;

/** Utility class to run the setup script once */
public class CommandSingleton {

    private static boolean done = false;

    private static Semaphore flag = new Semaphore(1);

    public static void setup() {

        try {
            // things have not been setup up yet
            flag.acquire();
            if (done) {
                flag.release();
                return;
            }
            // get current working directory for debug and reference purposes only
            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();

            // create the docker-compose command
            DockerComposeBuilder composebuilder = DockerCompose.newBuilder()
                    .file("src/test/resources/first-network/docker-compose-cli.yaml");

            // close down anything running...
            composebuilder.duplicate().down().build().run();

            // ...and bring up
            DockerCompose compose = composebuilder.up().detach().build();
            compose.run();

            // the cli container contains a script that does the channel create, joing
            // and chaincode install/instantiate
            DockerBuilder dockerBuilder = new Docker.DockerBuilder();
            Docker docker = dockerBuilder.exec().container("cli").script("./scripts/script.sh").build();
            docker.run();
            done = true;
            flag.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}