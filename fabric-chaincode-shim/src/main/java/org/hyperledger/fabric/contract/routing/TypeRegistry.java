/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing;

import java.util.Collection;

import org.hyperledger.fabric.contract.routing.impl.DataTypeDefinitionImpl;

public interface TypeRegistry {

	void addDataType(Class<?> cl);

	Collection<DataTypeDefinitionImpl> getAllDataTypes();

}