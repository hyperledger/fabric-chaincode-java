/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim;

import java.io.IOException;

public interface GrpcServer {

    void start() throws IOException;

    void stop() throws InterruptedException;

    void blockUntilShutdown() throws InterruptedException;
}
