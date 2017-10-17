/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.fsm.exceptions;

public class UnknownEventException extends Exception {

	public final String event;

	public UnknownEventException(String event) {
		super("Event '" + event + "' does not exist");
		this.event = event;
	}

}
