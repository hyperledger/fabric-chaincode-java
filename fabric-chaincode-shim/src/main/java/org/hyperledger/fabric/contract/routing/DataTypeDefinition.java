/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing;

import java.util.Map;
import org.hyperledger.fabric.contract.metadata.TypeSchema;

public interface DataTypeDefinition {

	String getName();

	Map<String,PropertyDefinition> getProperties();

	String getSimpleName();

	Class<?> getTypeClass();
	
	TypeSchema getSchema();
}