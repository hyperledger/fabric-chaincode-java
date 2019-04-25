/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing.impl;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hyperledger.fabric.contract.routing.TypeRegistry;

/**
 * Registry to hold the complex data types as defined in the contract
 * Not used extensively at present but will have a great role when data handling comes up
 *
 */
public class TypeRegistryImpl implements TypeRegistry {

	private Map<String, DataTypeDefinitionImpl> components = new HashMap<>();

	/* (non-Javadoc)
	 * @see org.hyperledger.fabric.contract.routing.TypeRegistry#addDataType(java.lang.Class)
	 */
	@Override
	public void addDataType(Class<?> cl) {
		DataTypeDefinitionImpl type = new DataTypeDefinitionImpl(cl);
		components.put(type.getName(), type);
	}

	/* (non-Javadoc)
	 * @see org.hyperledger.fabric.contract.routing.TypeRegistry#getAllDataTypes()
	 */
	@Override
	public Collection<DataTypeDefinitionImpl> getAllDataTypes() {
		return components.values();
	}

}
