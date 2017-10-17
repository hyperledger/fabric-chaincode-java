/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.fsm.exceptions;

public class InTrasistionException extends Exception {

	public final String event;

	public InTrasistionException(String event) {
		super("Event '" + event + "' is inappropriate because"
				+ " the previous trasaction had not completed");
		this.event = event;
	}

}
