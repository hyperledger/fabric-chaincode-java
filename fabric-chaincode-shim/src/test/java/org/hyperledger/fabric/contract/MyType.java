/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONPropertyIgnore;

@DataType
public class MyType {

	@Property()
	private String value;

	
	private String state="";

	public final static String STARTED = "STARTED";
	public final static String STOPPED = "STOPPED";

	@JSONPropertyIgnore()
	public void setState(String state){
		this.state = state;
	}

	@JSONPropertyIgnore()
	public boolean isStarted(){
		return state.equals(STARTED);
	}

	@JSONPropertyIgnore()
	public boolean isStopped(){
		return state.equals(STARTED);
	}

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