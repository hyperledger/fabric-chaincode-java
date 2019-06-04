/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract;

import org.hyperledger.fabric.shim.ChaincodeStub;

/**
 *
 * This context is available to all 'transaction functions' and provides the
 * transaction context.
 *
 * It also provides access to the APIs for the world state. {@see ChaincodeStub}
 *
 * Applications can implement their own versions if they wish to add
 * functionality.
 */
public interface Context extends ChaincodeStub {
}
