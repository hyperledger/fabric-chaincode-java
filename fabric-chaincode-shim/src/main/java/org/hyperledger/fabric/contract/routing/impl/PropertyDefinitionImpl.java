/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.routing.impl;

import java.lang.reflect.Field;

import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.PropertyDefinition;

public class PropertyDefinitionImpl implements PropertyDefinition {

	private Class<?> typeClass;
	private TypeSchema schema;
	private Field field;
	private String name;

	public PropertyDefinitionImpl(String name, Class<?> typeClass, TypeSchema schema, Field f) {
		this.typeClass = typeClass;
		this.schema = schema;
		this.field = f;
		this.name =name;
	}

	@Override
	public Class<?> getTypeClass() {
		return this.typeClass;
	}

	@Override
	public TypeSchema getSchema() {
		return this.schema;
	}

	@Override
	public Field getField() {
		return this.field;
	}

	@Override
	public String getName() {
		return this.name;
	}

}
