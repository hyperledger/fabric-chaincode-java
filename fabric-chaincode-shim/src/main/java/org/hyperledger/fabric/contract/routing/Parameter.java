/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing;

public interface Parameter {

	Class<?> getType();
	String getSchema();

}
