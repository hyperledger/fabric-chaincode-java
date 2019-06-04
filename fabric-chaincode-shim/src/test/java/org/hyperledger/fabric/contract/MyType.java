/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class MyType {

	@Property()
	private String value;

	public MyType setValue(String value) {
		this.value = value;
		return this;
	}

	public String getValue() {
		return this.value;
	}

	public String toString() {
		return "++++ MyType: " + value;
	}
}