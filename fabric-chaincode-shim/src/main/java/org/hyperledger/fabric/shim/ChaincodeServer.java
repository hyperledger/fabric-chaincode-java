/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim;

import java.io.IOException;

public interface ChaincodeServer {

    void start() throws Exception;

    void blockUntilShutdown() throws InterruptedException;

}
