/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing;

import java.lang.reflect.Field;

import org.hyperledger.fabric.contract.metadata.TypeSchema;

public interface PropertyDefinition {

	Class<?> getTypeClass();

	TypeSchema getSchema();

	Field getField();

	String getName();

}
