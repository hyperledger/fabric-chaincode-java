/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.fsm.exceptions;

public class InvalidEventException extends Exception {

	public final String event;
	public final String state;

	public InvalidEventException(String event, String state) {
		super("Event '" + event + "' is innappropriate"
				+ " given the current state, " + state);
		this.event = event;
		this.state = state;
	}

}
