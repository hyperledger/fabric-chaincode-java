/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing;

import java.util.Collection;

public interface TypeRegistry {

	void addDataType(DataTypeDefinition dtd);

	void addDataType(Class<?> cl);

	DataTypeDefinition getDataType(String name);

	Collection<DataTypeDefinition> getAllDataTypes();

}