/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing;

import java.util.Collection;

import org.hyperledger.fabric.contract.routing.impl.TypeRegistryImpl;

public interface TypeRegistry {

	static TypeRegistry getRegistry(){
		return TypeRegistryImpl.getInstance();
	}

	void addDataType(DataTypeDefinition dtd);

	void addDataType(Class<?> cl);

	DataTypeDefinition getDataType(String name);

	Collection<DataTypeDefinition> getAllDataTypes();

}