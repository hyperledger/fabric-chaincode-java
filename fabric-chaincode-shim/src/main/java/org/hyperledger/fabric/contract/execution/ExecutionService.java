/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.execution;

import org.hyperledger.fabric.contract.routing.Routing;
import org.hyperledger.fabric.shim.Chaincode;

/**
 * Service that executes {@link InvocationRequest} (wrapped INit/Invoke + extra data) using routing information {@link Routing}
 */
public interface ExecutionService {

    Chaincode.Response executeRequest(Routing rd, InvocationRequest req);
}
