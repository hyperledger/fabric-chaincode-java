/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.impl;

import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;

public class NextStateInfo {

	public ChaincodeMessage message;
	public boolean sendToCC;

	public NextStateInfo(ChaincodeMessage message, boolean sendToCC) {
		this.message = message;
		this.sendToCC = sendToCC;
	}

}
